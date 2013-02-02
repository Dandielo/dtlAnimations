package net.dandielo.animation;

import java.util.List;

import net.dandielo.bukkit.DtlAnimations;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.Packet52MultiBlockChange;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnimationFrame implements Cloneable {
	private List<Packet52MultiBlockChange> data;
	private int schedule;
	private Location location;
	
	private boolean defaultFrame; 
	
	public AnimationFrame(AnimationSet animation, ConfigurationSection frame)
	{
		String filepath = "";
		String filename = "";

		if (frame.contains("file"))
		{
			filepath = frame.getString("path", "plugins/DtlAnimations/frames");
			filename = frame.getString("file");
			
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
			filepath = frame.getString("PATH", "plugins/DtlAnimations/frames");
			filename = frame.getString("FILE");
			
			defaultFrame = frame.getBoolean("DEFAULT", false);
			
			ConfigurationSection loc = frame.getConfigurationSection("LOCATION"); 
			if ( loc != null )
			location = new Location(DtlAnimations.getInstance().getServer().getWorld(loc.getString("WORLD")),
					loc.getDouble("X"), 
					loc.getDouble("Y"),
					loc.getDouble("Z")
					);
			
			schedule = frame.getInt("SHEDULE", animation.getSchedule());
			
			if ( location == null )
				location = animation.getLocation();
		}
		
		try
		{
			data = DtlAnimations.getInstance().getPacketsManager().fromSchematic(filepath, filename, location);
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
	
	public int getSchedule()
	{
		return schedule;
	}
	
}
