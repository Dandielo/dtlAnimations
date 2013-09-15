package net.dandielo.animation;

import java.io.File;
import java.util.List;

import net.dandielo.bukkit.DtlAnimations;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.Packet52MultiBlockChange;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class AnimationFrame implements Cloneable {
	private List<Packet52MultiBlockChange> data;
	private int schedule;
	private Location location;
	private String name;
	private String filename;
	
	private boolean defaultFrame; 
	
	public AnimationFrame(AnimationSet animation, String filename, String name)
	{
		String filepath = "plugins/dtlAnimations/frames";
		
		//set the frame name
		this.name = name;
		this.filename = filename;
		
		schedule = animation.getSchedule();
		location = animation.getLocation();
		
		defaultFrame = false;
		
		try
		{
			data = DtlAnimations.getInstance().getPacketsManager().fromSchematic(filepath, filename, location);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public void setScheduleTime(int s)
	{
		schedule = s;
	}
	
	public void setLocation(Location loc)
	{
		location = loc;
	}
	
	public AnimationFrame(AnimationSet animation, ConfigurationSection frame, String name)
	{
		String filepath = "";
		String filename = "";
		
		//set the name
		this.name = name;

		if (frame.contains("file"))
		{
			filepath = frame.getString("path", "plugins/dtlAnimations/frames");
			this.filename = filename = frame.getString("file");
			
			defaultFrame = frame.getBoolean("default", false);
			
			ConfigurationSection loc = frame.getConfigurationSection("location"); 
			if ( loc != null )
			location = new Location(DtlAnimations.getInstance().getServer().getWorld(loc.getString("world")),
					loc.getDouble("x"), 
					loc.getDouble("y"),
					loc.getDouble("z")
					);
			
			schedule = frame.getInt("schedule", animation.getSchedule());
			
			if ( location == null )
				location = animation.getLocation();
		}
		
		//duplication for denizen scripts
		else
		{
			filepath = frame.getString("PATH", "plugins/dtlAnimations/frames");
			this.filename = filename = frame.getString("FILE");
			
			defaultFrame = frame.getBoolean("DEFAULT", false);
			
			ConfigurationSection loc = frame.getConfigurationSection("LOCATION"); 
			if ( loc != null )
			location = new Location(DtlAnimations.getInstance().getServer().getWorld(loc.getString("WORLD")),
					loc.getDouble("X"), 
					loc.getDouble("Y"),
					loc.getDouble("Z")
					);
			
			schedule = frame.getInt("SCHEDULE", animation.getSchedule());
			
			if ( location == null )
				location = animation.getLocation();
		}
		
		try
		{
			data = DtlAnimations.getInstance().getPacketsManager().fromSchematic(filepath, filename, location);
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public void copyToServer()
	{
		String path = "plugins/dtlAnimations/frames";
		File file = new File(path,filename+".schematic");
		
		if ( !file.exists() ) return;

		WorldEdit we = DtlAnimations.getInstance().getWE().getWorldEdit();
		LocalSession local = new LocalSession(we.getConfiguration());
		EditSession edit = new EditSession(new BukkitWorld(location.getWorld()), we.getConfiguration().maxChangeLimit);
		edit.enableQueue();
		try
		{
			local.setClipboard(SchematicFormat.MCEDIT.load(file));
			local.getClipboard().place(edit, getPastePosition(local, location), false);
		}
		catch( Exception e ) { }
		edit.flushQueue();
		we.flushBlockBag(null, edit);
	}
	
	private Vector getPastePosition(LocalSession local, Location loc) throws EmptyClipboardException {
		if (loc == null) 
			return local.getClipboard().getOrigin();
		else 
			return new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getFile()
	{
		return filename;
	}
	
	public boolean isDefault()
	{
		return defaultFrame;
	}
	
	public void sendTo(Player player)
	{
		EntityPlayer E = ((CraftPlayer)player).getHandle();
		
		for ( Packet52MultiBlockChange packet : data )
		{
			E.playerConnection.sendPacket(packet);
		}
	}
	
	public int getSchedule()
	{
		return schedule;
	}
	
	@Override
	public boolean equals(Object that)
	{
		return name.equals(((AnimationFrame)that).name);
	}
	
}
