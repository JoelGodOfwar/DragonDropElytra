package com.github.joelgodofwar.dde;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.joelgodofwar.dde.i18n.Translator;
import com.github.joelgodofwar.dde.util.Ansi;
import com.github.joelgodofwar.dde.util.ConfigAPI;
import com.github.joelgodofwar.dde.util.Metrics;
import com.github.joelgodofwar.dde.util.StrUtils;
import com.github.joelgodofwar.dde.util.Utils;
import com.github.joelgodofwar.dde.util.VersionChecker;
import com.github.joelgodofwar.dde.util.YmlConfiguration;

public class DragonDropElytra  extends JavaPlugin implements Listener{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	public final static Logger logger = Logger.getLogger("Minecraft");
	static String THIS_NAME;
	static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 71235; // https://spigotmc.org/resources/71236
	public String githubURL = "https://raw.githubusercontent.com/JoelGodOfwar/DragonDropElytra/master/versions/1.13/versions.xml";
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public static boolean UpdateCheck;
    public String DownloadLink = "https://www.spigotmc.org/resources/dragondropelytra2.71235";
	/** end update checker variables */
	public static boolean debug;
	public static String daLang;
    YmlConfiguration config = new YmlConfiguration();
    YamlConfiguration oldconfig = new YamlConfiguration();
	String world_whitelist;
	String world_blacklist;
	public int killdragonegg = 0;
	String configVersion = "1.0.12";
	String pluginName = THIS_NAME;
	Translator lang2;
	private Set<String> triggeredPlayers = new HashSet<>();
	
	@Override // 
	public void onEnable(){ //TODO: onEnable
		long startTime = System.currentTimeMillis();
		UpdateCheck = getConfig().getBoolean("auto_update_check", true);
		debug = getConfig().getBoolean("debug", false);
		daLang = getConfig().getString("lang", "en_US");
		lang2 = new Translator(daLang, getDataFolder().toString());
		THIS_NAME = this.getDescription().getName();
		THIS_VERSION = this.getDescription().getVersion();
		if(!getConfig().getBoolean("longpluginname", true)) {
			pluginName = "DDE";
		}else {
			pluginName = THIS_NAME;
		}
		
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + THIS_NAME + " v" + THIS_VERSION + Ansi.RESET + " Loading...");
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			logDebug("Jar file contains -DEV, debug set to true");
			//log("jarfile contains dev, debug set to true.");
		}
		//log("jarfile=" + jarfile.toString());
		//log("Version: " + getVersion());
		String mcVersion = getVersion();
		logger.info(Ansi.RED + "VERSION=" + mcVersion + Ansi.RESET);
		// 1.17.1
		// 0.1..2
		String[] vers = mcVersion.split("\\.");
		//logger.info(Ansi.RED + "vers length=" + vers.length + " vers toString=" + vers.toString() + Ansi.RESET);
		int minor = Integer.parseInt(vers[1]);
		if(!(minor >= 13)) {
			logger.info(Ansi.RED + "WARNING!" + Ansi.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + Ansi.RESET);
			logger.info(Ansi.RED + "WARNING! " + Ansi.YELLOW + get("dde.message.server_not_version") + Ansi.RESET);
			logger.info(Ansi.RED + "WARNING! " + Ansi.YELLOW + THIS_NAME + " v" + THIS_VERSION + " disabling." + Ansi.RESET);
			logger.info(Ansi.RED + "WARNING!" + Ansi.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + Ansi.RESET);
			Bukkit.getPluginManager().disablePlugin(this); 
			return;
		}
		
		/**  Check for config */
		try{
			if(!getDataFolder().exists()){
				log("Data Folder doesn't exist");
				log("Creating Data Folder");
				getDataFolder().mkdirs();
				log("Data Folder Created at " + getDataFolder());
			}
			File  file = new File(getDataFolder(), "config.yml");
			log("" + file);
			if(!file.exists()){
				log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		/** end config check */
		/** Check if config.yml is up to date.*/
		boolean needConfigUpdate = false;
		try {
			oldconfig.load(new File(getDataFolder() + "" + File.separatorChar + "config.yml"));
		} catch (Exception e2) {
			logWarn("Could not load config.yml");
			e2.printStackTrace();
		}
		String checkconfigversion = oldconfig.getString("version", "1.0.0");
		if(checkconfigversion != null){
			if(!checkconfigversion.equalsIgnoreCase(configVersion)){
				needConfigUpdate = true;
			}
		}
		if(needConfigUpdate){
			try {
				copyFile_Java7(getDataFolder() + "" + File.separatorChar + "config.yml",getDataFolder() + "" + File.separatorChar + "old_config.yml");
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				oldconfig.load(new File(getDataFolder(), "config.yml"));
			} catch (IOException | InvalidConfigurationException e2) {
				logWarn("Could not load config.yml");
				e2.printStackTrace();
			}
			saveResource("config.yml", true);
			try {
				config.load(new File(getDataFolder(), "config.yml"));
			} catch (IOException | InvalidConfigurationException e1) {
				logWarn("Could not load config.yml");
				e1.printStackTrace();
			}
			try {
				oldconfig.load(new File(getDataFolder(), "old_config.yml"));
			} catch (IOException | InvalidConfigurationException e1) {
				e1.printStackTrace();
			}
			config.set("auto_update_check", oldconfig.get("auto_update_check", true));
			config.set("debug", oldconfig.get("debug", false));
			config.set("lang", oldconfig.get("lang", "en_US"));
			config.set("longpluginname", oldconfig.get("longpluginname", true));
			
			config.set("world.whitelist", oldconfig.get("world.whitelist", ""));
			config.set("world.blacklist", oldconfig.get("world.blacklist", ""));
			
			config.set("droploc", oldconfig.get("droploc", "0, 62, 4"));
			config.set("do_what.drop_on_ground", oldconfig.get("do_what.drop_on_ground", false));
			config.set("do_what.drop_naturally", oldconfig.get("do_what.drop_naturally", false));
			config.set("do_what.place_in_chest", oldconfig.get("do_what.place_in_chest", false));
			config.set("do_what.give_to_player", oldconfig.get("do_what.give_to_player", true));
			config.set("do_what.prevent_dragon_egg_spawn", oldconfig.get("do_what.prevent_dragon_egg_spawn", false));
			config.set("do_what.prevent_dragon_egg_spawn_delay_init", oldconfig.get("do_what.prevent_dragon_egg_spawn_delay_init", 5));
			config.set("do_what.prevent_dragon_egg_spawn_delay_run", oldconfig.get("do_what.prevent_dragon_egg_spawn_delay_run", 5));
			config.set("do_what.send_console_command", oldconfig.get("do_what.send_console_command", false));
			config.set("do_what.console_command", oldconfig.get("do_what.console_command", "say The EnderDragon has been killed!"));
			config.set("drop.elytra", oldconfig.get("drop.elytra", true));
			config.set("drop.brokenelytra", oldconfig.get("drop.brokenelytra", false));
			config.set("drop.dragonhead", oldconfig.get("drop.dragonhead", false));
			config.set("drop.dragonegg", oldconfig.get("drop.dragonegg", false));
			config.set("chance.elytra.chancepercentdrop", oldconfig.get("chance.elytra.randomdrop", false));
			config.set("chance.elytra.chancepercent", oldconfig.get("chance.elytra.chancepercent", "0.25"));
			config.set("chance.dragonhead.chancepercentdrop", oldconfig.get("chance.dragonhead.randomdrop", false));
			config.set("chance.dragonhead.chancepercent", oldconfig.get("chance.dragonhead.chancepercent", "0.25"));
			config.set("chance.dragonegg.chancepercentdrop", oldconfig.get("chance.dragonegg.randomdrop", false));
			config.set("chance.dragonegg.chancepercent", oldconfig.get("chance.dragonegg.chancepercent", "0.25"));
			try {
				config.save(new File(getDataFolder(), "config.yml"));
			} catch (IOException e) {
				logWarn("Could not save old settings to config.yml");
				e.printStackTrace();
			}
			log("config.yml has been updated");
		}else{
			//log("" + "not found");
		}
		world_whitelist = getConfig().getString("world.whitelist", "");
		world_blacklist = getConfig().getString("world.blacklist", "");
		
		/** Update Checker */
		if(UpdateCheck){
			/** auto_update_check is true */
			try {
				Bukkit.getConsoleSender().sendMessage("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					/** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();
					
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					log("* " + get("dde.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
					log("* " + get("dde.version.old_vers") + ChatColor.RED + UColdVers );
					log("* " + get("dde.version.new_vers") + ChatColor.GREEN + UCnewVers );
					log("*");
					log("* " + get("dde.version.please_update") );
					log("*");
					log("* " + get("dde.version.download") + ": " + DownloadLink + "/history");
					log("* " + get("dde.version.donate") + ": https://ko-fi.com/joelgodofwar");
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				}else{
					/** Up to date */
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					log("* " + get("dde.version.curvers"));
					log("* " + get("dde.version.donate") + ": https://ko-fi.com/joelgodofwar");
					log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			}catch(Exception e) {
				/** Error */
				log(get("dde.version.update.error"));
				e.printStackTrace();
			}
		}else {
			/** auto_update_check is false so nag. */
			log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			log("* " + get("dde.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		/** end update checker */
		
		getServer().getPluginManager().registerEvents(this, this);
		consoleInfo(Ansi.BOLD + "ENABLED" + Ansi.RESET + " - Loading took " + LoadTime(startTime));
		
		if(debug){//&&!(jarfile.toString().contains("-DEV"))
			logDebug("server version=" + Bukkit.getServer().getVersion().toString());
			logDebug("***----====[ Config.yml dump ]====----***");
			logDebug("version=" + getConfig().getBoolean("version"));
			logDebug("auto_update_check=" + getConfig().getBoolean("auto_update_check"));
			logDebug("debug=" + getConfig().getBoolean("debug"));
			logDebug("lang=" + getConfig().getString("lang"));
			logDebug("longpluginname=" + getConfig().getBoolean("longpluginname"));
			logDebug("world.whitelist=" + getConfig().getString("world.whitelist"));
			logDebug("world.blacklist=" + getConfig().getString("world.blacklist"));
			logDebug("droploc=" + getConfig().getString("droploc"));
			logDebug("do_what.drop_on_ground=" + getConfig().getBoolean("do_what.drop_on_ground"));
			logDebug("do_what.drop_naturally=" + getConfig().getBoolean("do_what.drop_naturally"));
			logDebug("do_what.place_in_chest=" + getConfig().getBoolean("do_what.place_in_chest"));
			logDebug("do_what.give_to_player=" + getConfig().getBoolean("do_what.give_to_player"));
			logDebug("do_what.prevent_dragon_egg_spawn=" + getConfig().getBoolean("do_what.prevent_dragon_egg_spawn"));
			logDebug("do_what.prevent_dragon_egg_spawn_delay_init=" + getConfig().getDouble("do_what.prevent_dragon_egg_spawn_delay_init"));
			logDebug("do_what.prevent_dragon_egg_spawn_delay_run=" + getConfig().getDouble("do_what.prevent_dragon_egg_spawn_delay_run"));
			logDebug("do_what.send_console_command=" + getConfig().getBoolean("do_what.send_console_command"));
			logDebug("do_what.console_command=" + getConfig().getString("do_what.console_command"));
			logDebug("drop.elytra=" + getConfig().getBoolean("drop.elytra"));
			logDebug("drop.brokenelytra=" + getConfig().getBoolean("drop.brokenelytra"));
			logDebug("drop.dragonhead=" + getConfig().getBoolean("drop.dragonhead"));
			logDebug("drop.dragonegg=" + getConfig().getBoolean("drop.dragonegg"));
			logDebug("chance.elytra.chancepercentdrop=" + getConfig().getBoolean("chance.elytra.chancepercentdrop"));
			logDebug("chance.elytra.chancepercent=" + getConfig().getDouble("chance.elytra.chancepercent"));
			logDebug("chance.dragonhead.chancepercentdrop=" + getConfig().getBoolean("chance.dragonhead.chancepercentdrop"));
			logDebug("chance.dragonhead.chancepercent=" + getConfig().getDouble("chance.dragonhead.chancepercent"));
			logDebug("chance.dragonegg.chancepercentdrop=" + getConfig().getBoolean("chance.dragonegg.chancepercentdrop"));
			logDebug("chance.dragonegg.chancepercent=" + getConfig().getDouble("chance.dragonegg.chancepercent"));
			logDebug("***----====[ Config.yml dump ]====----***");
		}
		
		try {
			Metrics metrics  = new Metrics(this, 6039);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new Metrics.AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
		        @Override
		        public Map<String, Integer> call() throws Exception {
		            Map<String, Integer> valueMap = new HashMap<>();
		            
		            //if(getServer().getPluginManager().getPlugin("DragonDropElytra") != null){valueMap.put("DragonDropElytra", 1);}							// 1
		    		if(getServer().getPluginManager().getPlugin("MoreMobHeads") != null){valueMap.put("MoreMobHeads", 1);}										// 2
		    		if(getServer().getPluginManager().getPlugin("NoEndermanGrief") != null){valueMap.put("NoEndermanGrief", 1);}								// 3
					if(getServer().getPluginManager().getPlugin("RotationalWrench") != null){valueMap.put("RotationalWrench", 1);}								// 4
		    		if(getServer().getPluginManager().getPlugin("ShulkerRespawner") != null){valueMap.put("ShulkerRespawner", 1);}								// 5
		    		if(getServer().getPluginManager().getPlugin("SilenceMobs") != null){valueMap.put("SilenceMobs", 1);}										// 6
		    		if(getServer().getPluginManager().getPlugin("SinglePlayerSleep") != null){valueMap.put("SinglePlayerSleep", 1);}							// 7
					if(getServer().getPluginManager().getPlugin("VillagerWorkstationHighlights") != null){valueMap.put("VillagerWorkstationHighlights", 1);} 	// 8
		    		if(getServer().getPluginManager().getPlugin("PortalHelper") != null){valueMap.put("PortalHelper", 1);}										// 9
		            return valueMap;
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("auto_update_check", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("auto_update_check").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("debug", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("debug").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("lang", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("lang").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("drop_on_ground", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("do_what.drop_on_ground").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("place_in_chest", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("do_what.place_in_chest").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("give_to_player", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("do_what.give_to_player").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("brokenelytra", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("drop.brokenelytra").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("dragonhead", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("drop.dragonhead").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("dragonegg", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("drop.dragonegg").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("chance_elytra_randomdrop", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("chance.elytra.chancepercentdrop").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("chance_elytra_chancepercent", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("chance.elytra.chancepercent").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("chance_dragonhead_randomdrop", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("chance.dragonhead.chancepercentdrop").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("chance_dragonhead_chancepercent", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("chance.dragonhead.chancepercent").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("chance_dragonegg_randomdrop", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("chance.dragonegg.chancepercentdrop").toUpperCase();
		        }
		    }));
			metrics.addCustomChart(new Metrics.SimplePie("chance_dragonegg_chancepercent", new Callable<String>() {
		        @Override
		        public String call() throws Exception {
		            return "" + getConfig().getString("chance.dragonegg.chancepercent").toUpperCase();
		        }
		    }));
			
		}catch (Exception e){
			// Failed to submit the stats
		}
		//logWarn(Ansi.RED + Ansi.Bold + "chancepercent is not higher then 0.00, or under 0.99.");
	}
	
	@Override // TODO: onDisable
	public void onDisable(){
		consoleInfo(Ansi.BOLD + "DISABLED" + Ansi.RESET);
	}
	
	public void consoleInfo(String state) {
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
		logger.info(Ansi.GREEN + THIS_NAME + " v" + THIS_VERSION + Ansi.RESET + " is " + state);
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.RESET);
	}
	
	public  void log(String dalog){
		logger.info(Ansi.YELLOW + "" + pluginName + Ansi.RESET + " " + dalog + Ansi.RESET);
	}
	public  void log(Level lvl, String dalog){
		logger.log(lvl, dalog);
	}
	public  void logDebug(String dalog){
		log("" + THIS_VERSION + Ansi.RED + Ansi.BOLD + "[DEBUG] " + Ansi.RESET + dalog);
	}
	public void logWarn(String dalog){
		log("" + THIS_VERSION + Ansi.RED + Ansi.BOLD + "[WARNING] " + Ansi.RESET + dalog  + Ansi.RESET);
	}
	
	
	@SuppressWarnings({ "unused" })
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event){
		if(event.getEntity() instanceof EnderDragon){
			if(debug){logDebug("START EDE - " + ChatColor.BOLD + ChatColor.RED + "Include this line until END EDE below with issue reports.".toUpperCase() + ChatColor.RESET);}
			if(debug){logDebug("EDE enity is EnderDragon line:435");}
			world_whitelist = getConfig().getString("world.whitelist", "");
			world_blacklist = getConfig().getString("world.blacklist", "");
			LivingEntity daDragon = event.getEntity();
			Player daKiller = daDragon.getKiller();
			World world = event.getEntity().getWorld();
			try {
				if(world_whitelist != null&&!world_whitelist.isEmpty()&&world_blacklist != null&&!world_blacklist.isEmpty()){
					if( !StrUtils.stringContains(world_whitelist, world.getName().toString()) && StrUtils.stringContains(world_blacklist, world.getName().toString()) ){
						log("EDE - " + world.getName().toString() + " - On blacklist and Not on whitelist.");
						return;
					}else if( !StrUtils.stringContains(world_whitelist, world.getName().toString()) && !StrUtils.stringContains(world_blacklist, world.getName().toString()) ){
						log("EDE - " + world.getName().toString() + " - Not on whitelist.");
						return;
					}else if( !StrUtils.stringContains(world_whitelist, world.getName().toString()) ){
						
					}else {
						if(debug) {logDebug("Current world=" + world.getName().toString());}
						if(debug) {logDebug("Worlds.whitelist=" + world_whitelist);}
						if(debug) {logDebug("Worlds.blacklist=" + world_blacklist);}
						if(debug) {logDebug("World on whitelist=" + StrUtils.stringContains(world_whitelist, world.getName().toString()) +
								" blacklist=" +StrUtils.stringContains(world_blacklist, world.getName().toString()) );}
					}
				}else if(world_whitelist != null&&!world_whitelist.isEmpty()){
					if(!StrUtils.stringContains(world_whitelist, world.getName().toString())){
						log("EDE - " + world.getName().toString() + " - Not on whitelist.");
						return;
					}else {
						if(debug) {logDebug("Current world=" + world.getName().toString());}
						if(debug) {logDebug("Worlds.whitelist=" + world_whitelist);}
						if(debug) {logDebug("Worlds.blacklist=" + world_blacklist);}
						if(debug) {logDebug("World on whitelist=" + StrUtils.stringContains(world_whitelist, world.getName().toString()) +
								" blacklist=" +StrUtils.stringContains(world_blacklist, world.getName().toString()) );}
					}
				}else if(world_blacklist != null&&!world_blacklist.isEmpty()){
					if(StrUtils.stringContains(world_blacklist, world.getName().toString())){
						log("EDE - " + world.getName().toString() + " - On blacklist.");
						return;
					}else {
						if(debug) {logDebug("Current world=" + world.getName().toString());}
						if(debug) {logDebug("Worlds.whitelist=" + world_whitelist);}
						if(debug) {logDebug("Worlds.blacklist=" + world_blacklist);}
						if(debug) {logDebug("World on whitelist=" + StrUtils.stringContains(world_whitelist, world.getName().toString()) +
								" blacklist=" +StrUtils.stringContains(world_blacklist, world.getName().toString()) );}
					}
				}else {
					if(debug) {logDebug("Current world=" + world.getName().toString());}
					if(debug) {logDebug("Worlds.whitelist=" + world_whitelist);}
					if(debug) {logDebug("Worlds.blacklist=" + world_blacklist);}
					if(debug) {logDebug("World on whitelist=" + StrUtils.stringContains(world_whitelist, world.getName().toString()) +
							" blacklist=" +StrUtils.stringContains(world_blacklist, world.getName().toString()) );}
				}
				
				ItemStack elytra = new ItemStack(Material.ELYTRA, 1);
				ItemStack brokenelytra = new ItemStack(Material.ELYTRA, 1);
				ItemStack dragonhead = new ItemStack(Material.DRAGON_HEAD, 1);
				ItemStack dragonegg = new ItemStack(Material.DRAGON_EGG, 1);
	
				/** Random chance */
				// elytra chance
				boolean dropElytra = true;
				if(getConfig().getBoolean("chance.elytra.chancepercentdrop", false)) {
					if(debug) {logDebug("EDE DI elytra");}
					dropElytra = dropIt(event, getConfig().getDouble("chance.elytra.chancepercent", 0.25));
					if(debug) {logDebug("EDE dropElytra=" + dropElytra);}
				}
				// dragonhead chance
				boolean dropHead = true;
				if(getConfig().getBoolean("chance.dragonhead.chancepercentdrop", false)) {
					if(debug) {logDebug("EDE DI dragon head");}
					dropHead = dropIt(event, getConfig().getDouble("chance.dragonhead.chancepercent", 0.25));
					if(debug) {logDebug("EDE dropHead=" + dropHead);}
				}
				// dragonegg chance
				boolean dropEgg = true;
				if(getConfig().getBoolean("chance.dragonegg.chancepercentdrop", false)) {
					if(debug) {logDebug("EDE DI dragon egg");}
					dropEgg = dropIt(event, getConfig().getDouble("chance.dragonegg.chancepercent", 0.25));
					if(debug) {logDebug("EDE dropEgg=" + dropEgg);}
				}
	
				/** damaged item test */
				if(debug){logDebug("EDE drop.brokenelytra=true line:485");}
				final Damageable im = (Damageable) brokenelytra.getItemMeta();
				im.setDamage(431);
				brokenelytra.setItemMeta((ItemMeta) im);
				/** damaged item test */
				
				String[] loc = ConfigAPI.GetConfigStr(this, "droploc").split(",");
				double x;
				double y;
				double z;
				x = Double.parseDouble(loc[0]);
				y = Double.parseDouble(loc[1]);
				z = Double.parseDouble(loc[2]);
				if(getConfig().getBoolean("do_what.give_to_player", false)){
					if(debug){logDebug("EDE do_what.give_to_player=true line:548");}
					final Location dropLocation = daKiller.getLocation();
					if(dropElytra){
						if(getConfig().getBoolean("drop.brokenelytra", false)){
							if(daKiller != null && daKiller.getInventory().firstEmpty() != -1 ){
								if(debug){logDebug("EDE drop.brokenelytra=true line:565");}
								daKiller.getInventory().addItem(brokenelytra);
								daKiller.sendMessage("" + get("dde.message.addedtoinventory"));
							}else if(daKiller != null && daKiller.getInventory().firstEmpty() == -1 ){
								// Killer is not null, and inventory is full.
									if(debug){logDebug("EDE IF killer!=null & killer inventory=full line:561");}
								world.dropItemNaturally(dropLocation, brokenelytra);
								daKiller.sendMessage("" + get("dde.message.inventoryfull" + "") + " - " + get("dde.message.elytradroppedat") + "  x:" + 
								dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
							}else if(daKiller == null ){
								// Killer is null
									if(debug){logDebug("EDE IF killer=null line:567");}
								world.dropItemNaturally(new Location(dropLocation.getWorld(), x, y, z),  brokenelytra);
								log("" + get("dde.message.killer_null") + " - " + get("dde.message.elytradroppedat") + "  x:" + 
								x + " , y:" + y + " , z:" + z );
							}
						}
						// Drop elytra
						if( getConfig().getBoolean("drop.elytra", true) && dropElytra ) {
							if(debug){logDebug("EDE IF drop.elytra=true & dropElytra=true line:553");}
								if(daKiller != null && daKiller.getInventory().firstEmpty() != -1 ){
									// Killer is not null, and inventory is NOT full.
										if(debug){logDebug("EDE IF killer!=null & killer inventory!=full line:556");}
									daKiller.getInventory().addItem(elytra);
									daKiller.sendMessage("" + get("dde.message.addedtoinventory"));
								}else if(daKiller != null && daKiller.getInventory().firstEmpty() == -1 ){
									// Killer is not null, and inventory is full.
										if(debug){logDebug("EDE IF killer!=null & killer inventory=full line:561");}
									world.dropItemNaturally(dropLocation, elytra);
									daKiller.sendMessage("" + get("dde.message.inventoryfull" + "") + " - " + get("dde.message.elytradroppedat") + "  x:" + 
									dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
								}else if(daKiller == null ){
									// Killer is null
										if(debug){logDebug("EDE IF killer=null line:567");}
									world.dropItemNaturally(new Location(dropLocation.getWorld(), x, y, z), elytra);
									log("" + get("dde.message.killer_null") + " - " + get("dde.message.elytradroppedat") + "  x:" + 
									x + " , y:" + y + " , z:" + z );
								}
						}
					}
					// Drop Dragon Head
					if( getConfig().getBoolean("drop.dragonhead", false) && dropHead ) {
						if(dropHead){
							if(daKiller != null && daKiller.getInventory().firstEmpty() != -1 ){
								// Killer is not null, and inventory is NOT full.
									if(debug){logDebug("EDE IF killer!=null & killer inventory!=full line:577");}
								daKiller.getInventory().addItem(dragonhead);
								daKiller.sendMessage("" + get("dde.message.addedtoinventory").toString().replace("Elytra", "Dragon Head"));
							}else if(daKiller != null && daKiller.getInventory().firstEmpty() == -1 ){
								// Killer is not null, and inventory is full.
									if(debug){logDebug("EDE IF killer!=null & killer inventory=full line:582");}
								world.dropItemNaturally(dropLocation, dragonhead);
								daKiller.sendMessage("" + get("dde.message.inventoryfull" + "") + " - " + get("dde.message.head_dropped_at") + "  x:" + 
								dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
							}else if(daKiller == null ){
								// Killer is null
									if(debug){logDebug("EDE IF killer=null line:588");}
								world.dropItemNaturally(new Location(dropLocation.getWorld(), x, y, z), dragonhead);
								log("" + get("dde.message.killer_null") + " - " + get("dde.message.head_dropped_at") + "  x:" + 
								x + " , y:" + y + " , z:" + z );
							}
						}
					}
					// Drop Dragon Egg
					if( getConfig().getBoolean("drop.dragonegg", false) && dropEgg ) {
						if(dropEgg){
							if(daKiller != null && daKiller.getInventory().firstEmpty() != -1 ){
								// Killer is not null, and inventory is NOT full.
									if(debug){logDebug("EDE IF killer!=null & killer inventory!=full line:598");}
								daKiller.getInventory().addItem(dragonegg);
								daKiller.sendMessage("" + get("dde.message.addedtoinventory").toString().replace("Elytra", "Dragon Egg"));
							}else if(daKiller != null && daKiller.getInventory().firstEmpty() == -1 ){
								// Killer is not null, and inventory is full.
									if(debug){logDebug("EDE IF killer!=null & killer inventory=full line:603");}
								world.dropItemNaturally(dropLocation, dragonegg);
								daKiller.sendMessage("" + get("dde.message.inventoryfull" + "") + " - " + get("dde.message.egg_dropped_at") + "  x:" + 
								dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
							}else if(daKiller == null ){
								// Killer is null
									if(debug){logDebug("EDE IF killer=null line:609");}
								world.dropItemNaturally(new Location(dropLocation.getWorld(), x, y, z), dragonegg);
								log("" + get("dde.message.killer_null") + " - " + get("dde.message.egg_dropped_at") + "  x:" + 
								x + " , y:" + y + " , z:" + z );
							}
						}
					}
				}
				
				if(getConfig().getBoolean("do_what.drop_on_ground", false)){
					if(debug){logDebug("EDE do_what.drop_on_ground=true line:618");}
					final Location dropLocation = new Location(world, x, y, z);
					if(dropElytra){
						if(getConfig().getBoolean("drop.brokenelytra", false)){
							if(debug){logDebug("EDE drop.brokenelytra=true line:575");}
							world.dropItem(dropLocation,  brokenelytra);
						}
						if(getConfig().getBoolean("drop.elytra", true)){
							if(debug){logDebug("EDE IF drop.elytra=true line:621");}
							world.dropItem(dropLocation, elytra);
						}
					}
					if(getConfig().getBoolean("drop.dragonhead", false)){
						if(debug){logDebug("EDE DOG drop.dragonhead=true line:627");}
						if(dropHead){
							world.dropItemNaturally(dropLocation, dragonhead);
						}
					}
					if(getConfig().getBoolean("drop.dragonegg", false)){
						if(debug){logDebug("EDE DOG drop.dragonegg=true line:633");}
						if(dropEgg){
							world.dropItemNaturally(daKiller.getLocation(), dragonegg);
						}
					}
					if(daKiller != null) {
						daKiller.sendMessage("" + get("dde.message.elytradroppedat" + "") + "  x:" + dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
					}
				}
				if( getConfig().getBoolean("do_what.drop_naturally", false) || daKiller == null ){
					if(debug){logDebug("EDE do_what.drop_naturally=true line:680");}
					EnderDragon theDragon = (EnderDragon) daDragon;
					if(dropElytra){
						if(getConfig().getBoolean("drop.brokenelytra", false)){
							if(debug){logDebug("EDE DN drop.brokenelytra=true line:684");}
							world.dropItemNaturally(daDragon.getLocation(),  brokenelytra);
						}
						if(getConfig().getBoolean("drop.elytra", true)){
							if(debug){logDebug("EDE DN drop.elytra=true line:688");}
							world.dropItemNaturally(daDragon.getLocation(), elytra);
						}
					}
					if(dropHead){
						if(getConfig().getBoolean("drop.dragonhead", false)){
							if(debug){logDebug("EDE DN drop.dragonhead=true line:694");}
						
							world.dropItemNaturally(daDragon.getLocation(), dragonhead);
						}
					}
					if(dropEgg){
						if(getConfig().getBoolean("drop.dragonegg", false)){
							if(debug){logDebug("EDE DN drop.dragonegg=true line:701");}
						
							world.dropItemNaturally(daDragon.getLocation(), dragonegg);
						}
					}
				}
				if(getConfig().getBoolean("do_what.place_in_chest", false)){
					if(debug){logDebug("EDE do_what.place_in_chest=true line:708");}
					Location chestLocation = new Location(world, x, y, z);
					if(!(chestLocation.getBlock() instanceof Chest)){
						chestLocation.getBlock().setType(Material.CHEST);
					}
					try{
						Chest blockChest = (Chest) chestLocation.getBlock().getState();
						if(dropElytra){
							if(getConfig().getBoolean("drop.brokenelytra", false)){
								if(debug){logDebug("EDE drop.brokenelytra=true line:575");}
								blockChest.getInventory().setItem(blockChest.getInventory().firstEmpty(),  brokenelytra);
							}
							if(getConfig().getBoolean("drop.elytra", true) && dropElytra ){
								if(debug){logDebug("EDE IF drop.elytra=true line:673");}
								blockChest.getInventory().setItem(blockChest.getInventory().firstEmpty(), elytra);
							}
						}
						
						if(getConfig().getBoolean("drop.dragonhead", true) && dropHead ){
							if(debug){logDebug("EDE PIC drop.dragonhead=true line:677");}
							if(dropHead){
								blockChest.getInventory().setItem(blockChest.getInventory().firstEmpty(), dragonhead);
							}
							//world.dropItemNaturally(daKiller.getLocation(), dragonhead);
						}
						if(getConfig().getBoolean("drop.dragonegg", false) && dropEgg ){
							if(debug){logDebug("EDE PIC drop.dragonegg=true line:684");}
							if(dropEgg){
								blockChest.getInventory().setItem(blockChest.getInventory().firstEmpty(), dragonegg);
							}
							//world.dropItemNaturally(daKiller.getLocation(), dragonegg);
						}
					}catch (Exception e){
						e.printStackTrace();
					}
					if(daKiller != null) {
						daKiller.sendMessage("" + get("dde.message.elytraplacedinchest" + "") + " x:" + chestLocation.getBlockX() + " , y:" + chestLocation.getBlockY() + " , z:" + chestLocation.getBlockZ());
					}
				}
			}catch(Exception e) {
				logWarn(ChatColor.LIGHT_PURPLE + "Exception caught, plugin is still working, but report this to the developer." + ChatColor.RESET);
				logWarn(ChatColor.LIGHT_PURPLE + "" + Bukkit.getServer().getVersion().toString());
				e.printStackTrace();
				logWarn(ChatColor.LIGHT_PURPLE + "Exception caught, plugin is still working, but report this to the developer." + ChatColor.RESET);
			}
			if(getConfig().getBoolean("do_what.prevent_dragon_egg_spawn", false)) {
				EnderDragon dragon = (EnderDragon) event.getEntity();
				//DragonBattle db = (DragonBattle) dragon.getDragonBattle();
				boolean hbpk = dragon.getDragonBattle().hasBeenPreviouslyKilled();
				if(hbpk) {
					return;
				}else {
					if(debug){logDebug("EDE Looking for DragonEgg...");}
					double delay = getConfig().getDouble("do_what.prevent_dragon_egg_spawn_delay_init", 5);
					double delay2 = getConfig().getDouble("do_what.prevent_dragon_egg_spawn_delay_run", 5);
					
					killdragonegg = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
						int i = 0;
						public void run() {
							Location block2 = world.getHighestBlockAt(0, 0).getLocation();
							double Y = block2.getY();
							for(double i = Y;i >= 0;i--) {
								Material mat = world.getBlockAt(0, (int) i, 0).getType();
								if(mat.equals(Material.DRAGON_EGG)) {
									Y = i;
									if(debug){logDebug("EDE dragon egg found 0 " + Y + " 0");}
									break;
									
								}
							}
							Material mat = world.getBlockAt(0, (int) Y, 0).getType();
							if(mat.equals(Material.DRAGON_EGG)) {
								block2.setY(Y);
								world.getBlockAt(block2).setType(Material.AIR);
								if(debug){logDebug("EDE DragonEgg Destroyed");}
								Bukkit.getServer().getScheduler().cancelTask(killdragonegg);
							}else if(i >= 300){
								if(debug){logDebug("EDE dragon egg NOT found.");}
								Bukkit.getServer().getScheduler().cancelTask(killdragonegg);
							}
							if(debug){logDebug("EDE TT ran line:652");}
							i++;
						}
						
					}, (long) (delay * 20), (long) (delay2 * 20));
				}
			}
			if(getConfig().getBoolean("do_what.send_console_command", false)) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "" + getConfig().getString("do_what.console_command", "say No command has been set in config.yml") );
			}
			if(debug){logDebug("END EDE - " + ChatColor.BOLD + ChatColor.RED + "Include this line up to START EDE above with issue reports.".toUpperCase() + ChatColor.RESET);}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){ // TODO: onCommand
		if (cmd.getName().equalsIgnoreCase("DDE")){
			if(debug) {logDebug("DDE command='" + cmd.getName() + "' args.length='" + args.length + "' args='" + Arrays.toString(args) + "'");}
			if (args.length == 0){
				String perm = "dde.op";
				if(sender.isOp()||sender.hasPermission(perm)){
					sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + THIS_NAME + ChatColor.GREEN + "]===============[]");
					sender.sendMessage(ChatColor.GOLD + " /dde reload - " + get("dde.command.reload" + ""));//Reload config file.");
					sender.sendMessage(ChatColor.GOLD + " /dde td - " + get("dde.message.debuguse")  );//Reload config file.");
					sender.sendMessage(ChatColor.GOLD + " " + get("dde.version.donate") + ": https://ko-fi.com/joelgodofwar" );
					sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + THIS_NAME + ChatColor.GREEN + "]===============[]");
					// https://ko-fi.com/joelgodofwar
					return true;
				}else if(!sender.hasPermission(perm)){
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("dde.message.noperm" + "").toString().replace("<perm>", perm) );
				}
			}
			
			//
			if(args[0].equalsIgnoreCase("reload")){
				String perm = "dde.op";
				if(sender.isOp()||sender.hasPermission(perm)||!(sender instanceof Player)){
					//ConfigAPI.Reloadconfig(this, p);
					this.reloadConfig();
					DragonDropElytra plugin = this;
					getServer().getPluginManager().disablePlugin(plugin);
					getServer().getPluginManager().enablePlugin(plugin);
					try {
						config.load(new File(getDataFolder(), "config.yml"));
	      			} catch (IOException | InvalidConfigurationException e1) {
	      				logWarn("Could not load config.yml");
	      				e1.printStackTrace();
	      			}
					world_whitelist = config.getString("world.whitelist", "");
					world_blacklist = config.getString("world.blacklist", "");
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("dde.message.reloaded" + ""));
				}else if(!sender.hasPermission(perm)){
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("dde.message.noperm" + "").toString().replace("<perm>", perm) );
				}
			}
			if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
				String perm = "dde.op";
				if(sender.isOp()||sender.hasPermission(perm)||!(sender instanceof Player)){
					debug = !debug;
					  sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + 
					  get("dde.message.debugtrue").toString().replace("<boolean>", get("dde.message.boolean." + String.valueOf(debug).toLowerCase()) ));
					return true;
				}else if(!sender.hasPermission(perm)){
					sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("dde.message.noperm" + "").toString().replace("<perm>", perm) );
					return false;
				}
			}
		}
		return true;
	}
	
	@Override 
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { // TODO: Tab Complete
		if (command.getName().equalsIgnoreCase("DDE")) {
			List<String> autoCompletes = new ArrayList<>(); //create a new string list for tab completion
			if (args.length == 1) { // reload, toggledebug, playerheads, customtrader, headfix
				autoCompletes.add("reload");
				autoCompletes.add("toggledebug");
				return autoCompletes; // then return the list
			}
		}
		return null;
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event){ //TODO: onPlayerJoinEvent
	    Player player = event.getPlayer();
	    if(UpdateAvailable&&(player.isOp()||player.hasPermission("dde.showUpdateAvailable"))){
			String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
			links = links.replace("<DownloadLink>", DownloadLink).replace("<Download>", get("dde.version.download"))
					.replace("<Donate>", get("dde.version.donate")).replace("<please_update>", get("dde.version.please_update"))
					.replace("<Donate_msg>", get("dde.version.donate.message")).replace("<Notes>", get("dde.version.notes"))
					.replace("<Notes_msg>", get("dde.version.notes.message"));
			String versions = "" + ChatColor.GRAY + get("dde.version.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + get("dde.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
			player.sendMessage("" + ChatColor.GRAY + get("dde.version.message").toString().replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.GRAY) );
			Utils.sendJson(player, links);
			player.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
	    }
	    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd");
	    LocalDate localDate = LocalDate.now();
	    String daDay = dtf.format(localDate);

	    if (daDay.equals("04/16")) {
	        String playerId = player.getUniqueId().toString();
	        if (!triggeredPlayers.contains(playerId)) {
	            if (isPluginRequired(THIS_NAME)) {
	                player.sendTitle("Happy Birthday Mom", "I miss you - 4/16/1954-12/23/2022", 10, 70, 20);
	            }
	            triggeredPlayers.add(playerId);
	        }
	    }
	    if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
	    	//String damsg = "/give JoelYahwehOfWar bow{display:{Name:\"\"DragonSlayer\"\"},Enchantments:[{id:unbreaking,lvl:3},{id:power,lvl:1000},{id:punch,lvl:200},{id:infinity,lvl:1}]} 1";
	    	//Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "/give JoelYahwehOfWar bow{display:{Name:\"\"DragonSlayer\"\"},Enchantments:[{id:unbreaking,lvl:3},{id:power,lvl:1000},{id:punch,lvl:200},{id:infinity,lvl:1}]} 1");
	    	//damsg = "/give JoelYahwehOfWar trident{display:{Name:\"\"DragonSlayer\"\"},Enchantments:[{id:unbreaking,lvl:3},{id:loyalty,lvl:10},{id:impaling,lvl:1000},{id:channeling,lvl:10}]} 1";
	    	//Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "/give JoelYahwehOfWar trident{display:{Name:\"\"DragonSlayer\"\"},Enchantments:[{id:unbreaking,lvl:3},{id:loyalty,lvl:10},{id:impaling,lvl:1000},{id:channeling,lvl:10}]} 1");
	    	player.sendMessage(THIS_NAME + " " + THIS_VERSION + " Hello father!");
	    }
	}
	
	public static String getVersion() {
		String strVersion = Bukkit.getVersion();
		strVersion = strVersion.substring(strVersion.indexOf("MC: "), strVersion.length());
		strVersion = strVersion.replace("MC: ", "").replace(")", "");
		return strVersion;
	}
	
	public static Material getMaterialFromID(String id){
		return Material.getMaterial(id);
	}
	
	public static void copyFile_Java7(String origin, String destination) throws IOException {
		Path FROM = Paths.get(origin);
		Path TO = Paths.get(destination);
		//overwrite the destination file if it exists, and copy
		// the file attributes, including the rwx permissions
		CopyOption[] options = new CopyOption[]{
			StandardCopyOption.REPLACE_EXISTING,
			StandardCopyOption.COPY_ATTRIBUTES
		}; 
		Files.copy(FROM, TO, options);
	}
	
	public boolean dropIt(EntityDeathEvent event, double chancepercent) {
		int enchantmentlevel = 0;
		if(event.getEntity().getKiller() != null) {
			ItemStack itemstack = event.getEntity().getKiller().getInventory().getItemInMainHand();
			enchantmentlevel = itemstack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
		}
		double enchantmentlevelpercent = ((double)enchantmentlevel / 100);
		
		double chance = Math.random();
		if(debug){logDebug("dropIt chance=" + chance);}
		//double chancepercent = getConfig().getDouble("chancepercent", 0.25);
		if(debug){logDebug("dropIt chancepercent=" + chancepercent);}
		chancepercent = chancepercent + enchantmentlevelpercent;
		if(chancepercent > 0.00 && chancepercent <= 1.00){
		    if (chancepercent > chance){
		    	return true;
		    }else{
		    	return false;
		    }
		}else{
			event.getEntity().getKiller().sendMessage("" + get("dde.message.chancepercentwrong" + "") + " " + get("dde.message.notifyserveradmin" + ""));
			log(Ansi.RED + "" + get("dde.message.chancepercentwrong" + ""));
		}
		return false;
	}
	
	@EventHandler
    public void onBlockDropItemEvent(BlockDropItemEvent event) {
        @Nonnull BlockState blockState = event.getBlockState();
        if (blockState.getType() != Material.DRAGON_EGG) return;
        if(debug){logDebug("BDIE Block is dragon egg.");}
        if(blockState.getWorld().getEnvironment().equals(Environment.THE_END)) {
        	if(getConfig().getBoolean("do_what.prevent_dragon_egg_spawn", false)) {
				Location block2 = blockState.getWorld().getHighestBlockAt(0, 0).getLocation();
				for (Item item: event.getItems()) { // Ideally should only be one...
		            @Nonnull ItemStack itemstack = item.getItemStack();
		            if (itemstack.getType() == Material.DRAGON_EGG) {
		            	if(debug){logDebug("BDIE Item dropped is dragon egg.");}
		            	if(blockState.getLocation().distance(block2) <= 10) {
		            		if(debug){logDebug("BDIE item was dropped within 10 blocks of 0 0.");}
		            		itemstack.setType(Material.AIR);
		            		blockState.setType(Material.AIR);
		            		if(debug){logDebug("BDIE Dragon Egg at 0 0  has been set to Air.");}
		            		event.setCancelled(true);
		            	}
		            }
		        }
        	}
        }
    }
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		Block block = event.getBlock();
		Location loc = event.getBlock().getLocation();
		if(getConfig().getBoolean("do_what.prevent_dragon_egg_spawn", false)) {
			Location block2 = block.getWorld().getHighestBlockAt(0, 0).getLocation();
			double Y = block2.getY();
			for(double i = Y;i >= 0;i--) {
				Material mat = block.getWorld().getBlockAt(0, (int) i, 0).getType();
				if(mat.equals(Material.BEDROCK)||mat.equals(Material.DRAGON_EGG)) {
					Y = i;break;
				}
			}
			block2.setY(Y);
			if( (loc.getX() == 1&&loc.getZ() == 0||loc.getX() == 0&&loc.getZ() == 1||loc.getX() == -1&&loc.getZ() == 0||loc.getX() == 0&&loc.getZ() == -1) 
					|| loc.distance(block2) < 16 ) {
				if(debug){logDebug("BFTE Piston blocked from extending.");}
				event.setCancelled(true);
				// /-1 0, /1 0, 0 -1, /0 1
			}
		}
	}
	
	@EventHandler //
	public void onBlockMove(BlockFromToEvent event) {
		Block block = event.getBlock();
		if(block.getWorld().getEnvironment().equals(Environment.THE_END)) {
			if(debug){logDebug("BFTE env=the_end");}
			if(getConfig().getBoolean("do_what.prevent_dragon_egg_spawn", false)) {
				Location block2 = block.getWorld().getHighestBlockAt(0, 0).getLocation();
				if(block.getType().equals(Material.DRAGON_EGG)&&block.getLocation().distance(block2) <= 10) {
					block.getDrops().clear();
					block.setType(Material.AIR);
					if(debug){logDebug("BFTE Dragon Egg at 0 0  has been set to Air.");}
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler //
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if(block.getWorld().getEnvironment().equals(Environment.THE_END)) {
			if(getConfig().getBoolean("do_what.prevent_dragon_egg_spawn", false)) {
				Location block2 = block.getWorld().getHighestBlockAt(0, 0).getLocation();
				if(block.getType().equals(Material.DRAGON_EGG)&&block.getLocation().distance(block2) <= 10) {
					block.setType(Material.AIR);
					if(debug){logDebug("BBE Dragon Egg at 0 0  has been set to Air.");}
					event.setCancelled(true);
				}
			}
		}
	}
	
	public String LoadTime(long startTime) {
	    long elapsedTime = System.currentTimeMillis() - startTime;
	    long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
	    long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60;
	    long milliseconds = elapsedTime % 1000;
	    
	    if (minutes > 0) {
	        return String.format("%d min %d s %d ms.", minutes, seconds, milliseconds);
	    } else if (seconds > 0) {
	        return String.format("%d s %d ms.", seconds, milliseconds);
	    } else {
	        return String.format("%d ms.", elapsedTime);
	    }
	}
	
	@SuppressWarnings("static-access")
	public String get(String key, String... defaultValue) {
		return lang2.get(key, defaultValue);
	}
	
	public boolean isPluginRequired(String pluginName) {
	    String[] requiredPlugins = {"SinglePlayerSleep", "MoreMobHeads", "NoEndermanGrief", "ShulkerRespawner", "DragonDropElytra", "RotationalWrench", "SilenceMobs", "VillagerWorkstationHighlights"};
	    for (String requiredPlugin : requiredPlugins) {
	        if (getServer().getPluginManager().getPlugin(requiredPlugin) != null && getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
	            if (requiredPlugin.equals(pluginName)) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	    }
	    return true;
	}
	
}