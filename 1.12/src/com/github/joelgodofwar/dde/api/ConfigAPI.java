package com.github.joelgodofwar.dde.api;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.github.joelgodofwar.dde.DragonDropElytra;

public class ConfigAPI  {

	public final static Logger logger = Logger.getLogger("Minecraft");
	
	public static  void CheckForConfig(Plugin plugin){
		try{
			if(!plugin.getDataFolder().exists()){
				log(": Data Folder doesn't exist");
				log(": Creating Data Folder");
				plugin.getDataFolder().mkdirs();
				log(": Data Folder Created at " + plugin.getDataFolder());
			}
			File  file = new File(plugin.getDataFolder(), "config.yml");
			plugin.getLogger().info("" + file);
			if(!file.exists()){
				log("config.yml not found, creating!");
				plugin.saveDefaultConfig();
				FileConfiguration config = plugin.getConfig();
				
				config.options().copyDefaults(true);
				plugin.saveConfig();
			}
			}catch(Exception e){
				e.printStackTrace();
			}
	}
	
	public static void Reloadconfig(Plugin plugin){
		// Load config.
		FileConfiguration config = plugin.getConfig();
		String daString = config.getString("debug").replace("'", "") + ",";
		
		if(daString.contains("true")){
			DragonDropElytra.debug = true;
		}else{
			DragonDropElytra.debug = false;
		}
		String daString2 = config.getString("auto-update-check").replace("'", "") + ",";
		if(daString2.contains("true")){
			DragonDropElytra.UpdateCheck = true;
		}else{
			DragonDropElytra.UpdateCheck = false;
		}
		
		if(DragonDropElytra.debug){log("UpdateCheck = " + DragonDropElytra.UpdateCheck);} //TODO: Logger
	}
	
	public static String GetConfigStr(Plugin plugin, String string){
		FileConfiguration config = plugin.getConfig();
		String daString = config.getString(string).replace("'", "") + ",";
		return daString;
		
	}
	
	public static boolean GetConfigBool(Plugin plugin, String string){
		FileConfiguration config = plugin.getConfig();
		String daString = config.getString(string).replace("'", "") + ",";
		if(daString.contains("true")){
			return true;
		}else{
			return false;
		}
	}
	
	public static  void log(String dalog){
		Bukkit.getLogger().info(dalog);
	}
}
