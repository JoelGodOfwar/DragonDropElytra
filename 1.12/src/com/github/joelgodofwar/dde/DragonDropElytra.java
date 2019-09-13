package com.github.joelgodofwar.dde;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import com.github.joelgodofwar.dde.api.Ansi;
import com.github.joelgodofwar.dde.api.ConfigAPI;

public class DragonDropElytra  extends JavaPlugin implements Listener{
	
	public final static Logger logger = Logger.getLogger("Minecraft");
	public static boolean UpdateCheck;
	public static boolean debug;
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public String updateurl = "https://raw.githubusercontent.com/JoelGodOfwar/DragonDropElytra/master/versions/1.12/version.txt";
	
	@Override // TODO: onEnable
	public void onEnable(){
		
		/** DEV check **/
		File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			//log("jarfile contains dev, debug set to true.");
		}
		PluginDescriptionFile pdfFile = this.getDescription();
		String[] serverversion;
		serverversion = getVersion().split("\\.");
		if(debug){debuglog("getVersion = " + getVersion());}
		if(debug){debuglog("serverversion = " + serverversion.length);}
		for (int i = 0; i < serverversion.length; i++)
            log(serverversion[i] + " i=" + i);
		if (!(Integer.parseInt(serverversion[1]) >= 9)){
			
		//if(!getVersion().contains("1.9")&&!getVersion().contains("1.10")&&!getVersion().contains("1.11")){
			logger.info(Ansi.RED + "WARNING!" + Ansi.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + Ansi.SANE);
			logger.info(Ansi.RED + "WARNING! " + Ansi.YELLOW + "Server is NOT version 1.9.*+" + Ansi.SANE);
			logger.info(Ansi.RED + "WARNING! " + Ansi.YELLOW + pdfFile.getName() + " v" + pdfFile.getVersion() + " disabling." + Ansi.SANE);
			logger.info(Ansi.RED + "WARNING!" + Ansi.GREEN + "*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!*!" + Ansi.SANE);
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		ConfigAPI.CheckForConfig(this);
		consoleInfo("enabled");
		getServer().getPluginManager().registerEvents(this, this);
		
		String varCheck = getConfig().getString("auto-update-check");
		String varCheck2 = getConfig().getString("debug");
		String varCheck3 = getConfig().getString("droploc");
		String varCheck4 = getConfig().getString("drop-on-ground");
		String varCheck5 = getConfig().getString("place-in-chest");
		String varCheck6 = getConfig().getString("give-to-player");
		if(varCheck.contains("default")){
			getConfig().set("auto-update-check", true);
		}
		if(!debug){
			if(varCheck2.contains("default")){
				getConfig().set("debug", false);
			}
		}
		if(varCheck3.contains("default")){
			getConfig().set("droploc", "0, 62, 4");
		}
		if(varCheck4.contains("default")){
			getConfig().set("drop-on-ground", false);
		}
		if(varCheck5.contains("default")){
			getConfig().set("place-in-chest", false);
		}
		if(varCheck6.contains("default")){
			getConfig().set("give-to-player", true);
		}
		saveConfig();
		ConfigAPI.Reloadconfig(this);
		
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		}catch (Exception e){
			// Failed to submit the stats
		}
		
		/** DEV check **/
		//File jarfile = this.getFile().getAbsoluteFile();
		if(jarfile.toString().contains("-DEV")){
			debug = true;
			log("jarfile contains dev, debug set to true.");
		}
		
	}
	
	@Override // TODO: onDisable
	public void onDisable(){
		consoleInfo("disabled");
	}
	
	public void consoleInfo(String state) {
		PluginDescriptionFile pdfFile = this.getDescription();
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.SANE);
		logger.info(Ansi.GREEN + pdfFile.getName() + " v" + pdfFile.getVersion() + Ansi.SANE + " is " + state);
		logger.info(Ansi.YELLOW + "**************************************" + Ansi.SANE);
	}
	
	public  void debuglog(String dalog){
		log(Ansi.RED + "[DEBUG] " + Ansi.SANE + dalog);
	}
	
	
	public  void log(String dalog){
		Bukkit.getLogger().info(Ansi.GREEN + this.getName() + " " + Ansi.SANE  + dalog);
	}
	
	@SuppressWarnings("unused")
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent event){
		if(event.getEntity() instanceof EnderDragon){
			LivingEntity daDragon = event.getEntity();
			Player daKiller = daDragon.getKiller();
			ItemStack items = new ItemStack(Material.ELYTRA, 1);
			World world = event.getEntity().getWorld();
			if(ConfigAPI.GetConfigBool(this, "give-to-player")){
				if(daKiller.getInventory().firstEmpty() == -1){
					final Location dropLocation = daKiller.getLocation();
					final Item dropped = world.dropItem(dropLocation, new ItemStack(Material.ELYTRA, 1));
					Player player = daDragon.getKiller();
					Location playerLoc = player.getLocation();
					daKiller.sendMessage("Inventory Full!");
					daKiller.sendMessage("Elytra dropped at  x:" + playerLoc.getBlockX() + " , y:" + playerLoc.getBlockY() + " , z:" + playerLoc.getBlockZ());
				}else{
					daKiller.getInventory().addItem(items);
					daKiller.sendMessage("Elytra has been added to your inventory.");
				}
			}
			
			String[] loc = ConfigAPI.GetConfigStr(this, "droploc").split(",");
			double x;
			double y;
			double z;
			x = Double.parseDouble(loc[0]);
			y = Double.parseDouble(loc[1]);
			z = Double.parseDouble(loc[2]);
			if(ConfigAPI.GetConfigBool(this, "drop-on-ground")){
				final Location dropLocation = new Location(world, x, y, z);
				final Item dropped = world.dropItem(dropLocation, new ItemStack(Material.ELYTRA, 1));
				daKiller.sendMessage("Elytra dropped at  x:" + dropLocation.getBlockX() + " , y:" + dropLocation.getBlockY() + " , z:" + dropLocation.getBlockZ());
			}
			if(ConfigAPI.GetConfigBool(this, "place-in-chest")){
				Location chestLocation = new Location(world, x, y, z);
				if(!(chestLocation.getBlock() instanceof Chest))
	            {
					chestLocation.getBlock().setType(Material.CHEST);
	            }
				try{
					Chest blockChest = (Chest) chestLocation.getBlock().getState();
					blockChest.getInventory().setItem(blockChest.getInventory().firstEmpty(), items);
				}catch (Exception e){
					e.printStackTrace();
				}
				daKiller.sendMessage("Elytra placed in chest at x:" + chestLocation.getBlockX() + " , y:" + chestLocation.getBlockY() + " , z:" + chestLocation.getBlockZ());
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	{
	    Player p = event.getPlayer();
	    if(p.isOp() && UpdateCheck){	
			try {
			
				URL url = new URL(updateurl);
				final URLConnection conn = url.openConnection();
	            conn.setConnectTimeout(5000);
	            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            final String response = reader.readLine();
	            final String localVersion = this.getDescription().getVersion();
	            if(debug){debuglog("response= ." + response + ".");} //TODO: Logger
	            if(debug){debuglog("localVersion= ." + localVersion + ".");} //TODO: Logger
	            if (!response.equalsIgnoreCase(localVersion)) {
					p.sendMessage(ChatColor.DARK_PURPLE + this.getName() + ChatColor.RED + " New version available!");
				}
			} catch (MalformedURLException e) {
				log("MalformedURLException");
				e.printStackTrace();
			} catch (IOException e) {
				log("IOException");
				e.printStackTrace();
			}catch (Exception e) {
				log("Exception");
				e.printStackTrace();
			}
		}
	    if(p.getDisplayName().equals("JoelYahwehOfWar")){
	    	p.sendMessage(this.getName() + " " + this.getDescription().getVersion() + " Hello father!");
	    }
	}
	
	public static String getVersion() {
		String strVersion = Bukkit.getVersion();
		strVersion = strVersion.substring(strVersion.indexOf("MC: "), strVersion.length());
		strVersion = strVersion.replace("MC: ", "").replace(")", "");
		return strVersion;
	}
}