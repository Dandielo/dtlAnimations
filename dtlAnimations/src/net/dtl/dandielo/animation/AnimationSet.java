package net.dtl.dandielo.animation;

import static net.dtl.dandielo.animation.AnimationManager.utils;

import java.util.ArrayList;
import java.util.List;

import net.dtl.dandielo.FrameLoader;
import net.dtl.dandielo.bukkit.DtlAnimations;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class AnimationSet implements Comparable<AnimationSet> {

	private String name;
	private List<AnimationFrame> frames;

	private Location location;
	private int distance; 
	private int shedule;

	private int frame = 0;
	
	//added for clonning
	private AnimationSet(AnimationSet animation)
	{
		name = animation.name;
		frames = new ArrayList<AnimationFrame>(frames);
		location = animation.location;
		distance = animation.distance;
		shedule = animation.shedule;
	}
	
	public AnimationSet(ConfigurationSection animation)
	{
		frames = new ArrayList<AnimationFrame>();

		// Ugly code duplication here since Denizen script yaml keys are always uppercase. 
		// Sorry :( Maybe there's a better way?

		// The animation (if lowercase keys)
		if (animation.contains("name")) 
		{
			name = animation.getString("name", animation.getName());
			distance = animation.getInt("distance", 60);
			shedule = animation.getInt("schedule", 20);
			
			ConfigurationSection loc = animation.getConfigurationSection("location"); 
			location = new Location(DtlAnimations.getInstance().getServer().getWorld(loc.getString("world")),
					loc.getDouble("x"), 
					loc.getDouble("y"),
					loc.getDouble("z")
					);
			
			for ( String frame : animation.getConfigurationSection("frames").getKeys(false) )
				frames.add( new AnimationFrame(this, animation.getConfigurationSection( FrameLoader.buildPath("frames",frame) )) );
		} 
		
		else // Try uppercase keys (because of Denizen)	
		{
			name = animation.getString("NAME", animation.getName());
			distance = animation.getInt("DISTANCE", 60);
			shedule = animation.getInt("SCHEDULE", 20);
			
			ConfigurationSection loc = animation.getConfigurationSection("LOCATION"); 
			location = new Location(DtlAnimations.getInstance().getServer().getWorld(loc.getString("WORLD")),
									loc.getDouble("X"), 
									loc.getDouble("Y"),
									loc.getDouble("Z")
									);//utils.makeLocation(animation.getString("LOCATION"));

			for ( String frame : animation.getConfigurationSection("FRAMES").getKeys(false) )
				frames.add( new AnimationFrame(this, animation.getConfigurationSection( FrameLoader.buildPath("frames",frame) )) );
		}

		// Loaded animation
		System.out.print("Loaded animation " + name + " with " + frames.size() + " frames.");

	}

	public void nextFrame()
	{
		++frame;
		frame %= frames.size();
	}

	public AnimationFrame getFrame()
	{
		return frames.get(frame);
	}

	public Location getLocation()
	{
		return location;
	}	

	public int getShedule()
	{
		return shedule;
	}

	public int getDistance()
	{
		return distance;
	}

	@Override 
	public int hashCode()
	{
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if ( !( o instanceof AnimationSet ) )
			return false;

		return ((AnimationSet)o).name.equals(name);
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(AnimationSet animation) {
		return name.compareTo(animation.name);
	}


	
	//To avoid loading every animation more than one time for Denizen scripts
	private boolean running;

	
	//So animations can be added multiplied for a player
	public AnimationSet runAs(String player)
	{
		AnimationSet animation = new AnimationSet(this);
		animation.name += "_" + player;
		animation.running = true;
		return animation;
	}
	
	public boolean running()
	{
		return running;
	}
	
	public void setRunning(boolean run)
	{
		running = run;
	}
	
}
