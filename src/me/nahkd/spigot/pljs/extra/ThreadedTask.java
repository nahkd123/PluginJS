package me.nahkd.spigot.pljs.extra;

import me.nahkd.spigot.pljs.JSObjectConsole;
import me.nahkd.spigot.pljs.PluginJSMain;

public abstract class ThreadedTask extends Thread {
	
	public abstract void inner();
	
	public void run(String name) {
		Thread newThread = new Thread(new Runnable() {
			@Override
			public void run() {
				inner();
			}
		});
		try {
			newThread.start();
		} catch (IllegalThreadStateException e) {
			new JSObjectConsole("Thread").log("Thread " + name + "already started");
		}
		PluginJSMain.threadedTasks.put(name, this);
		new JSObjectConsole("Thread").log("Thread " + name + " has been started");
	}
	
}
