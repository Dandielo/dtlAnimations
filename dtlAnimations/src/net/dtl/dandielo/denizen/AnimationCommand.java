package net.dtl.dandielo.denizen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.runnables.Runnable1;
import net.dtl.dandielo.animation.AnimationSet;
import net.dtl.dandielo.bukkit.DtlAnimations;

/**
 * <p>Sends a block animation sequence using .schematic files and an 'ANIMATION script'</p>
 * 
 * 
 * <br><b>dScript Usage:</b><br>
 * <pre>ANIMATION ({START}|STOP) [SCRIPT:animation_script] (DURATION:#)</pre>
 * 
 * <ol><tt>Arguments: [] - Required, () - Optional, {} - Default</ol></tt>
 * 
 * <ol><tt>[START|STOP]</tt><br> 
 *         The AnimationAction. Starts or stops the specified 'ANIMATION script'.</ol>
 * 
 * <ol><tt>[SCRIPT:animation_script]</tt><br> 
 *         The animation script to use. See below for format.</ol>
 * 
 * <ol><tt>[DURATION:#]</tt><br> 
 *         The duration of the animation, in seconds. To be used with 'START'. After
 *         the duration, this animation will automatically 'STOP'.</ol>
 * 
 * <p>
 * Note: Animations used with Denizen go into /plugins/Denizen/scripts/, along with your
 * other scripts. To use an animation in the root dtlAnimation folder, use EXECUTE commands. 
 * </p>
 * 
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - ANIMATION START SCRIPT:pendulum_swing<br>
 *  - ANIMATION START SCRIPT:windmill_spin DURATION:360<br>
 * </ol></tt>
 * 
 * <br><b>Sample Animation Script Format:</b><br>
 * <ol><pre>
 * 'Windmill Animation':
 *   name: Windmill
 *   schedule: 40
 *   distance: 60
 *   location: 0.0,80.0,0.0:world
 *   frames:
 *     1:
 *       file: frame1
 *     2:
 *       file: frame2
 *     3:
 *       file: frame3
 *     4:
 *       file: frame4 
 * </pre></ol>
 *
 * <p>
 * Note: See dtlAnimations documentation for more information. 
 * </p>
 * 
 * @author Dandielo (dtlAnimation), aufdemrand (this Command)
 *
 */

public class AnimationCommand extends AbstractCommand {

	private enum AnimationAction { START, STOP }
	private DtlAnimations animator;

	public AnimationCommand() {
		this.activate().as("ANIMATION").withOptions("({START}|STOP) [SCRIPT:animation_script]", 1);
	}

	@Override
	public void onEnable() {
		animator = (DtlAnimations) Bukkit.getPluginManager().getPlugin("DtlAnimations");
	}

	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {

		// Initialize fields used
		String script = null;
		int duration = -1;
		// Make default action START
		AnimationAction action = AnimationAction.START;

		// Iterate through arguments
		for (String arg : scriptEntry.getArguments()) {

			// matchesScript will ensure there is an actual script with this name loaded
			if (aH.matchesScript(arg)) {
				// All script names for denizen are upper-case to avoid case sensitivity
				script = aH.getStringFrom(arg).toUpperCase();
				dB.echoDebug("...set SCRIPT: '%s'", script);
				continue;

				// mathesDuration will make sure the argument is a positive integer
			} else if (aH.matchesDouble(null)) {
				duration = aH.getIntegerFrom(arg);
				dB.echoDebug("...set DURATION: '%s'", String.valueOf(duration));
				continue;

				// matches the same values as the AnimationAction enum
			} else if (aH.matchesArg("START, STOP", arg)) {
				action = AnimationAction.valueOf(aH.getStringFrom(arg).toUpperCase());
				dB.echoDebug("...set AnimationAction: '%s'", action.toString());
				continue;

				// Unknown argument should be caught to avoid unwanted behavior.
			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);

		}

		// Check for null fields from 'required' arguments
		if (script == null) 
			throw new InvalidArgumentsException("Must specify a valid 'Animation SCRIPT'.");

		// Stash objects in scriptEntry for use in execute()
		scriptEntry.addObject("duration", duration);
		scriptEntry.addObject("action", action);
		scriptEntry.addObject("script", script);
		scriptEntry.addObject("duration", duration);
	}


	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {

		// Grab objects needed from scriptEntry
		String script = (String) scriptEntry.getObject("script");

		// Execute!
		switch ((AnimationAction) scriptEntry.getObject("action")) {

		case START:
			startAnimation(script);
			break;

		case STOP:
			stopAnimation(script);
			break;
		}

		// Handle duration, if set
		int duration = (Integer) scriptEntry.getObject("duration");
		if (duration > 0 && (AnimationAction) scriptEntry.getObject("action") == AnimationAction.START) {

			// If this script already has a duration, stop the task so a new one can be made
			if (durations.containsKey(script))
				denizen.getServer().getScheduler().cancelTask(durations.get(script));

			dB.echoDebug(Messages.DEBUG_SETTING_DELAYED_TASK, "Stop ANIMATION '" + script + "'");
			// Add this delayed task to the duration map
			durations.put(script, 
					denizen.getServer().getScheduler().scheduleSyncDelayedTask(denizen, 
							new Runnable1<String>(script) {

						@Override
						public void run(String script) {
							try {
								dB.log(Messages.DEBUG_RUNNING_DELAYED_TASK, "Stop ANIMATION '" + script + "'");
								stopAnimation(script);
							} catch (Exception e) {
								dB.echoError("Unable to stop Animation '%s'. Perhaps it's already stopped?", script);
							}
						}

					}, duration * 20));
		}
	}

	// For keeping track of animations with durations
	private Map<String, Integer> durations = new ConcurrentHashMap<String, Integer>();

	// For keeping track of animations by name
	private Map<String, AnimationSet> animations = new ConcurrentHashMap<String, AnimationSet>();

	// Calls animationmanager to add an AnimationSet
	private void startAnimation(String script) {
		if (animations.containsKey(script)) {
			dB.echoDebug("Animation '%s' is already running.", script);
		} else {
			animations.put(script, new AnimationSet(denizen.getScripts().getConfigurationSection(script)));
			animator.getAnimationManager().addAnimation(animations.get(script));
		}
	}

	// Calls animationmanager to remove an AnimationSet
	private void stopAnimation(String script) {
		if (animations.containsKey(script)) {
			dB.echoDebug("Stopping animation '%s'.", script);
			animator.getAnimationManager().removeAnimation(animations.get(script));
			animations.remove(script);
		} else {
			dB.echoDebug("Animation '%s' is not running.", script);		
		}

	}

}
