package net.dtl.dandielo.animation;

import static net.dtl.dandielo.animation.AnimationManager.utils;

import java.util.List;

import net.dtl.api.PacketsAPI;
import net.dtl.dandielo.bukkit.DtlAnimations;
import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.Packet52MultiBlockChange;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnimationFrame {
	private List<Packet52MultiBlockChange> data;
	private int shedule;
	private Location location;
	
	private boolean defaultFrame; 
	
	public AnimationFrame(AnimationSet animation, ConfigurationSection frame)
	{
		String filepath = frame.getString("path", "plugins/DtlAnimations/frames");
		String filename = frame.getString("file");
		
		defaultFrame = frame.getBoolean("default", false);
		
		ConfigurationSection loc = frame.getConfigurationSection("location"); 
		if ( loc != null )
		location = new Location(DtlAnimations.getInstance().getServer().getWorld(loc.getString("world")),
				loc.getDouble("x"), 
				loc.getDouble("y"),
				loc.getDouble("z")
				);
		
		shedule = frame.getInt("shedule", animation.getShedule());
		
		if ( location == null )
			location = animation.getLocation();
		
		try
		{
			data = PacketsAPI.getInstance().getBlocksManager().fromSchematic(filepath, filename, location);
		} catch (Exception e) 
		{
			//TODO nice debuger 
		//	System.out.print("Failed to load frame from: " + filepath.replace('\\', '/') + "/" + filename);
			e.printStackTrace();
		}
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
	
	public int getShedule()
	{
		return shedule;
	}
	
}