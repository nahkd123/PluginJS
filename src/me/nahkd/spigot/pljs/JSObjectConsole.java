package me.nahkd.spigot.pljs;

public class JSObjectConsole extends JSObject {
	
	public int groupLevel;

	public JSObjectConsole(String name) {
		super(name);
		this.groupLevel = 0;
	}
	
	private String getGroupPrefix() {
		String o = "";
		for (int i = 0; i < groupLevel; i++) o = o + " §d|";
		return o;
	}
	
	public void log(String log) {
		PluginJSMain.instance.getServer().getConsoleSender().sendMessage("§2[PluginJS]§1[§9" + name + "§1] §b[Log] " + getGroupPrefix() + "§3" + log);
	}
	
	public void warn(String warn) {
		PluginJSMain.instance.getServer().getConsoleSender().sendMessage("§2[PluginJS]§1[§9" + name + "§1] §e[WARN]" + getGroupPrefix() + "§6" + warn);
	}
	
	public void error(String error) {
		PluginJSMain.instance.getServer().getConsoleSender().sendMessage("§2[PluginJS]§1[§9" + name + "§1]§c[ERROR]" + getGroupPrefix() + error);
	}
	
	public void group() {
		this.groupLevel++;
	}
	
	public void groupEnd() {
		--this.groupLevel;
	}
	
	public void sleep(long millls) {
		try {
			Thread.sleep(millls);
		} catch (InterruptedException e) {
			warn("The sleep method has been interrupted by another thread");
			group();
			warn(e.getMessage());
			groupEnd();
		}
	}

}
