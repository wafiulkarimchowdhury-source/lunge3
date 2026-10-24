package com.leftclicklunge.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Settings, stored in .minecraft/config/leftclicklunge.json
 * Edit the file while the game is closed.
 */
public final class LungeConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("leftclicklunge.json");
	private static LungeConfig instance = new LungeConfig();

	/** Master switch. This is what the toggle keybind flips. */
	public boolean enabled = true;

	/** The macro only runs when NO player/mob is within this many blocks of you. */
	public double safeRadius = 4.0;

	/** Minimum ticks between lunges while holding left click (20 ticks = 1 second). 5 = bare hand speed. */
	public int repeatIntervalTicks = 5;

	/** Lunge needs enough hunger; the macro stays off below this food level (0-20). */
	public int lowestFoodLevelToLunge = 7;

	/** -1 = automatically use the hotbar spear with the highest Lunge level. 0-8 = force that hotbar slot (0 = first slot). */
	public int fixedSpearSlot = -1;

	/** true = only players and mobs count as "nearby". false = every entity (items, boats, minecarts...) counts. */
	public boolean onlyLivingEntities = true;

	/** Show ON/OFF above the hotbar when you press the toggle key. */
	public boolean actionBarMessages = true;

	public static LungeConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				LungeConfig loaded = GSON.fromJson(reader, LungeConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (Exception e) {
				System.err.println("[LeftClickLunge] Could not read config, using defaults: " + e);
			}
		}
		instance.sanitize();
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			System.err.println("[LeftClickLunge] Could not save config: " + e);
		}
	}

	private void sanitize() {
		if (safeRadius < 0) safeRadius = 0;
		if (repeatIntervalTicks < 1) repeatIntervalTicks = 1;
		if (lowestFoodLevelToLunge < 0) lowestFoodLevelToLunge = 0;
		if (fixedSpearSlot < -1 || fixedSpearSlot > 8) fixedSpearSlot = -1;
	}
}
