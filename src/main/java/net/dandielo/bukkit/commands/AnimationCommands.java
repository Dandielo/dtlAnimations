package net.dandielo.bukkit.commands;

import java.util.Map;

import net.dandielo.bukkit.DtlAnimations;
import net.dandielo.commands.Command;

import org.bukkit.command.CommandSender;

public class AnimationCommands {
	
	@Command(
	name = "anim",
	syntax = "create <name>",
	perm = "dtl.anim.commands.create")
	public void createAnimation(DtlAnimations plugin, CommandSender sender, Map<String, String> args)
	{
		
	}
	
}
