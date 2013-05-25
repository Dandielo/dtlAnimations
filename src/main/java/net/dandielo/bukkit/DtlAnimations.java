package net.dandielo.bukkit;

import java.util.logging.Logger;

import net.dandielo.FrameLoader;
import net.dandielo.PacketsManager;
import net.dandielo.PlayerListener;
import net.dandielo.animation.AnimationManager;
import net.dandielo.denizen.AnimationCommand;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class DtlAnimations extends JavaPlugin {
	protected final static Logger logger = Logger.getLogger("Minecraft");
	protected static CommandSender sender;
	
	private static DtlAnimations instance;
	private AnimationManager animationManager;
	private PacketsManager packetsManager;
	protected FrameLoader loader;
	
	@Override
	public void onEnable()
	{
		instance = this;

		//loading sender
		sender = Bukkit.getServer().getConsoleSender();
		
		this.saveDefaultConfig();

		packetsManager = new PacketsManager();
		animationManager = new AnimationManager();
		loader = new FrameLoader(getConfig());
		
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		
		if (getServer().getPluginManager().getPlugin("Denizen") != null)
			new AnimationCommand();
	}
	
	public AnimationManager getAnimationManager()
	{
		return animationManager;
	}
	
	//Plugin static methods
	public static DtlAnimations getInstance()
	{
		return instance;
	}
	
	public PacketsManager getPacketsManager()
	{
		return packetsManager;
	}
	
	//logger info
	public static void info(String message)
	{
		sender.sendMessage("["+getInstance().getDescription().getName()+"] " + message);
	}
	//logger warning
	public static void warning(String message)
	{
		logger.warning("["+getInstance().getDescription().getName()+"] " + message);
	}
	//logger severe
	public static void severe(String message)
	{
		logger.severe("["+getInstance().getDescription().getName()+"] " + message);
	}
	
}
