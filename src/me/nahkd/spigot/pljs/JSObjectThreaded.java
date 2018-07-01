package me.nahkd.spigot.pljs;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import me.nahkd.spigot.pljs.extra.ThreadedTask;

public class JSObjectThreaded extends JSObject {
	
	Map<String, ThreadedTask> tasks;

	public JSObjectThreaded(String name) {
		super(name);
		tasks = new HashMap<>();
	}
	
	public <T> void newThread(T f) {
		Field[] ff = f.getClass().getDeclaredFields();
		for (Field fi : ff) {
			System.out.println(fi.getName());
			fi.setAccessible(true);
			try {
				System.out.println(fi.get(f));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fi.setAccessible(false);
		}
	}

}
