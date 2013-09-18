package net.dandielo.bukkit.commands;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
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
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "You cannot open another edit session!");
			return;
		}

		AnimCreationSteps steps = new AnimCreationSteps(args.get("name"));

		players.put(sender.getName(), steps);
		steps.setSession(DtlAnimations.getInstance().getWE().getSession((Player) sender));

		sender.sendMessage(ChatColor.GOLD + "Started a new animation setup: " + ChatColor.DARK_AQUA + args.get("name"));
	}
	
	@Command(
	name = "anim",
	syntax = "load <name>",
	perm = "dtl.anim.commands.load")
	public void loadAnimation(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "You cannot open another edit session!");
			return;
		}
		
		AnimCreationSteps steps = new AnimCreationSteps(args.get("name"), true);

		players.put(sender.getName(), steps);
		steps.setSession(DtlAnimations.getInstance().getWE().getSession((Player) sender));
		sender.sendMessage(ChatColor.GOLD + "Loaded animation: " + ChatColor.DARK_AQUA + args.get("name"));
	}

	@Command(
	name = "frame",
	syntax = "save <frame>",
	perm = "dtl.anim.commands.save-frame")
	public void saveFrame(DtlAnimations plugin, CommandSender sender, Map<String, String> args) throws IncompleteRegionException
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.areaEditStep() )
		{
			sender.sendMessage(ChatColor.GOLD + "If you want to add a new frame use this command: " + ChatColor.DARK_AQUA + "/frame next");
		}

		steps.saveFrame((Player) sender, args.get("frame"), steps.frameCount(), false);
		sender.sendMessage(ChatColor.GOLD + "Frame saved: " + ChatColor.DARK_AQUA + args.get("frame"));
	}

	@Command(
	name = "frame",
	syntax = "saveat <index> <frame>",
	perm = "dtl.anim.commands.saveat-frame")
	public void saveFrameAt(DtlAnimations plugin, CommandSender sender, Map<String, String> args) throws IncompleteRegionException
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.areaEditStep() )
		{
			sender.sendMessage(ChatColor.GOLD + "If you want to add a new frame use this command: " + ChatColor.DARK_AQUA + "/frame next");
		}

		try
		{
		    steps.saveFrame((Player) sender, args.get("frame"), Integer.parseInt(args.get("index")), false);
		    sender.sendMessage(ChatColor.GOLD + "Frame saved: " + ChatColor.DARK_AQUA + args.get("frame"));
		} catch( Exception e )
		{
			sender.sendMessage(ChatColor.RED + "Wrong index was supplied");
		}
		
	}

	@Command(
	name = "frame",
	syntax = "replace <frame>",
	perm = "dtl.anim.commands.replace-frame")
	public void saveFrameAs(DtlAnimations plugin, CommandSender sender, Map<String, String> args) throws IncompleteRegionException
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.areaEditStep() )
		{
			sender.sendMessage(ChatColor.GOLD + "If you want to set a new frame use this command: " + ChatColor.DARK_AQUA + "/frame next");
			return;
		}

		steps.saveFrame((Player) sender, args.get("frame"), steps.frameCount(), true);
		sender.sendMessage(ChatColor.GOLD + "Frame replaced: " + ChatColor.DARK_AQUA + args.get("frame"));
	}
	
	@Command(
	name = "frame",
	syntax = "edit <frame>",
	perm = "dtl.anim.commands.edit-frame")
	public void editFrame(DtlAnimations plugin, CommandSender sender, Map<String, String> args) throws IncompleteRegionException
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.frameSettingStep() )
		{
			sender.sendMessage(ChatColor.GOLD + "Finish or cancel this frame before editing another");
		}

		if ( steps.editFrame(args.get("frame")) )
		    sender.sendMessage(ChatColor.GOLD + "Frame loaded and pasted");
		else
			sender.sendMessage(ChatColor.RED + "Frame not found");
	}

	@Command(
	name = "frame",
	syntax = "schedule <time>",
	perm = "dtl.anim.commands.schedule-frame")
	public void scheduleFrame(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.frameSettingStep() )
		{
			sender.sendMessage(ChatColor.GOLD + "Finish or cancel this frame to use this command");
		}

		try
		{
			steps.setFrameSchedule(Integer.parseInt(args.get("time")));
			sender.sendMessage(ChatColor.GOLD + "Frame schedule time changed: " + ChatColor.DARK_AQUA + args.get("time"));
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
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.frameSettingStep() )
		{
			sender.sendMessage(ChatColor.GOLD + "Finish this frame before adding a new one");
			return;
		}

		steps.nextFrame();
		sender.sendMessage(ChatColor.GOLD + "Started next frame edit process");
	}

	@Command(
	name = "frame",
	syntax = "list",
	perm = "dtl.anim.commands.list-frame")
	public void frameList(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		
		StringBuilder builder = new StringBuilder();
		for ( AnimationFrame frame : steps.incompleteAnimation().getFrames() )
			builder.append(", " + ChatColor.DARK_AQUA + frame.getName() + ChatColor.GOLD);
		
		//send the frame list list
		sender.sendMessage(ChatColor.GOLD + "Frame list: " + builder.toString().substring(2));
	}
	
	@Command(
	name = "frame",
	syntax = "remove <name>",
	perm = "dtl.anim.commands.remove-frame")
	public void frameRemove(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.frameSettingStep() ) 
		{
			sender.sendMessage(ChatColor.GOLD + "Finish or cancel current frame before using this command");
			return;
		}
		
		if ( steps.removeFrame(args.get("name")) )
		    sender.sendMessage(ChatColor.GOLD + "Frame removed sucessfuly");
		else
		    sender.sendMessage(ChatColor.RED + "Frame not found");
	}
	
	@Command(
	name = "frame",
	syntax = "show <name>",
	perm = "dtl.anim.commands.show-frame")
	public void frameShow(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.frameSettingStep() )
		{
			sender.sendMessage(ChatColor.GOLD + "Finish or cancel this frame before displaying another");
			return;
		}
		
		steps.showFrame(args.get("name"));
		sender.sendMessage(ChatColor.GOLD + "Frame pasted");
	}
	
	@Command(
	name = "frame",
	syntax = "cancel",
	perm = "dtl.anim.commands.show-frame")
	public void frameCancel(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) )
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( !steps.areaEditStep() ) 
		{
			sender.sendMessage(ChatColor.GOLD + "Nothing to cancel");
			return;
		}
		
		steps.cancelFrame();
		sender.sendMessage(ChatColor.GOLD + "Cancelled frame creation");
	}
	
	@Command(
	name = "anim",
	syntax = "schedule <time>",
	perm = "dtl.anim.commands.schedule")
	public void scheduleAnimation(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( steps == null ) 
		{
			sender.sendMessage(ChatColor.GOLD + "Cannot use this for an empty animation");
			return;
		}
		
		try
		{
			steps.setSchedule(Integer.parseInt(args.get("time")));
			sender.sendMessage(ChatColor.GOLD + "Schedule time changed: " + ChatColor.DARK_AQUA + args.get("time"));
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
		if ( !(sender instanceof Player) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		}
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No session found!");
			return;
		}
		AnimCreationSteps steps = players.get(sender.getName());
		if ( steps == null )
		{
			sender.sendMessage(ChatColor.GOLD + "Cannot use this for an empty animation");
			return;
		}
		
		try
		{
			steps.setDistance(Integer.parseInt(args.get("dist")));
			sender.sendMessage(ChatColor.GOLD + "Distance changed: " + ChatColor.DARK_AQUA + args.get("time"));
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
		if ( !(sender instanceof Player) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		} 
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "No animation session found");
			return;
		}

		AnimCreationSteps steps = players.get(sender.getName());
		plugin.getLoader().addAnnimationYaml(steps.completeAnimation().toString(), steps.asYaml());
		//DtlAnimations.getInstance().getAnimationManager().addAnimation(steps.asAnimation());
		
		sender.sendMessage(ChatColor.GOLD + "Animation finished: " + ChatColor.DARK_AQUA + steps.completeAnimation().toString());
		players.remove(sender.getName());
	}

	@Command(
	name = "anim",
	syntax = "cancel",
	perm = "dtl.anim.commands.cancel")
	public void cancelAnimation(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		if ( !(sender instanceof Player) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "Only players can use this command");
			return;
		} 
		if ( !players.containsKey(sender.getName()) ) 
		{
			sender.sendMessage(ChatColor.GOLD + "First start or load an animaton before canceling it");
			return;
		}

		AnimCreationSteps steps = players.get(sender.getName());
		sender.sendMessage(ChatColor.GOLD + "Session cancelled: " + ChatColor.DARK_AQUA + steps.completeAnimation().toString());
		
		players.remove(sender.getName());
	}
	
	/*@Command(
	name = "anim",
	syntax = "preview {args}",
	perm = "dtl.anim.commands.preview")*/
	public void previewAnim(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
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
			sender.sendMessage(ChatColor.GOLD + "Trying to start the requested animation: " + ChatColor.DARK_AQUA + args.get("name"));
			plugin.getManager().addAnimation(anim);
		}
	}

	@Command(
	name = "anim",
	syntax = "stop <name>",
	perm = "dtl.anim.commands.stop")
	public void stopAnim(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{		
		AnimationSet anim = plugin.getLoader().getAnimation(args.get("name"));
		if ( anim == null || !plugin.getManager().isRunning(anim) )
		{
			sender.sendMessage(ChatColor.RED + "Falied to stop the requested animation: " + ChatColor.DARK_AQUA + args.get("name"));
		}
		else
		{
			sender.sendMessage(ChatColor.GOLD + "Trying to stop the requested animation: " + ChatColor.DARK_AQUA + args.get("name"));
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
		
		private YamlConfiguration yaml;

		public AnimCreationSteps(String name)
		{
			this(name, false);
		}
		public AnimCreationSteps(String name, boolean load)
		{
			this.name = name.replace(" ", "_");
			//set the animation name
			if ( !load )
			{
				yaml = new YamlConfiguration();
				anim = new AnimationSet(name);

				//save the basic animation yaml
				yaml.set(name + ".name", name);
				yaml.set(name + ".schedule", anim.getSchedule());
				yaml.set(name + ".distance", anim.getDistance());

				step = AREA_EDIT;
			}
			else
			{
				anim = DtlAnimations.getInstance().getLoader().getAnimation(name);

				yaml = new YamlConfiguration();
				yaml.set(name + ".name", name);
				yaml.set(name + ".schedule", anim.getSchedule());
				yaml.set(name + ".distance", anim.getDistance());
				
				Location loc = anim.getLocation();
				yaml.set(this.name + ".location.x", loc.getBlockX());
				yaml.set(this.name + ".location.y", loc.getBlockY());
				yaml.set(this.name + ".location.z", loc.getBlockZ());
				yaml.set(this.name + ".location.world", loc.getWorld().getName());
				
				yaml.set("name", DtlAnimations.getInstance().getLoader().getAnimationYaml(name));
				
				frame = anim.getFrames().get(0);
				step = FRAME_SETTING;
			}
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
		//	if ( yaml.contains(name + "frames." + frameCount) )
		//	    yaml.set(name + "frames." + frameCount + ".schedule", anim.getSchedule());
		}

		public void setSession(LocalSession localSession)
		{
			this.session = localSession;
		}

		public int frameCount()
		{
			return anim.getFrames().size();
		}
		
		public AnimationSet completeAnimation()
		{
			step = ANIMATION_FINISHED;
			return anim;
		}

		public AnimationSet incompleteAnimation()
		{
			return anim;
		}
		
		public boolean removeFrame(String name)
		{
			boolean found = false;
			Iterator<AnimationFrame> it = anim.getFrames().iterator();
			while(it.hasNext() && !found)
			{
				found = it.next().getName().equals(name);
			}
			if ( found ) 
			{
				it.remove();
			}
			return found;
		}
		
		public AnimationFrame showFrame(String name)
		{
			AnimationFrame found = null;
			Iterator<AnimationFrame> it = anim.getFrames().iterator();
			while(it.hasNext() && found == null)
			{
				found = (found = it.next()).getName().equals(name) ? found : null;
			}
			if ( found != null )
				found.copyToServer();
			return found;
		}

		public boolean editFrame(String name)
		{
			AnimationFrame frame = showFrame(name);
			if ( frame != null ) step = FRAME_SETTING;
			return (this.frame = frame) != null;
		}
		
		public void cancelFrame()
		{
			step = FRAME_SETTING;
		}
		
		public void nextFrame()
		{
			step = AREA_EDIT;
		}

		public void saveFrame(Player player, String name, int at, boolean replace)
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
				frame = new AnimationFrame(anim, name, name);
			//	Location loc = player.getLocation();
				frame.setLocation(anim.getLocation());
				
				//frame yaml saving
				//yaml.set(this.name + ".frames." + frameCount + ".file", name);
				
				/*yaml.set(this.name + ".frames." + frameCount + ".location.x", loc.getBlockX());
				yaml.set(this.name + ".frames." + frameCount + ".location.y", loc.getBlockY());
				yaml.set(this.name + ".frames." + frameCount + ".location.z", loc.getBlockZ());
				yaml.set(this.name + ".frames." + frameCount + ".location.world", localPlayer.getWorld().getName());*/

				//add the frame to the animation
				if ( replace )
					anim.addFrame(frame);
				else
					anim.addFrame(at, frame);
			}
			catch( IncompleteRegionException e )
			{
				player.sendMessage(ChatColor.RED + "Frame cuboid selection is invalid!");
			}
			catch( Exception e ) { e.printStackTrace(); } 
		}

		public YamlConfiguration asYaml()
		{
			int count = 0;
			for ( AnimationFrame frame : anim.getFrames() )
			{
				yaml.set(this.name + ".frames." + count++ + ".file", frame.getFile());
			}
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
			AREA_SELECT, AREA_EDIT, FRAME_EDIT, FRAME_SHOW, FRAME_SETTING, ANIMATION_FINISHED;
		}
	}

}
