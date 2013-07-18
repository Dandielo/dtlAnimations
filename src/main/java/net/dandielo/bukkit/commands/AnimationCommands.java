package net.dandielo.bukkit.commands;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.dandielo.animation.AnimationFrame;
import net.dandielo.animation.AnimationSet;
import net.dandielo.bukkit.DtlAnimations;
import net.dandielo.commands.Command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;

import static net.dandielo.bukkit.commands.AnimationCommands.AnimCreationSteps.CreationStep.*;

public class AnimationCommands implements Listener {

	private Map<String, AnimCreationSteps> players = new HashMap<String, AnimCreationSteps>();
	public static WorldEditPlugin we;
	
	@Command(
	name = "anim",
	syntax = "create <name>",
	perm = "dtl.anim.commands.create")
	public void createAnimation(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) ) return;
		if ( players.containsKey(sender.getName()) ) return;

		AnimCreationSteps steps = new AnimCreationSteps(args.get("name"), ((Player)sender).getLocation());

		players.put(sender.getName(), steps);
		steps.setSession(DtlAnimations.getInstance().getWE().getSession((Player) sender));

		sender.sendMessage(ChatColor.AQUA + "Started a new animation setup: " + ChatColor.DARK_AQUA + args.get("name"));
	}

	@Command(
	name = "frame",
	syntax = "save <frame>",
	perm = "dtl.anim.commands.create-frame")
	public void saveFrame(DtlAnimations plugin, CommandSender sender, Map<String, String> args) throws IncompleteRegionException
	{
		if ( !(sender instanceof Player) ) return;
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.areaEditStep() ) return;

		steps.saveFrame((Player) sender, args.get("frame"));
		sender.sendMessage(ChatColor.AQUA + "Frame saved: " + ChatColor.DARK_AQUA + args.get("frame"));
	}

	@Command(
	name = "frame",
	syntax = "schedule <time>",
	perm = "dtl.anim.commands.schedule-frame")
	public void scheduleFrame(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) ) return;
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.frameSettingStep() ) return;

		try
		{
			steps.setFrameSchedule(Integer.parseInt(args.get("time")));
			sender.sendMessage(ChatColor.AQUA + "Frame schedule time changed: " + ChatColor.DARK_AQUA + args.get("time"));
		}
		catch ( Exception e )
		{
			sender.sendMessage(ChatColor.RED + "Frame schedule invalid: " + ChatColor.DARK_AQUA + args.get("time"));
		}

	}

	@Command(
	name = "frame",
	syntax = "next",
	perm = "dtl.anim.commands.next-frame")
	public void nextFrame(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) ) return;
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.frameSettingStep() ) return;

		steps.nextFrame();
		
		sender.sendMessage(ChatColor.AQUA + "Now set your next frame");
	}
	
	@Command(
	name = "anim",
	syntax = "schedule <time>",
	perm = "dtl.anim.commands.schedule")
	public void scheduleAnimation(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) ) return;
		AnimCreationSteps steps = players.get(sender.getName());
		if ( steps == null ) return;
		
		try
		{
			steps.setSchedule(Integer.parseInt(args.get("time")));
			sender.sendMessage(ChatColor.AQUA + "Schedule time changed: " + ChatColor.DARK_AQUA + args.get("time"));
		}
		catch( Exception e )
		{
			sender.sendMessage(ChatColor.RED + "Schedule time invalid: " + ChatColor.DARK_AQUA + args.get("time"));
		}
	}
	
	@Command(
	name = "anim",
	syntax = "distance <dist>",
	perm = "dtl.anim.commands.distance")
	public void distanceAnimation(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) ) return;
		AnimCreationSteps steps = players.get(sender.getName());
		if ( steps == null ) return;
		
		try
		{
			steps.setDistance(Integer.parseInt(args.get("dist")));
			sender.sendMessage(ChatColor.AQUA + "Distance changed: " + ChatColor.DARK_AQUA + args.get("time"));
		}
		catch( Exception e )
		{
			sender.sendMessage(ChatColor.RED + "Distance invalid: " + ChatColor.DARK_AQUA + args.get("time"));
		}
	}

	@Command(
	name = "anim",
	syntax = "finish",
	perm = "dtl.anim.commands.finish")
	public void finishAnimation(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) ) return;
		if ( !players.containsKey(sender.getName()) ) return;

		AnimCreationSteps steps = players.get(sender.getName());
		plugin.getLoader().addAnnimationYaml(steps.asAnimation().toString(), steps.asYaml());
		//DtlAnimations.getInstance().getAnimationManager().addAnimation(steps.asAnimation());
		
		sender.sendMessage(ChatColor.AQUA + "Animation finished: " + ChatColor.DARK_AQUA + steps.asAnimation().toString());
		
		players.remove(sender.getName());
	}

	@Command(
	name = "anim",
	syntax = "cancel",
	perm = "dtl.anim.commands.cancel")
	public void cancelAnimation(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) ) return;
		if ( !players.containsKey(sender.getName()) ) return;

		AnimCreationSteps steps = players.get(sender.getName());
		sender.sendMessage(ChatColor.AQUA + "Creation cancelled: " + ChatColor.DARK_AQUA + steps.asAnimation().toString());
		
		players.remove(sender.getName());
	}
	
	@Command(
	name = "anim",
	syntax = "start <name>",
	perm = "dtl.anim.commands.start")
	public void startAnim(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		AnimationSet anim = plugin.getLoader().getAnimation(args.get("name"));
		if ( anim == null || plugin.getManager().isRunning(anim) )
		{
			sender.sendMessage(ChatColor.RED + "Falied to start the requested animation: " + ChatColor.DARK_AQUA + args.get("name"));
		}
		else
		{
			sender.sendMessage(ChatColor.AQUA + "Trying to start the requested animation: " + ChatColor.DARK_AQUA + args.get("name"));
			plugin.getManager().addAnimation(anim);
		}
	}

	@Command(
	name = "anim",
	syntax = "stop <name>",
	perm = "dtl.anim.commands.stop")
	public void stopAnim(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( args.get("name").equals("all") )
		{
			sender.sendMessage(ChatColor.AQUA + "Trying to stop all animations");
			plugin.getManager().removeAllAnimations();
			return;
		}
		
		AnimationSet anim = plugin.getLoader().getAnimation(args.get("name"));
		if ( anim == null || !plugin.getManager().isRunning(anim) )
		{
			sender.sendMessage(ChatColor.RED + "Falied to stop the requested animation: " + ChatColor.DARK_AQUA + args.get("name"));
		}
		else
		{
			sender.sendMessage(ChatColor.AQUA + "Trying to stop the requested animation: " + ChatColor.DARK_AQUA + args.get("name"));
			plugin.getManager().removeAnimation(anim);
		}
	}

	static class AnimCreationSteps
	{
		private String name;
		private CreationStep step;
		private LocalSession session;

		private AnimationSet anim;
		private AnimationFrame frame;
		private int frameCount;
		
		private YamlConfiguration yaml;

		public AnimCreationSteps(String name, Location loc)
		{
			//set the animation name
			this.name = name.replace(" ", "_");
			
			yaml = new YamlConfiguration();
			anim = new AnimationSet(name);
			anim.setLocation(loc);
			
			//save the basic animation yaml
			yaml.set(name + ".name", name);
			yaml.set(name + ".schedule", anim.getSchedule());
			yaml.set(name + ".distance", anim.getDistance());
			yaml.set(name + ".location.x", loc.getBlockX());
			yaml.set(name + ".location.y", loc.getBlockY());
			yaml.set(name + ".location.z", loc.getBlockZ());
			yaml.set(name + ".location.world", loc.getWorld().getName());
			
			frameCount = 0;
			
			step = AREA_EDIT;
		}

		public void setDistance(int parseInt)
		{
			anim.setDistance(parseInt);
			yaml.set(name + ".distance", anim.getDistance());
		}

		public void setSchedule(int parseInt)
		{
			anim.setSchedule(parseInt);
			yaml.set(name + ".schedule", anim.getSchedule());
		}

		public void setFrameSchedule(int parseInt)
		{
			frame.setScheduleTime(parseInt);
			if ( yaml.contains(name + "frames." + frameCount) )
			    yaml.set(name + "frames." + frameCount + ".schedule", anim.getSchedule());
		}

		public void setSession(LocalSession localSession)
		{
			this.session = localSession;
		}

		public AnimationSet asAnimation()
		{
			step = ANIMATION_FINISHED;
			return anim;
		}
		
		public void nextFrame()
		{
			step = AREA_EDIT;
			frameCount++;
		}

		public void saveFrame(Player player, String name)
		{
			if ( !new File("plugins/dtlAnimations/frames").exists() )
				new File("plugins/dtlAnimations/frames").mkdirs();
			
			File file = new File("plugins/dtlAnimations/frames", name + ".schematic");
			
			step = FRAME_SETTING;

			try
			{
				if ( !file.exists() )
					file.createNewFile();
				
				//get the region
				Region region = we.getSelection(player).getRegionSelector().getRegion();
				
				//get the LocalPlayer
				LocalPlayer localPlayer = we.wrapCommandSender(player);

				//save the selected cuboid
				Vector max = region.getMaximumPoint();
				Vector min = region.getMinimumPoint();
				Vector pos = session.getPlacementPosition(localPlayer);
				
				Vector loc = min;
				anim.setLocation(new Location(Bukkit.getWorld(localPlayer.getWorld().getName()), loc.getX(), loc.getY(), loc.getZ()));
				yaml.set(this.name + ".location.x", loc.getBlockX());
				yaml.set(this.name + ".location.y", loc.getBlockY());
				yaml.set(this.name + ".location.z", loc.getBlockZ());
				yaml.set(this.name + ".location.world", localPlayer.getWorld().getName());

				//create the clipboard
				CuboidClipboard cb = new CuboidClipboard(max.subtract(min).add(new Vector(1,1,1)), 
						min, min.subtract(pos));
				
				//create the edit session and copy all blocks
				cb.copy(session.createEditSession(localPlayer));
				
				//save the schematic
				SchematicFormat.MCEDIT.save(cb, file);
				
				//create the new AnimationFrame
				frame = new AnimationFrame(anim, name);
			//	Location loc = player.getLocation();
				frame.setLocation(anim.getLocation());
				
				//frame yaml saving
				yaml.set(this.name + ".frames." + frameCount + ".file", name);
				/*yaml.set(this.name + ".frames." + frameCount + ".location.x", loc.getBlockX());
				yaml.set(this.name + ".frames." + frameCount + ".location.y", loc.getBlockY());
				yaml.set(this.name + ".frames." + frameCount + ".location.z", loc.getBlockZ());
				yaml.set(this.name + ".frames." + frameCount + ".location.world", localPlayer.getWorld().getName());*/

				//add the frame to the animation
				anim.addFrame(frame);
			}
			catch( IncompleteRegionException e )
			{
				player.sendMessage(ChatColor.RED + "Frame cuboid selection is invalid!");
			}
			catch( Exception e ) { e.printStackTrace(); } 
		}

		public YamlConfiguration asYaml()
		{
			return yaml;
		}
		
		
		public boolean areaSelectionStep()
		{
			return step.equals(AREA_SELECT);
		}

		public boolean areaEditStep()
		{
			return step.equals(AREA_EDIT);
		}

		public boolean frameSettingStep()
		{
			return step.equals(FRAME_SETTING);
		}

		enum CreationStep
		{
			AREA_SELECT, AREA_EDIT, FRAME_SETTING, ANIMATION_FINISHED;
		}
	}





	/*     Vector min = sel.getNativeMinimumPoint();
    Vector max = sel.getNativeMaximumPoint();
    for(int x = min.getBlockX();x <= max.getBlockX(); x=x+1){
        for(int y = min.getBlockY();y <= max.getBlockY(); y=y+1){
            for(int z = min.getBlockZ();z <= max.getBlockZ(); z=z+1){
                Location tmpblock = new Location(((Player)sender).getWorld(), x, y, z);
                tmpblock.getBlock().setType(Material.AIR); 
            }
        }
    }*/
}
