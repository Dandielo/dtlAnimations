package net.dandielo;

import static net.dandielo.bukkit.DtlAnimations.info;

import java.util.List;

import net.dandielo.animation.AnimationManager;
import net.dandielo.animation.AnimationSet;
import net.dandielo.bukkit.DtlAnimations;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	private AnimationManager manager = DtlAnimations.getInstance().getAnimationManager();
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		List<AnimationSet> animations = manager.getNearAnimations(event.getPlayer());
		
		for ( AnimationSet animation : animations )
		{
			manager.removePlayer(animation, event.getPlayer());//.removeAnimation(animation);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		List<AnimationSet> animations = manager.getNearAnimations(event.getPlayer());
		
		for ( AnimationSet animation : animations )
		{
			if ( manager.addPlayer(animation, event.getPlayer()) );
			//	info(ChatColor.RED + event.getPlayer().getName() + " has startet reciving animation packets for animation: " + ChatColor.GOLD + animation);
			
		}
	}
	
}
