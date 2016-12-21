package oreexcavation.handlers;

import java.io.File;
import java.util.Arrays;
import net.minecraftforge.common.config.Configuration;
import oreexcavation.core.ExcavationSettings;
import oreexcavation.core.OreExcavation;
import oreexcavation.overrides.ToolOverrideHandler;
import oreexcavation.shapes.ShapeRegistry;
import oreexcavation.utils.JsonHelper;
import org.apache.logging.log4j.Level;
import com.google.gson.JsonObject;

public class ConfigHandler
{
	public static Configuration config;
	
	public static void initConfigs()
	{
		if(config == null)
		{
			OreExcavation.logger.log(Level.ERROR, "Config attempted to be loaded before it was initialised!");
			return;
		}
		
		config.load();
		
		ExcavationSettings.hideUpdates = config.getBoolean("Hide Updates", Configuration.CATEGORY_GENERAL, false, "Hides update notifications");
		ExcavationSettings.mineLimit = config.getInt("Limit", Configuration.CATEGORY_GENERAL, 128, 1, Integer.MAX_VALUE, "The maximum number of blocks that can be excavated at once");
		ExcavationSettings.mineSpeed = config.getInt("Speed", Configuration.CATEGORY_GENERAL, 64, 1, Integer.MAX_VALUE, "How many blocks per tick can be excavated");
		ExcavationSettings.mineRange = config.getInt("Range", Configuration.CATEGORY_GENERAL, 16, 1, Integer.MAX_VALUE, "How far from the origin an excavation can travel");
		ExcavationSettings.exaustion = config.getFloat("Exaustion", Configuration.CATEGORY_GENERAL, 0.1F, 0F, Float.MAX_VALUE, "Amount of exaustion per block excavated");
		ExcavationSettings.experience = config.getInt("Experience", Configuration.CATEGORY_GENERAL, 0, 0, Integer.MAX_VALUE, "Experience cost per block excavated");
		ExcavationSettings.openHand = config.getBoolean("Open Hand", Configuration.CATEGORY_GENERAL, true, "Allow excavation with an open hand");
		ExcavationSettings.mustHold = config.getBoolean("Must Hold", Configuration.CATEGORY_GENERAL, true, "Allows players to cancel excavation by releasing the keys");
		ExcavationSettings.invertTBlacklist = config.getBoolean("Invert Tool Blacklist", Configuration.CATEGORY_GENERAL, false, "Inverts the tool blacklist to function as a whitelist");
		ExcavationSettings.invertBBlacklist = config.getBoolean("Invert Block Blacklist", Configuration.CATEGORY_GENERAL, false, "Inverts the block blacklist to function as a whitelist");
		ExcavationSettings.ignoreTools = config.getBoolean("Ignore Tool", Configuration.CATEGORY_GENERAL, false, "Ignores whether or not the held tool is valid");
		ExcavationSettings.altTools = config.getBoolean("Alt Tools", Configuration.CATEGORY_GENERAL, false, "Use alternate check for tool validity (e.g. swords on webs)");
		ExcavationSettings.toolClass = config.getBoolean("Only Standard Types", Configuration.CATEGORY_GENERAL, false, "Limit excavation to standard tool types (Picks, Shoves, Axes & Shears)");
		ExcavationSettings.mineMode = config.getInt("Mode", Configuration.CATEGORY_GENERAL, 0, -1, 2, "Excavation mode (-1 Disabled, 0 = Keybind, 1 = Sneak, 2 = Always)");
		ExcavationSettings.tpsGuard = config.getBoolean("TPS Guard", Configuration.CATEGORY_GENERAL, true, "Temporarily reduces excavation speed if TPS begins to slow down");
		ExcavationSettings.autoPickup = config.getBoolean("Auto Pickup", Configuration.CATEGORY_GENERAL, false, "Skips spawning drops in world adding them directly to your inventory");
		ExcavationSettings.allowShapes = config.getBoolean("Allow Shapes", Configuration.CATEGORY_GENERAL, true, "Allow players to use shape mining");
		ExcavationSettings.maxUndos = config.getInt("Max Undos", Configuration.CATEGORY_GENERAL, 3, 0, Integer.MAX_VALUE, "How many excavations should be kept in undo history (may lead to exploits)");
		
		String [] tbl = config.getStringList("Tool Blacklist", Configuration.CATEGORY_GENERAL, new String[0], "Tools blacklisted from excavating");
		String [] bbl = config.getStringList("Block Blacklist", Configuration.CATEGORY_GENERAL, new String[0], "Blocks blacklisted from being excavated");
		
		ExcavationSettings.toolBlacklist.clear();
		ExcavationSettings.toolBlacklist.addAll(Arrays.asList(tbl));
		
		ExcavationSettings.blockBlacklist.clear();
		ExcavationSettings.blockBlacklist.addAll(Arrays.asList(bbl));
		
		config.save();
		
		File fileOverrides = new File("config/oreexcavation_overrides.json");
		
		if(fileOverrides.exists())
		{
			ToolOverrideHandler.INSTANCE.loadOverrides(JsonHelper.ReadFromFile(fileOverrides));
		} else
		{
			JsonObject json = ToolOverrideHandler.INSTANCE.getDefaultOverrides();
			JsonHelper.WriteToFile(fileOverrides, json);
			ToolOverrideHandler.INSTANCE.loadOverrides(json);
		}
		
		ShapeRegistry.INSTANCE.loadShapes(new File("config/oreexcavation_shapes.json"));
	}
}
