package net.dandielo.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Executor implements CommandExecutor {
	public static CommandManager cManager;
	
	public Executor(CommandManager manager)
	{
		cManager = manager;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		return cManager.execute(name, args, sender);
	}
}
