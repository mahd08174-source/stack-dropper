package com.stackdropper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Settings saved to config/stack-dropper.json. */
public class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Config instance = new Config();

	/** GLFW key code of the toggle key. Default: V. */
	public int keyCode = GLFW.GLFW_KEY_V;
	/** How many stacks to drop each time. */
	public int stacksPerDrop = 2;
	/** If true, every stack in the inventory is dropped each time. */
	public boolean wholeInventory = false;
	/** Seconds between drops. Default: 0.1 */
	public double intervalSeconds = 0.1;

	public static Config get() {
		return instance;
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("stack-dropper.json");
	}

	public static void load() {
		try {
			Path p = path();
			if (Files.exists(p)) {
				Config loaded = GSON.fromJson(Files.readString(p), Config.class);
				if (loaded != null) {
					instance = loaded;
				}
			}
		} catch (Exception e) {
			instance = new Config();
		}
		instance.clamp();
	}

	public static void save() {
		instance.clamp();
		try {
			Files.writeString(path(), GSON.toJson(instance));
		} catch (IOException ignored) {
		}
	}

	private void clamp() {
		stacksPerDrop = Math.max(1, Math.min(36, stacksPerDrop));
		intervalSeconds = Math.max(0.01, Math.min(600.0, intervalSeconds));
	}
}
