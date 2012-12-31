package net.dtl.dandielo.animation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dtl.dandielo.bukkit.DtlAnimations;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AnimationManager {

	public static Utils utils = new Utils();

	private Map<AnimationSet, List<Player>> players = Collections.synchronizedMap(new HashMap<AnimationSet, List<Player>>());
	private Map<AnimationSet, Integer> animations = Collections.synchronizedMap(new HashMap<AnimationSet, Integer>());
	private static int runningAnimations = 0;

	private DtlAnimations plugin = DtlAnimations.getInstance();
	private boolean stop = false;

	public void addAnimation(AnimationSet animation)
	{
		animations.put(animation, plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new Update(animation), animation.getShedule()));
		players.put(animation, Collections.synchronizedList(new LinkedList<Player>()) );

		System.out.print("Curently running annimations: " + ++runningAnimations);
	}

	public void removeAnimation(AnimationSet animation) {
		// Hopefully this covers everything involved in stopping an animation...
		// Maybe some kind of 'reset' procedure if in the middle of an animation?
		if (animations.containsKey(animation)) {
			plugin.getServer().getScheduler().cancelTask(animations.get(animation));
			animations.remove(animation);
			players.remove(animation);
			runningAnimations--;
		}
	}

	private void scheduleNextUpdate(AnimationSet animation, AnimationFrame frame) {
		plugin.getServer().getScheduler().cancelTask(animations.get(animation));
		animations.put(animation, plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new Update(animation), frame.getShedule()) );
	}

	public boolean addPlayer(AnimationSet animation, Player player)
	{
		List<Player> players = this.players.get(animation);

		boolean has = false;
		ListIterator<Player> it = (ListIterator<Player>) players.iterator();
		while(it.hasNext() && !has)
		{
			if ( it.next().getName().equals(player.getName()) )
				has = true;//it.remove();
		}
		if ( !has )
		{
			it.add(player);
			return true;
		}

		/*if ( players == null )
		{
			players = Collections.synchronizedList(new LinkedList<Player>());
			this.players.put(animation, players);
		}
		if ( !players.contains(player) )
		{
			players.add(player);
			return true;
		}*/
		return false;

	}

	public List<AnimationSet> getNearAnimations(Player player)
	{
		List<AnimationSet> ret = new ArrayList<AnimationSet>();
		for ( AnimationSet animation : animations.keySet() )
			if ( player.getLocation().distance(animation.getLocation()) < animation.getDistance() )
				ret.add(animation);
		return ret;
	}

	public boolean checkDistance(AnimationSet animation, Player player)
	{
		return player.getLocation().distance(animation.getLocation()) < animation.getDistance();
	}

	public class Update implements Runnable
	{

		private AnimationSet animation;

		public Update(AnimationSet animation) {	
			this.animation = animation;

		}

		@Override
		public void run() {
			if (!stop)
			{	
				AnimationFrame frame = animation.getFrame();

				List<Player> pl = players.get(animation);

				ListIterator<Player> it = (ListIterator<Player>) pl.iterator();
				while(it.hasNext())
				{
					Player p = it.next();
					if ( checkDistance(animation, p) )
						frame.sendTo(p);
					else
						it.remove();
				}
				/*		for ( Player p : pl )
				{
					if ( checkDistance(animation, p) )
						frame.sendTo(p);
					else
						pl.remove(p);
				}*/

				animation.nextFrame();
				scheduleNextUpdate(animation, frame);
			}
		}
	}

	public static class Utils
	{
		private Pattern pattern;
		private char decimalSeparator;

		public Utils()
		{
			//get the separator
			decimalSeparator = ((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator();
			pattern = Pattern.compile("([\\d]+[^" + decimalSeparator + "|:]{0,1}[\\d]*)");

		}

		public Location makeLocation(String locStr)
		{
			if ( locStr == null )
				return null;

			Location loc = null;

			int i = 0;
			double val[] = new double[3];
			String world = locStr.split(":")[1];

			Matcher matcher = pattern.matcher(locStr);


			while(matcher.find())
			{
				val[i] = Double.parseDouble(matcher.group());
				++i;
			}

			loc = new Location(DtlAnimations.getInstance().getServer().getWorld(world), val[0], val[1], val[2]);

			return loc;
		}
	}

}
