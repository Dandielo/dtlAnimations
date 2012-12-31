package net.dtl.dandielo;

import static net.dtl.dandielo.bukkit.DtlAnimations.info;

import java.util.List;

import net.dtl.dandielo.animation.AnimationManager;
import net.dtl.dandielo.animation.AnimationSet;
import net.dtl.dandielo.bukkit.DtlAnimations;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerListener implements Listener {

	private AnimationManager manager = DtlAnimations.getInstance().getAnimationManager();
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		List<AnimationSet> animations = manager.getNearAnimations(event.getPlayer());
		
		for ( AnimationSet animation : animations )
		{
			if ( manager.addPlayer(animation, event.getPlayer()) )
				info(ChatColor.RED + event.getPlayer().getName() + " has startet reciving animation packets for animation: " + ChatColor.GOLD + animation);
			
		}
	}
	
}
