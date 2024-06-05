package com.github.joelgodofwar.dde;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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

import com.github.joelgodofwar.dde.bstats.bukkit.Metrics;
import com.github.joelgodofwar.dde.bstats.charts.AdvancedPie;
import com.github.joelgodofwar.dde.bstats.charts.SimplePie;
import com.github.joelgodofwar.dde.common.MinecraftVersion;
import com.github.joelgodofwar.dde.common.PluginLibrary;
import com.github.joelgodofwar.dde.common.PluginLogger;
import com.github.joelgodofwar.dde.common.error.DetailedErrorReporter;
import com.github.joelgodofwar.dde.common.error.Report;
import com.github.joelgodofwar.dde.i18n.Translator;
import com.github.joelgodofwar.dde.util.StrUtils;
import com.github.joelgodofwar.dde.util.Utils;
import com.github.joelgodofwar.dde.util.VersionChecker;
import com.github.joelgodofwar.dde.util.YmlConfiguration;

public class DragonDropElytra  extends JavaPlugin implements Listener{
	/** Languages: čeština (cs_CZ), Deutsch (de_DE), English (en_US), Español (es_ES), Español (es_MX), Français (fr_FR), Italiano (it_IT), Magyar (hu_HU), 日本語 (ja_JP), 한국어 (ko_KR), Lolcat (lol_US), Melayu (my_MY), Nederlands (nl_NL), Polski (pl_PL), Português (pt_BR), Русский (ru_RU), Svenska (sv_SV), Türkçe (tr_TR), 中文(简体) (zh_CN), 中文(繁體) (zh_TW) */
	//public final static Logger logger = Logger.getLogger("Minecraft");
	static String THIS_NAME;
	static String THIS_VERSION;
	/** update checker variables */
	public int projectID = 71235; // https://spigotmc.org/resources/71236
	public String githubURL = "https://raw.githubusercontent.com/JoelGodOfwar/DragonDropElytra/master/versions/1.13/versions.xml";
	boolean UpdateAvailable =  false;
	public String UColdVers;
	public String UCnewVers;
	public boolean UpdateCheck;
	public String DownloadLink = "https://dev.bukkit.org/projects/dragondropelytra2";
	/** end update checker variables */
	public boolean debug;
	public static String daLang;
	YmlConfiguration config = new YmlConfiguration();
	YamlConfiguration oldconfig = new YamlConfiguration();
	String world_whitelist;
	String world_blacklist;
	public int killdragonegg = 0;
	String configVersion = "1.0.14";
	String pluginName = THIS_NAME;
	Translator lang2;
	//private Set<String> triggeredPlayers = new HashSet<>();
	public String jarfilename = this.getFile().getAbsoluteFile().toString();
	public static DetailedErrorReporter reporter;
	public boolean colorful_console;
	public PluginLogger LOGGER;

	@SuppressWarnings("unused") @Override //
	public void onEnable(){ //TODO: onEnable
		long startTime = System.currentTimeMillis();
		LOGGER = new PluginLogger(this);
		reporter = new DetailedErrorReporter(this);
		UpdateCheck = getConfig().getBoolean("plugin_settings.auto_update_check", true);
		debug = getConfig().getBoolean("plugin_settings.debug", false);
		daLang = getConfig().getString("plugin_settings.lang", "en_US");
		lang2 = new Translator(daLang, getDataFolder().toString());
		THIS_NAME = this.getDescription().getName();
		THIS_VERSION = this.getDescription().getVersion();
		colorful_console = getConfig().getBoolean("plugin_settings.colorful_console", true);

		LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.GREEN + " v" + THIS_VERSION + ChatColor.RESET + " Loading...");
		LOGGER.log("Server Version: " + getServer().getVersion().toString());

		MinecraftVersion version = this.verifyMinecraftVersion();
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			LOGGER.debug(ChatColor.RED + "Jar file contains -DEV, debug set to true" + ChatColor.RESET);
			LOGGER.debug(ChatColor.RED + "jarfilename= " + StrUtils.Right(jarfilename, jarfilename.length() - jarfilename.lastIndexOf(File.separatorChar)) + ChatColor.RESET);
			//log("jarfile contains dev, debug set to true.");
		}
		//log("jarfile=" + jarfile.toString());
		//log("Version: " + getVersion());
		String mcVersion = getVersion();
		LOGGER.log(ChatColor.RED + "VERSION=" + mcVersion + ChatColor.RESET);
		// 1.17.1
		// 0.1..2
		String[] vers = mcVersion.split("\\.");
		//LOGGER.log(ChatColor.RED + "vers length=" + vers.length + " vers toString=" + vers.toString() + ChatColor.RESET);
		int minor = Integer.parseInt(vers[1]);
		if(!(minor >= 13)) {
			LOGGER.log(ChatColor.RED + "WARNING!" + ChatColor.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + ChatColor.RESET);
			LOGGER.log(ChatColor.RED + "WARNING! " + ChatColor.YELLOW + get("jgow.message.server_not_version") + ChatColor.RESET);
			LOGGER.log(ChatColor.RED + "WARNING! " + ChatColor.YELLOW + THIS_NAME + " v" + THIS_VERSION + " disabling." + ChatColor.RESET);
			LOGGER.log(ChatColor.RED + "WARNING!" + ChatColor.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + ChatColor.RESET);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		/**  Check for config */
		try{
			if(!getDataFolder().exists()){
				LOGGER.log("Data Folder doesn't exist");
				LOGGER.log("Creating Data Folder");
				getDataFolder().mkdirs();
				LOGGER.log("Data Folder Created at " + getDataFolder());
			}
			File  file = new File(getDataFolder(), "config.yml");
			LOGGER.log("" + file);
			if(!file.exists()){
				LOGGER.log("config.yml not found, creating!");
				saveResource("config.yml", true);
			}
		}catch(Exception exception){
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_CHECK_CONFIG).error(exception));
		}
		/** end config check */
		/** Check if config.yml is up to date.*/
		boolean needConfigUpdate = false;
		try {
			oldconfig.load(new File(getDataFolder() + "" + File.separatorChar + "config.yml"));
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
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
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_COPY_FILE).error(exception));
			}
			try {
				oldconfig.load(new File(getDataFolder(), "config.yml"));
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
			}
			saveResource("config.yml", true);
			try {
				config.load(new File(getDataFolder(), "config.yml"));
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
			}
			try {
				oldconfig.load(new File(getDataFolder(), "old_config.yml"));
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
			}
			config.set("plugin_settings.auto_update_check", oldconfig.get("auto_update_check", true));
			config.set("plugin_settings.debug", oldconfig.get("debug", false));
			config.set("plugin_settings.lang", oldconfig.get("lang", "en_US"));
			config.set("plugin_settings.colorful_console", oldconfig.get("colorful_console", true));

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
			} catch (Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_SAVE_CONFIG).error(exception));
			}
			LOGGER.log("config.yml has been updated");
		}else{
			//log("" + "not found");
		}
		world_whitelist = getConfig().getString("world.whitelist", "");
		world_blacklist = getConfig().getString("world.blacklist", "");

		/** Update Checker */
		if(UpdateCheck){
			/** auto_update_check is true */
			try {
				LOGGER.log("Checking for updates...");
				VersionChecker updater = new VersionChecker(this, projectID, githubURL);
				if(updater.checkForUpdates()) {
					/** Update available */
					UpdateAvailable = true; // TODO: Update Checker
					UColdVers = updater.oldVersion();
					UCnewVers = updater.newVersion();

					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("jgow.version.message").toString().replace("<MyPlugin>", THIS_NAME) );
					LOGGER.log("* " + get("jgow.version.old_vers") + ChatColor.RED + UColdVers );
					LOGGER.log("* " + get("jgow.version.new_vers") + ChatColor.GREEN + UCnewVers );
					LOGGER.log("*");
					LOGGER.log("* " + get("jgow.version.please_update") );
					LOGGER.log("*");
					LOGGER.log("* " + get("jgow.version.download") + ": " + DownloadLink + "/files");
					LOGGER.log("* " + get("jgow.version.donate") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
				}else{
					/** Up to date */
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					LOGGER.log("* " + get("jgow.version.curvers"));
					LOGGER.log("* " + get("jgow.version.donate") + ": https://ko-fi.com/joelgodofwar");
					LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
					UpdateAvailable = false;
				}
			}catch(Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
				//LOGGER.log("This is not a fatal exception, report it, but plugin will continue to work.");
			}
		}else {
			/** auto_update_check is false so nag. */
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
			LOGGER.log("* " + get("jgow.version.donate.message") + ": https://ko-fi.com/joelgodofwar");
			LOGGER.log("*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*");
		}
		/** end update checker */

		getServer().getPluginManager().registerEvents(this, this);
		consoleInfo(ChatColor.GREEN + "ENABLED" + ChatColor.RESET + " - Loading took " + LoadTime(startTime));

		try {
			Metrics metrics  = new Metrics(this, 6039);
			// New chart here
			// myPlugins()
			metrics.addCustomChart(new AdvancedPie("my_other_plugins", new Callable<Map<String, Integer>>() {
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
			metrics.addCustomChart(new SimplePie("auto_update_check", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("auto_update_check").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("debug", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("debug").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("lang", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("lang").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("drop_on_ground", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("do_what.drop_on_ground").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("place_in_chest", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("do_what.place_in_chest").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("give_to_player", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("do_what.give_to_player").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("brokenelytra", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("drop.brokenelytra").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("dragonhead", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("drop.dragonhead").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("dragonegg", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("drop.dragonegg").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("chance_elytra_randomdrop", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("chance.elytra.chancepercentdrop").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("chance_elytra_chancepercent", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("chance.elytra.chancepercent").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("chance_dragonhead_randomdrop", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("chance.dragonhead.chancepercentdrop").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("chance_dragonhead_chancepercent", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("chance.dragonhead.chancepercent").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("chance_dragonegg_randomdrop", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("chance.dragonegg.chancepercentdrop").toUpperCase();
				}
			}));
			metrics.addCustomChart(new SimplePie("chance_dragonegg_chancepercent", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return "" + getConfig().getString("chance.dragonegg.chancepercent").toUpperCase();
				}
			}));

		} catch (Exception exception) {
			// Handle the exception or log it
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_METRICS_LOAD_ERROR).error(exception));
		}
		//LOGGER.warn(ChatColor.RED + ChatColor.Bold + "chancepercent is not higher then 0.00, or under 0.99.");
	}

	@Override // TODO: onDisable
	public void onDisable(){
		consoleInfo(ChatColor.RED + "DISABLED");
	}

	public void consoleInfo(String state) {
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
		LOGGER.log(ChatColor.YELLOW + " v" + THIS_VERSION + ChatColor.RESET + " is " + state  + ChatColor.RESET);
		//LOGGER.log(ChatColor.YELLOW + "**************************************" + ChatColor.RESET);
	}

	public boolean isFolia() {
		if(getServer().getVersion().toString().contains("Folia")) {
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "unused" })
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event){
		if(event.getEntity() instanceof EnderDragon){
			LOGGER.debug("START EDE - " + ChatColor.BOLD + ChatColor.RED + "Include this line until END EDE below with issue reports.".toUpperCase() + ChatColor.RESET);
			LOGGER.debug("EDE enity is EnderDragon line:435");
			world_whitelist = getConfig().getString("world.whitelist", "");
			world_blacklist = getConfig().getString("world.blacklist", "");
			LivingEntity daDragon = event.getEntity();
			Player daKiller = daDragon.getKiller();
			World world = event.getEntity().getWorld();
			try {
				if((world_whitelist != null)&&!world_whitelist.isEmpty()&&(world_blacklist != null)&&!world_blacklist.isEmpty()){
					if( !StrUtils.stringContains(world_whitelist, world.getName().toString()) && StrUtils.stringContains(world_blacklist, world.getName().toString()) ){
						LOGGER.log("EDE - " + world.getName().toString() + " - On blacklist and Not on whitelist.");
						return;
					}else if( !StrUtils.stringContains(world_whitelist, world.getName().toString()) && !StrUtils.stringContains(world_blacklist, world.getName().toString()) ){
						LOGGER.log("EDE - " + world.getName().toString() + " - Not on whitelist.");
						return;
					}else if( !StrUtils.stringContains(world_whitelist, world.getName().toString()) ){

					}else {
						LOGGER.debug("Current world=" + world.getName().toString());
						LOGGER.debug("Worlds.whitelist=" + world_whitelist);
						LOGGER.debug("Worlds.blacklist=" + world_blacklist);
						LOGGER.debug("World on whitelist=" + StrUtils.stringContains(world_whitelist, world.getName().toString()) +
								" blacklist=" +StrUtils.stringContains(world_blacklist, world.getName().toString()) );
					}
				}else if((world_whitelist != null)&&!world_whitelist.isEmpty()){
					if(!StrUtils.stringContains(world_whitelist, world.getName().toString())){
						LOGGER.log("EDE - " + world.getName().toString() + " - Not on whitelist.");
						return;
					}else {
						LOGGER.debug("Current world=" + world.getName().toString());
						LOGGER.debug("Worlds.whitelist=" + world_whitelist);
						LOGGER.debug("Worlds.blacklist=" + world_blacklist);
						LOGGER.debug("World on whitelist=" + StrUtils.stringContains(world_whitelist, world.getName().toString()) +
								" blacklist=" +StrUtils.stringContains(world_blacklist, world.getName().toString()) );
					}
				}else if((world_blacklist != null)&&!world_blacklist.isEmpty()){
					if(StrUtils.stringContains(world_blacklist, world.getName().toString())){
						LOGGER.log("EDE - " + world.getName().toString() + " - On blacklist.");
						return;
					}else {
						LOGGER.debug("Current world=" + world.getName().toString());
						LOGGER.debug("Worlds.whitelist=" + world_whitelist);
						LOGGER.debug("Worlds.blacklist=" + world_blacklist);
						LOGGER.debug("World on whitelist=" + StrUtils.stringContains(world_whitelist, world.getName().toString()) +
								" blacklist=" +StrUtils.stringContains(world_blacklist, world.getName().toString()) );
					}
				}else {
					LOGGER.debug("Current world=" + world.getName().toString());
					LOGGER.debug("Worlds.whitelist=" + world_whitelist);
					LOGGER.debug("Worlds.blacklist=" + world_blacklist);
					LOGGER.debug("World on whitelist=" + StrUtils.stringContains(world_whitelist, world.getName().toString()) +
							" blacklist=" +StrUtils.stringContains(world_blacklist, world.getName().toString()) );
				}

				ItemStack elytra = new ItemStack(Material.ELYTRA, 1);
				ItemStack brokenelytra = new ItemStack(Material.ELYTRA, 1);
				ItemStack dragonhead = new ItemStack(Material.DRAGON_HEAD, 1);
				ItemStack dragonegg = new ItemStack(Material.DRAGON_EGG, 1);

				/** Random chance */
				// elytra chance
				boolean dropElytra = true;
				if(getConfig().getBoolean("chance.elytra.chancepercentdrop", false)) {
					LOGGER.debug("EDE DI elytra");
					dropElytra = dropIt(event, getConfig().getDouble("chance.elytra.chancepercent", 0.25));
					LOGGER.debug("EDE dropElytra=" + dropElytra);
				}
				// dragonhead chance
				boolean dropHead = true;
				if(getConfig().getBoolean("chance.dragonhead.chancepercentdrop", false)) {
					LOGGER.debug("EDE DI dragon head");
					dropHead = dropIt(event, getConfig().getDouble("chance.dragonhead.chancepercent", 0.25));
					LOGGER.debug("EDE dropHead=" + dropHead);
				}
				// dragonegg chance
				boolean dropEgg = true;
				if(getConfig().getBoolean("chance.dragonegg.chancepercentdrop", false)) {
					LOGGER.debug("EDE DI dragon egg");
					dropEgg = dropIt(event, getConfig().getDouble("chance.dragonegg.chancepercent", 0.25));
					LOGGER.debug("EDE dropEgg=" + dropEgg);
				}

				/** damaged item test */
				LOGGER.debug("EDE drop.brokenelytra=true line:485");
				final Damageable im = (Damageable) brokenelytra.getItemMeta();
				im.setDamage(431);
				brokenelytra.setItemMeta((ItemMeta) im);
				/** damaged item test */

				String[] loc = getConfig().getString("droploc").replace("'", "").split(","); //ConfigAPI.GetConfigStr(this, "droploc").split(",");
				double x;
				double y;
				double z;
				x = Double.parseDouble(loc[0]);
				y = Double.parseDouble(loc[1]);
				z = Double.parseDouble(loc[2]);
				if(getConfig().getBoolean("do_what.give_to_player", false)){
					LOGGER.debug("EDE do_what.give_to_player=true line:548");
					final Location dropLocation = daKiller.getLocation();
					if(dropElytra){
						if(getConfig().getBoolean("drop.brokenelytra", false)){
							if((daKiller != null) && (daKiller.getInventory().firstEmpty() != -1) ){
								LOGGER.debug("EDE drop.brokenelytra=true line:565");
								daKiller.getInventory().addItem(brokenelytra);
								daKiller.sendMessage("" + get("jgow.message.addedtoinventory"));
							}else if((daKiller != null) && (daKiller.getInventory().firstEmpty() == -1) ){
								// Killer is not null, and inventory is full.
								LOGGER.debug("EDE IF killer!=null & killer inventory=full line:561");
								world.dropItemNaturally(dropLocation, brokenelytra);
								daKiller.sendMessage("" + get("jgow.message.inventoryfull" + "") + " - " + get("jgow.message.elytradroppedat") + "  x:" +
										dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
							}else if(daKiller == null ){
								// Killer is null
								LOGGER.debug("EDE IF killer=null line:567");
								world.dropItemNaturally(new Location(dropLocation.getWorld(), x, y, z),  brokenelytra);
								LOGGER.log("" + get("jgow.message.killer_null") + " - " + get("jgow.message.elytradroppedat") + "  x:" +
										x + " , y:" + y + " , z:" + z );
							}
						}
						// Drop elytra
						if( getConfig().getBoolean("drop.elytra", true) && dropElytra ) {
							LOGGER.debug("EDE IF drop.elytra=true & dropElytra=true line:553");
							if((daKiller != null) && (daKiller.getInventory().firstEmpty() != -1) ){
								// Killer is not null, and inventory is NOT full.
								LOGGER.debug("EDE IF killer!=null & killer inventory!=full line:556");
								daKiller.getInventory().addItem(elytra);
								daKiller.sendMessage("" + get("jgow.message.addedtoinventory"));
							}else if((daKiller != null) && (daKiller.getInventory().firstEmpty() == -1) ){
								// Killer is not null, and inventory is full.
								LOGGER.debug("EDE IF killer!=null & killer inventory=full line:561");
								world.dropItemNaturally(dropLocation, elytra);
								daKiller.sendMessage("" + get("jgow.message.inventoryfull" + "") + " - " + get("jgow.message.elytradroppedat") + "  x:" +
										dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
							}else if(daKiller == null ){
								// Killer is null
								LOGGER.debug("EDE IF killer=null line:567");
								world.dropItemNaturally(new Location(dropLocation.getWorld(), x, y, z), elytra);
								LOGGER.log("" + get("jgow.message.killer_null") + " - " + get("jgow.message.elytradroppedat") + "  x:" +
										x + " , y:" + y + " , z:" + z );
							}
						}
					}
					// Drop Dragon Head
					if( getConfig().getBoolean("drop.dragonhead", false) && dropHead ) {
						if(dropHead){
							if((daKiller != null) && (daKiller.getInventory().firstEmpty() != -1) ){
								// Killer is not null, and inventory is NOT full.
								LOGGER.debug("EDE IF killer!=null & killer inventory!=full line:577");
								daKiller.getInventory().addItem(dragonhead);
								daKiller.sendMessage("" + get("jgow.message.addedtoinventory").toString().replace("Elytra", "Dragon Head"));
							}else if((daKiller != null) && (daKiller.getInventory().firstEmpty() == -1) ){
								// Killer is not null, and inventory is full.
								LOGGER.debug("EDE IF killer!=null & killer inventory=full line:582");
								world.dropItemNaturally(dropLocation, dragonhead);
								daKiller.sendMessage("" + get("jgow.message.inventoryfull" + "") + " - " + get("jgow.message.head_dropped_at") + "  x:" +
										dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
							}else if(daKiller == null ){
								// Killer is null
								LOGGER.debug("EDE IF killer=null line:588");
								world.dropItemNaturally(new Location(dropLocation.getWorld(), x, y, z), dragonhead);
								LOGGER.log("" + get("jgow.message.killer_null") + " - " + get("jgow.message.head_dropped_at") + "  x:" +
										x + " , y:" + y + " , z:" + z );
							}
						}
					}
					// Drop Dragon Egg
					if( getConfig().getBoolean("drop.dragonegg", false) && dropEgg ) {
						if(dropEgg){
							if((daKiller != null) && (daKiller.getInventory().firstEmpty() != -1) ){
								// Killer is not null, and inventory is NOT full.
								LOGGER.debug("EDE IF killer!=null & killer inventory!=full line:598");
								daKiller.getInventory().addItem(dragonegg);
								daKiller.sendMessage("" + get("jgow.message.addedtoinventory").toString().replace("Elytra", "Dragon Egg"));
							}else if((daKiller != null) && (daKiller.getInventory().firstEmpty() == -1) ){
								// Killer is not null, and inventory is full.
								LOGGER.debug("EDE IF killer!=null & killer inventory=full line:603");
								world.dropItemNaturally(dropLocation, dragonegg);
								daKiller.sendMessage("" + get("jgow.message.inventoryfull" + "") + " - " + get("jgow.message.egg_dropped_at") + "  x:" +
										dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
							}else if(daKiller == null ){
								// Killer is null
								LOGGER.debug("EDE IF killer=null line:609");
								world.dropItemNaturally(new Location(dropLocation.getWorld(), x, y, z), dragonegg);
								LOGGER.log("" + get("jgow.message.killer_null") + " - " + get("jgow.message.egg_dropped_at") + "  x:" +
										x + " , y:" + y + " , z:" + z );
							}
						}
					}
				}

				if(getConfig().getBoolean("do_what.drop_on_ground", false)){
					LOGGER.debug("EDE do_what.drop_on_ground=true line:618");
					final Location dropLocation = new Location(world, x, y, z);
					if(dropElytra){
						if(getConfig().getBoolean("drop.brokenelytra", false)){
							LOGGER.debug("EDE drop.brokenelytra=true line:575");
							world.dropItem(dropLocation,  brokenelytra);
						}
						if(getConfig().getBoolean("drop.elytra", true)){
							LOGGER.debug("EDE IF drop.elytra=true line:621");
							world.dropItem(dropLocation, elytra);
						}
					}
					if(getConfig().getBoolean("drop.dragonhead", false)){
						LOGGER.debug("EDE DOG drop.dragonhead=true line:627");
						if(dropHead){
							world.dropItemNaturally(dropLocation, dragonhead);
						}
					}
					if(getConfig().getBoolean("drop.dragonegg", false)){
						LOGGER.debug("EDE DOG drop.dragonegg=true line:633");
						if(dropEgg){
							world.dropItemNaturally(daKiller.getLocation(), dragonegg);
						}
					}
					if(daKiller != null) {
						daKiller.sendMessage("" + get("jgow.message.elytradroppedat" + "") + "  x:" + dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
					}
				}
				if( getConfig().getBoolean("do_what.drop_naturally", false) || (daKiller == null) ){
					LOGGER.debug("EDE do_what.drop_naturally=true line:680");
					EnderDragon theDragon = (EnderDragon) daDragon;
					if(dropElytra){
						if(getConfig().getBoolean("drop.brokenelytra", false)){
							LOGGER.debug("EDE DN drop.brokenelytra=true line:684");
							world.dropItemNaturally(daDragon.getLocation(),  brokenelytra);
						}
						if(getConfig().getBoolean("drop.elytra", true)){
							LOGGER.debug("EDE DN drop.elytra=true line:688");
							world.dropItemNaturally(daDragon.getLocation(), elytra);
						}
					}
					if(dropHead){
						if(getConfig().getBoolean("drop.dragonhead", false)){
							LOGGER.debug("EDE DN drop.dragonhead=true line:694");

							world.dropItemNaturally(daDragon.getLocation(), dragonhead);
						}
					}
					if(dropEgg){
						if(getConfig().getBoolean("drop.dragonegg", false)){
							LOGGER.debug("EDE DN drop.dragonegg=true line:701");

							world.dropItemNaturally(daDragon.getLocation(), dragonegg);
						}
					}
				}
				if(getConfig().getBoolean("do_what.place_in_chest", false)){
					LOGGER.debug("EDE do_what.place_in_chest=true line:708");
					Location chestLocation = new Location(world, x, y, z);
					if(!(chestLocation.getBlock() instanceof Chest)){
						chestLocation.getBlock().setType(Material.CHEST);
					}
					try{
						Chest blockChest = (Chest) chestLocation.getBlock().getState();
						if(dropElytra){
							if(getConfig().getBoolean("drop.brokenelytra", false)){
								LOGGER.debug("EDE drop.brokenelytra=true line:575");
								blockChest.getInventory().setItem(blockChest.getInventory().firstEmpty(),  brokenelytra);
							}
							if(getConfig().getBoolean("drop.elytra", true) && dropElytra ){
								LOGGER.debug("EDE IF drop.elytra=true line:673");
								blockChest.getInventory().setItem(blockChest.getInventory().firstEmpty(), elytra);
							}
						}

						if(getConfig().getBoolean("drop.dragonhead", true) && dropHead ){
							LOGGER.debug("EDE PIC drop.dragonhead=true line:677");
							if(dropHead){
								blockChest.getInventory().setItem(blockChest.getInventory().firstEmpty(), dragonhead);
							}
							//world.dropItemNaturally(daKiller.getLocation(), dragonhead);
						}
						if(getConfig().getBoolean("drop.dragonegg", false) && dropEgg ){
							LOGGER.debug("EDE PIC drop.dragonegg=true line:684");
							if(dropEgg){
								blockChest.getInventory().setItem(blockChest.getInventory().firstEmpty(), dragonegg);
							}
							//world.dropItemNaturally(daKiller.getLocation(), dragonegg);
						}
					}catch (Exception exception){
						reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_PLACE_IN_CHEST).error(exception));
					}
					if(daKiller != null) {
						daKiller.sendMessage("" + get("jgow.message.elytraplacedinchest" + "") + " x:" + chestLocation.getBlockX() + " , y:" + chestLocation.getBlockY() + " , z:" + chestLocation.getBlockZ());
					}
				}
			}catch(Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_DRAGON_DEATH).error(exception));
				// ERROR_PARSING_DRAGON_DEATH "Error parsing dragon death."
			}
			try {
				if(getConfig().getBoolean("do_what.prevent_dragon_egg_spawn", false)) {
					EnderDragon dragon = (EnderDragon) event.getEntity();
					//DragonBattle db = (DragonBattle) dragon.getDragonBattle();
					boolean hbpk = dragon.getDragonBattle().hasBeenPreviouslyKilled();
					if(hbpk) {
						return;
					}else {
						LOGGER.debug("EDE Looking for DragonEgg...");
						double delay = getConfig().getDouble("do_what.prevent_dragon_egg_spawn_delay_init", 5);
						double delay2 = getConfig().getDouble("do_what.prevent_dragon_egg_spawn_delay_run", 5);

						killdragonegg = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
							int i = 0;
							@Override public void run() {
								Location block2 = world.getHighestBlockAt(0, 0).getLocation();
								double Y = block2.getY();
								for(double i = Y;i >= 0;i--) {
									Material mat = world.getBlockAt(0, (int) i, 0).getType();
									if(mat.equals(Material.DRAGON_EGG)) {
										Y = i;
										LOGGER.debug("EDE dragon egg found 0 " + Y + " 0");
										break;

									}
								}
								Material mat = world.getBlockAt(0, (int) Y, 0).getType();
								if(mat.equals(Material.DRAGON_EGG)) {
									block2.setY(Y);
									world.getBlockAt(block2).setType(Material.AIR);
									LOGGER.debug("EDE DragonEgg Destroyed");
									Bukkit.getServer().getScheduler().cancelTask(killdragonegg);
								}else if(i >= 300){
									LOGGER.debug("EDE dragon egg NOT found.");
									Bukkit.getServer().getScheduler().cancelTask(killdragonegg);
								}
								LOGGER.debug("EDE TT ran line:652");
								i++;
							}

						}, (long) (delay * 20), (long) (delay2 * 20));
					}
				}
			}catch(Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_BLOCKING_DRAGON_EGG).error(exception));
				// ERROR_BLOCKING_DRAGON_EGG "Error blocking dragon egg."
			}
			try {
				if(getConfig().getBoolean("do_what.send_console_command", false)) {
					Bukkit.getScheduler().runTask(this, () -> {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "" + getConfig().getString("do_what.console_command", "say No command has been set in config.yml") );
					});
				}
			}catch(Exception exception) {
				reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_RUNNING_DRAGON_DEATH_COMMAND).error(exception));
				// ERROR_RUNNING_DRAGON_DEATH_COMMAND "Error running command after dragon death."
			}
			LOGGER.debug("END EDE - " + ChatColor.BOLD + ChatColor.RED + "Include this line up to START EDE above with issue reports.".toUpperCase() + ChatColor.RESET);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){ // TODO: onCommand
		try {
			if (cmd.getName().equalsIgnoreCase("DDE")){
				LOGGER.debug("DDE command='" + cmd.getName() + "' args.length='" + args.length + "' args='" + Arrays.toString(args) + "'");
				if (args.length == 0){
					String perm = "dde.op";
					if(sender.isOp()||sender.hasPermission(perm)){
						sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + THIS_NAME + ChatColor.GREEN + "]===============[]");
						sender.sendMessage(ChatColor.GOLD + " /dde reload - " + get("jgow.command.reload" + ""));//Reload config file.");
						sender.sendMessage(ChatColor.GOLD + " /dde td - " + get("jgow.message.debuguse")  );//Reload config file.");
						sender.sendMessage(ChatColor.GOLD + " " + get("jgow.version.donate") + ": https://ko-fi.com/joelgodofwar" );
						sender.sendMessage(ChatColor.GREEN + "[]===============[" + ChatColor.YELLOW + THIS_NAME + ChatColor.GREEN + "]===============[]");
						// https://ko-fi.com/joelgodofwar
						return true;
					}else if(!sender.hasPermission(perm)){
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("jgow.message.noperm" + "").toString().replace("<perm>", perm) );
					}
				}

				//
				if(args[0].equalsIgnoreCase("reload")){
					String perm = "dde.op";
					if(sender.isOp()||sender.hasPermission(perm)||!(sender instanceof Player)){
						this.reloadConfig();
						// DragonDropElytra plugin = this;
						// getServer().getPluginManager().disablePlugin(plugin);
						// getServer().getPluginManager().enablePlugin(plugin);
						LOGGER = new PluginLogger(this);
						reporter = new DetailedErrorReporter(this);
						UpdateCheck = getConfig().getBoolean("plugin_settings.auto_update_check", true);
						debug = getConfig().getBoolean("plugin_settings.debug", false);
						daLang = getConfig().getString("plugin_settings.lang", "en_US");
						lang2 = new Translator(daLang, getDataFolder().toString());
						THIS_NAME = this.getDescription().getName();
						THIS_VERSION = this.getDescription().getVersion();
						colorful_console = getConfig().getBoolean("plugin_settings.colorful_console", true);
						try {
							config.load(new File(getDataFolder(), "config.yml"));
						} catch (Exception exception) {
							reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_LOAD_CONFIG).error(exception));
						}
						world_whitelist = config.getString("world.whitelist", "");
						world_blacklist = config.getString("world.blacklist", "");
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("jgow.message.reloaded" + ""));
					}else if(!sender.hasPermission(perm)){
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("jgow.message.noperm" + "").toString().replace("<perm>", perm) );
					}
				}
				if(args[0].equalsIgnoreCase("toggledebug")||args[0].equalsIgnoreCase("td")){
					String perm = "dde.op";
					if(sender.isOp()||sender.hasPermission(perm)||!(sender instanceof Player)){
						debug = !debug;
						String debugTrueMessage = get("jgow.message.debugtrue", "Debug set to <boolean>. [error:default]");
						String booleanValue = "jgow.message.boolean." + (debug ? "true" : "false");
						String booleanMessage = get(booleanValue, (debug ? "true" : "false"));
						String theMessage = debugTrueMessage.replace("<boolean>", (debug ? ChatColor.GREEN : ChatColor.RED) + booleanMessage );
						LOGGER.log("debugTrueMessage=" + debugTrueMessage);
						LOGGER.log("booleanValue=" + booleanValue);
						LOGGER.log("booleanMessage=" + booleanMessage);
						LOGGER.log("theMessage=" + theMessage);

						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.WHITE + " " + theMessage );
						//get("jgow.message.debugtrue").toString().replace("<boolean>", (debug ? ChatColor.GREEN : ChatColor.RED) + get("jgow.message.boolean." + (debug ? "true" : "false") ) ));
						return true;
					}else if(!sender.hasPermission(perm)){
						sender.sendMessage(ChatColor.YELLOW + THIS_NAME + ChatColor.RED + " " + get("jgow.message.noperm" + "").toString().replace("<perm>", perm) );
						return false;
					}
				}
			}
		}catch(Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.ERROR_PARSING_COMMAND).error(exception));
			// ERROR_RUNNING_DRAGON_DEATH_COMMAND "Error running command after dragon death."
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) { // TODO: Tab Complete
		try {
			if (command.getName().equalsIgnoreCase("DDE")) {
				List<String> autoCompletes = new ArrayList<>(); //create a new string list for tab completion
				if (args.length == 1) { // reload, toggledebug, playerheads, customtrader, headfix
					autoCompletes.add("reload");
					autoCompletes.add("toggledebug");
					return autoCompletes; // then return the list
				}
			}
		}catch(Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_TAB_COMPLETE_ERROR).error(exception));
			// ERROR_RUNNING_DRAGON_DEATH_COMMAND "Error running command after dragon death."
		}
		return null;
	}

	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event){ //TODO: onPlayerJoinEvent
		Player player = event.getPlayer();
		try {
			if(UpdateAvailable&&(player.isOp()||player.hasPermission("dde.showUpdateAvailable"))){
				String links = "[\"\",{\"text\":\"<Download>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/history\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\" \",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<please_update>\"}},{\"text\":\"| \"},{\"text\":\"<Donate>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://ko-fi.com/joelgodofwar\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Donate_msg>\"}},{\"text\":\" | \"},{\"text\":\"<Notes>\",\"bold\":true,\"color\":\"gold\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"<DownloadLink>/updates\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":\"<Notes_msg>\"}}]";
				links = links.replace("<DownloadLink>", DownloadLink).replace("<Download>", get("jgow.version.download"))
						.replace("<Donate>", get("jgow.version.donate")).replace("<please_update>", get("jgow.version.please_update"))
						.replace("<Donate_msg>", get("jgow.version.donate.message")).replace("<Notes>", get("jgow.version.notes"))
						.replace("<Notes_msg>", get("jgow.version.notes.message"));
				String versions = "" + ChatColor.GRAY + get("jgow.version.new_vers") + ": " + ChatColor.GREEN + "{nVers} | " + get("jgow.version.old_vers") + ": " + ChatColor.RED + "{oVers}";
				player.sendMessage("" + ChatColor.GRAY + get("jgow.version.message").toString().replace("<MyPlugin>", ChatColor.GOLD + THIS_NAME + ChatColor.GRAY) );
				Utils.sendJson(player, links);
				player.sendMessage(versions.replace("{nVers}", UCnewVers).replace("{oVers}", UColdVers));
			}
		}catch(Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_UPDATE_PLUGIN).error(exception));
		}
		if(player.getDisplayName().equals("JoelYahwehOfWar")||player.getDisplayName().equals("JoelGodOfWar")){
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
		LOGGER.debug("dropIt chance=" + chance);
		//double chancepercent = getConfig().getDouble("chancepercent", 0.25);
		LOGGER.debug("dropIt chancepercent=" + chancepercent);
		chancepercent = chancepercent + enchantmentlevelpercent;
		if((chancepercent > 0.00) && (chancepercent <= 1.00)){
			if (chancepercent > chance){
				return true;
			}else{
				return false;
			}
		}else{
			event.getEntity().getKiller().sendMessage("" + get("jgow.message.chancepercentwrong" + "") + " " + get("jgow.message.notifyserveradmin" + ""));
			LOGGER.log(ChatColor.RED + "" + get("jgow.message.chancepercentwrong" + ""));
		}
		return false;
	}

	@EventHandler
	public void onBlockDropItemEvent(BlockDropItemEvent event) {
		@Nonnull BlockState blockState = event.getBlockState();
		if (blockState.getType() != Material.DRAGON_EGG) {
			return;
		}
		LOGGER.debug("BDIE Block is dragon egg.");
		if(blockState.getWorld().getEnvironment().equals(Environment.THE_END)) {
			if(getConfig().getBoolean("do_what.prevent_dragon_egg_spawn", false)) {
				Location block2 = blockState.getWorld().getHighestBlockAt(0, 0).getLocation();
				for (Item item: event.getItems()) { // Ideally should only be one...
					@Nonnull ItemStack itemstack = item.getItemStack();
					if (itemstack.getType() == Material.DRAGON_EGG) {
						LOGGER.debug("BDIE Item dropped is dragon egg.");
						if(blockState.getLocation().distance(block2) <= 10) {
							LOGGER.debug("BDIE item was dropped within 10 blocks of 0 0.");
							itemstack.setType(Material.AIR);
							blockState.setType(Material.AIR);
							LOGGER.debug("BDIE Dragon Egg at 0 0  has been set to Air.");
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
			if( (((loc.getX() == 1)&&(loc.getZ() == 0))||((loc.getX() == 0)&&(loc.getZ() == 1))||((loc.getX() == -1)&&(loc.getZ() == 0))||((loc.getX() == 0)&&(loc.getZ() == -1)))
					|| (loc.distance(block2) < 16) ) {
				LOGGER.debug("BFTE Piston blocked from extending.");
				event.setCancelled(true);
				// /-1 0, /1 0, 0 -1, /0 1
			}
		}
	}

	@EventHandler //
	public void onBlockMove(BlockFromToEvent event) {
		Block block = event.getBlock();
		if(block.getWorld().getEnvironment().equals(Environment.THE_END)) {
			LOGGER.debug("BFTE env=the_end");
			if(getConfig().getBoolean("do_what.prevent_dragon_egg_spawn", false)) {
				Location block2 = block.getWorld().getHighestBlockAt(0, 0).getLocation();
				if(block.getType().equals(Material.DRAGON_EGG)&&(block.getLocation().distance(block2) <= 10)) {
					block.getDrops().clear();
					block.setType(Material.AIR);
					LOGGER.debug("BFTE Dragon Egg at 0 0  has been set to Air.");
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
				if(block.getType().equals(Material.DRAGON_EGG)&&(block.getLocation().distance(block2) <= 10)) {
					block.setType(Material.AIR);
					LOGGER.debug("BBE Dragon Egg at 0 0  has been set to Air.");
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
			if ((getServer().getPluginManager().getPlugin(requiredPlugin) != null) && getServer().getPluginManager().isPluginEnabled(requiredPlugin)) {
				if (requiredPlugin.equals(pluginName)) {
					return true;
				} else {
					return false;
				}
			}
		}
		return true;
	}

	// Used to check Minecraft version
	private MinecraftVersion verifyMinecraftVersion() {
		MinecraftVersion minimum = new MinecraftVersion(PluginLibrary.MINIMUM_MINECRAFT_VERSION);
		MinecraftVersion maximum = new MinecraftVersion(PluginLibrary.MAXIMUM_MINECRAFT_VERSION);

		try {
			MinecraftVersion current = new MinecraftVersion(this.getServer());

			// We'll just warn the user for now
			if (current.compareTo(minimum) < 0) {
				LOGGER.warn("Version " + current + " is lower than the minimum " + minimum);
			}
			if (current.compareTo(maximum) > 0) {
				LOGGER.warn("Version " + current + " has not yet been tested! Proceed with caution.");
			}

			return current;
		} catch (Exception exception) {
			reporter.reportDetailed(this, Report.newBuilder(PluginLibrary.REPORT_CANNOT_PARSE_MINECRAFT_VERSION).error(exception).messageParam(maximum));

			// Unknown version - just assume it is the latest
			return maximum;
		}
	}

	public String getjarfilename() {
		return jarfilename;
	}

	public boolean getDebug() {
		return debug;
	}

}