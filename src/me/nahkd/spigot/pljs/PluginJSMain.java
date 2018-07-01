package me.nahkd.spigot.pljs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.nahkd.spigot.pljs.extra.ThreadedTask;

public class PluginJSMain extends JavaPlugin implements Listener {
	
	public static PluginJSMain instance;

	public File scriptsFolder = new File(getDataFolder(), "scripts");
	public File apiFolder = new File(getDataFolder(), "apis");
	
	public Map<String, ScriptEnginePackager> scripts = new HashMap<>();
	
	public static Map<String, ThreadedTask> threadedTasks;
	
	static class FileFilters {
		public static FileFilter scriptFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().endsWith(".js")) return true;
				return false;
			}
		};
	}
	
	class ScriptEnginePackager {
		public ScriptEngineManager m;
		public ScriptEngine e;
		public int so;
		public Invocable i;
		
		public ScriptEnginePackager(ScriptEngineManager m, ScriptEngine e, int so, Invocable i) {
			this.m = m;
			this.e = e;
			this.so = so;
			this.i = i;
		}
	}
	
	@Override
	public void onEnable() {
		
		instance = this;
		threadedTasks = new HashMap<>();
		
		if (!getDataFolder().exists()) getDataFolder().mkdir();
		if (!apiFolder.exists()) apiFolder.mkdir();
		
		if (!scriptsFolder.exists()) {
			getDataFolder().mkdir();
			scriptsFolder.mkdir();
			System.out.println("[PluginJS] Created new folder that contains scripts!");
			System.out.println("[PluginJS] You may have to restart server in order to make it work!");
		} else {
			int startOrder = 0;
			for (File f : scriptsFolder.listFiles(FileFilters.scriptFilter)) {
				ThreadedTask task = new ThreadedTask() {
					@Override
					public void inner() {
						ScriptEngineManager m = new ScriptEngineManager();
//						ScriptEngine e = m.getEngineByName("JavaScript");
						ScriptEngine e = m.getEngineByName("Nashorn");
						try {
							e.eval(Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8));
							Invocable i = (Invocable) e;
							
							File scriptDataFolder = new File(getDataFolder(), "scripts" + File.separatorChar + f.getName());
							scriptDataFolder.mkdir();
							File scriptConfigFile = new File(scriptDataFolder, "config.yml");
							FileConfiguration scriptConfig = YamlConfiguration.loadConfiguration(scriptConfigFile);
							
							e.put("startOrder", startOrder);
							e.put("scriptDataFolder", scriptDataFolder);
							e.put("config", scriptConfig);
							e.put("configFile", scriptConfigFile);
							e.put("PluginScriptHandler", instance);
							e.put("thisThread", this);
							
							//Basic objects
							JSObject console = new JSObjectConsole(f.getName());
							e.put("console", console);
							JSObject threaded = new JSObjectThreaded(f.getName());
							e.put("threads", threaded);
							
							//API load
							for (File fapi : apiFolder.listFiles()) {
								if ((boolean) e.eval("typeof onApiLoad === 'function' ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE")) {
									if ((boolean) i.invokeFunction("onApiLoad", fapi)) {
										e.eval(Files.newBufferedReader(fapi.toPath(), StandardCharsets.UTF_8));
									};
								} else {
									e.eval(Files.newBufferedReader(fapi.toPath(), StandardCharsets.UTF_8));
								}
							}
							
							String scriptName = (String) i.invokeFunction("onEnable");
							
							scripts.put(scriptName, new ScriptEnginePackager(m, e, startOrder, i));
							
						} catch (IOException ex) {
							System.out.println("[PluginJS] Have you enable access for javaw yet?");
							ex.printStackTrace();
						} catch (ScriptException ex) {
							System.out.println("[PluginJS] Script " + f.getName() + " got exception!!!!");
							System.out.println("Error: " + ex.getMessage());
							System.out.println("Error caused at " + ex.getLineNumber() + ":" + ex.getColumnNumber() + " (Line:Column)");
						} catch (NoSuchMethodException ex) {
							System.out.println("[PluginJS] Script " + f.getName() + " got exception!!!!");
							System.out.println("Method 'onEnable' not found!");
						}
					}
				};
				task.run("#" + startOrder);
			}
		}
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void runCommand(PlayerCommandPreprocessEvent e) {
		if (!e.isCancelled()) {
			for (String scriptName : scripts.keySet()) {
				ScriptEnginePackager sep = scripts.get(scriptName);
				try {
					if ((boolean) sep.e.eval("typeof onCommand === 'function' ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE")) {
						if (e.getMessage().split(" ").length <= 1) {
							boolean executed = (boolean) sep.i.invokeFunction("onCommand", e.getMessage().split(" ")[0].replaceFirst("/", ""), "", e.getPlayer());
							if (executed) e.setCancelled(true);
						} else {
							boolean executed = (boolean) sep.i.invokeFunction("onCommand", e.getMessage().split(" ")[0].replaceFirst("/", ""), e.getMessage().substring(e.getMessage().split(" ")[0].length() + 1, e.getMessage().length()), e.getPlayer());
							if (executed) e.setCancelled(true);
						}
					}
				} catch (ScriptException ex) {
					System.out.println("[PluginJS] Script " + scriptName + " got exception!!!!");
					System.out.println("Error: " + ex.getMessage());
					System.out.println("Error caused at " + ex.getLineNumber() + ":" + ex.getColumnNumber() + " (Line:Column)");
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
					System.out.println("lmao I have added checking thing before lol");
				}
			}
		}
	}
}
