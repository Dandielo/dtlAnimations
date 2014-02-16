package net.dandielo;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dandielo.jnbt.ByteArrayTag;
import net.dandielo.jnbt.CompoundTag;
//import net.dandielo.jnbt.IntTag;
import net.dandielo.jnbt.NBTInputStream;
import net.dandielo.jnbt.ShortTag;
import net.dandielo.jnbt.StringTag;
import net.dandielo.jnbt.Tag;
import net.minecraft.server.v1_7_R1.PacketDataSerializer;
import net.minecraft.server.v1_7_R1.PacketPlayOutMultiBlockChange;
import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.EmptyByteBuf;
import net.minecraft.util.io.netty.buffer.UnpooledByteBufAllocator;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class PacketsManager {

	public List<PacketPlayOutMultiBlockChange> fromSchematic(String path, String filename, Location pos) throws Exception
	{
		Map<Chunk, List<byte[]>> prePackets = asPrePacketDatas(path, filename, pos);
		List<PacketPlayOutMultiBlockChange> packets = new ArrayList<PacketPlayOutMultiBlockChange>();
		
		/** changing data into packets*/
		for ( Map.Entry<Chunk, List<byte[]>> entry : prePackets.entrySet() )
		{
			PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();

			
			ByteBuf buffer = new EmptyByteBuf(new UnpooledByteBufAllocator(false));
			PacketDataSerializer ser = new PacketDataSerializer(buffer);
			
			int ecX = entry.getKey().getX();
			int ecZ = entry.getKey().getZ();
			int rC = entry.getValue().size();

			ser.writeInt(ecX);
			ser.writeInt(ecZ);
			ser.writeShort(rC);
			ser.writeInt(rC*4);
			
			/*
			packet.a = ecX;
			packet.b = ecZ;
			packet.d = entry.getValue().size()*4;
			*/
		//	byte[] data = new byte[rC];
		//	int i = 0;
			for ( byte[] d : entry.getValue() )
			{
				ser.writeBytes(d);
			//	data[i] = d[0];
			//	data[i+1] = d[1];
			//	data[i+2] = d[2]; 
			//	data[i+3] = d[3]; 
				
			//	System.out.print(data[i] + " " + data[i+1] + " " + data[i+2] + " " + data[i+3]);
			//	i += 4;
			}/*
			
			packet.c = data;
			 */
			packet.a(ser);
			
			packets.add(packet);
		}
		
		return packets;
	}
	
	public Map<Chunk, List<byte[]>> asPrePacketDatas(String path, String filename, Location dest) throws Exception
	{
		File file = new File(path,filename+".schematic");

		if(!file.exists()) throw(new java.io.FileNotFoundException("File not found"));

		FileInputStream stream = new FileInputStream(file);
		NBTInputStream nbtStream = new NBTInputStream(new java.util.zip.GZIPInputStream(stream));

	//	Vector origin = new Vector();
	//	Vector offset = new Vector();

		// Schematic tag
		CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
		nbtStream.close();

		if (!schematicTag.getName().equals("Schematic")) {
			throw new Exception("Tag \"Schematic\" does not exist or is not first");
		}

		// Check
		Map<String, Tag> schematic = schematicTag.getValue();

		if (!schematic.containsKey("Blocks")) {
			throw new Exception("Schematic file is missing a \"Blocks\" tag");
		}

		// Get information
		short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
		short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
		short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

	/*	try {
			int originX = getChildTag(schematic, "WEOriginX", IntTag.class).getValue();
			int originY = getChildTag(schematic, "WEOriginY", IntTag.class).getValue();
			int originZ = getChildTag(schematic, "WEOriginZ", IntTag.class).getValue();
	//		origin = new org.bukkit.util.Vector(originX, originY, originZ);
		} catch (Exception e) {
			// No origin data
		}

		try {
			int offsetX = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
			int offsetY = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
			int offsetZ = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();
	//		offset = new Vector(offsetX, offsetY, offsetZ);
		} catch (Exception e) {
			// No offset data
		}*/

		// Check type of Schematic
		String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
		if (!materials.equals("Alpha")) {
			throw new Exception("Schematic file is not an Alpha schematic");
		}

		// Get blocks
		byte[] rawBlocks = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
		byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
		short[] blocks = new short[rawBlocks.length];

		/*if (schematic.containsKey("AddBlocks")) {
			byte[] addBlockIds = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
			for (int i = 0, index = 0; i < addBlockIds.length && index < blocks.length; ++i) {
				blocks[index] = (short) (((addBlockIds[i] >> 4) << 8) + (rawBlocks[index++] & 0xFF));
				if (index < blocks.length) {
					blocks[index] = (short) (((addBlockIds[i] & 0xF) << 8) + (rawBlocks[index++] & 0xFF));
				}
			}
		} else {*/
			for (int i = 0; i < rawBlocks.length; ++i) {
				blocks[i] = (short) (((rawBlocks[i] & 0xFF) << 4 ) | ( blockData[i] & 0x0F ));
			}
	//	}
		
//		for (int x = 0; x < width; ++x) {
//			for (int y = 0; y < height; ++y) {
//				for (int z = 0; z < length; ++z) {
//					int index = y * width * length + z * width + x;
//					
//				}
//			}
//		}
				
		int osX = dest.getBlockX();
		int osY = dest.getBlockY();
		int osZ = dest.getBlockZ();
		

		int scpX = osX % 16;
		int scpZ = osZ % 16;

		/** calculating to chunk dimensions */
		int cx = osX / 16;
		int cz = osZ / 16;
		
		Map<Chunk, List<byte[]>> packets = new HashMap<Chunk, List<byte[]>>();
		byte[] data = null;
		
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				for (int z = 0; z < length; ++z) {
					int index = y * width * length + z * width + x;
				//	Block b = world.getBlockAt(loc1.getBlockX() + ix, loc1.getBlockY() +  iy, loc1.getBlockZ() +  iz);
				//	Chunk c = b.getChunk();

					
					//-2
					//-3
					
					int bcx = scpX + x;
					int bcz = scpZ + z;
					
					/** chunk offsets*/
					int cosX = bcx < 0 ? -1 : bcx / 16;
					int cosZ = bcz < 0 ? -1 : bcz / 16;
					
					if ( bcx < 0 )
						bcx += 16;
					else
						bcx %= 16;
					
					if ( bcz < 0 )
						bcz += 16;
					else
						bcz %= 16;
					
					Chunk c = dest.getWorld().getChunkAt(cx + cosX, cz + cosZ);
					
					if ( !packets.containsKey(c) )
						packets.put(c, new ArrayList<byte[]>());
					
					/** initialize a data block */
					data = new byte[4];
					
					data[0] = (byte) ( ( bcx << 4 ) | bcz ); 
					data[1] = (byte) ((byte) y + osY);
					data[2] = (byte) ( blocks[index] >> 8 );
					data[3] = (byte) ((byte) ( blocks[index] & 0x00f0 ) | ( 0x000f & blocks[index] ) );
					
					packets.get(c).add(data);
				}
			}
		}

		return packets;
			
	}
	
	/**
	 * Get child tag of a NBT structure.
	 *
	 * @param items The parent tag map
	 * @param key The name of the tag to get
	 * @param expected The expected type of the tag
	 * @return child tag casted to the expected type
	 * @throws DataException if the tag does not exist or the tag is not of the expected type
	 	*/
	private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key,
			Class<T> expected) throws Exception {

		if (!items.containsKey(key)) {
			throw new Exception("Schematic file is missing a \"" + key + "\" tag");
		}
		Tag tag = items.get(key);
		if (!expected.isInstance(tag)) {
			throw new Exception(
					key + " tag is not of tag type " + expected.getName());
		}
		return expected.cast(tag);
	}
	
	
}
