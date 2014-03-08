package net.dandielo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.dandielo.animation.AnimationManager;
import net.dandielo.animation.AnimationSet;
import net.dandielo.bukkit.DtlAnimations;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class AnimationLoader {

	private static AnimationManager manager = DtlAnimations.getInstance().getManager();
	
	private final static char PATH_SEPARATOR = '.';
	protected boolean separateFiles;

	protected FileConfiguration animations;
	protected File animationsFile;	
	
	public AnimationLoader(ConfigurationSection config)
	{
//		ConfigurationSection config = DtlAnimations.getInstance().getConfig();

		
		String animationsFilename = config.getString("file");

		// Default settings
		if ( animationsFilename == null ) 
		{
			animationsFilename = "animations.yml";
			config.set("file", "animations.yml");
		}

		String baseDir = config.getString("basedir", "plugins/dtlAnimations" );// "plugins/PermissionsEx");

		if ( baseDir.contains("\\") && !"\\".equals(File.separator) ) 
		{
			baseDir = baseDir.replace("\\", File.separator);
		}


		File baseDirectory = new File(baseDir);
		if ( !baseDirectory.exists() ) 
		{
			baseDirectory.mkdirs();
		}

		this.animationsFile = new File(baseDir, animationsFilename);


		this.reload();

		
		if ( !animationsFile.exists() )
		{
			try 
			{
				animationsFile.createNewFile();
				
				this.save();
			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
	}
	
	public void reload() {
		animations = new YamlConfiguration();
		animations.options().pathSeparator(PATH_SEPARATOR);
		
		//remove each annimation so it can be reloaded
		manager.removeAllAnimations();
				
		try 
		{
			animations.load(animationsFile);

			for ( String key : animations.getKeys(false) )
			{
				ConfigurationSection animation = animations.getConfigurationSection(buildPath(key));
				
				AnimationSet anim = new AnimationSet(animation);
				anim.setRepeats(-2);
				manager.addAnimation(anim);
			}			
			
		} 
		catch (FileNotFoundException e)
		{
		//	severe(e.getMessage());
		} 
		catch (Throwable e)
		{
			throw new IllegalStateException("Error loading animations file", e);
		}
	}
	
	public AnimationSet getAnimation(String name)
	{
		AnimationSet anim = new AnimationSet(animations.getConfigurationSection(buildPath(name)));
		anim.setRepeats(-2);
		return anim;
	}
	public ConfigurationSection getAnimationYaml(String name)
	{
		return animations.getConfigurationSection(name);
	}
	
	public void removeAnimationYaml(String name)
	{
		this.animations.set(name, null);
		save();
	}
	public void addAnnimationYaml(String name, YamlConfiguration anim)
	{
		this.animations.set(name, anim.getConfigurationSection(name));
		save();
	}

	public void save() {
		try 
		{
			this.animations.save(animationsFile);
		} 
		catch (IOException e) 
		{
		}
	}
	
	public static String buildPath(String... path) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		char separator = PATH_SEPARATOR; //permissions.options().pathSeparator();

		for ( String node : path ) 
		{
			if ( !first ) 
			{
				builder.append(separator);
			}

			builder.append(node);

			first = false;
		}

		return builder.toString();
	}
	
	
}
