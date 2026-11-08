package maihatsuu.pantry;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Feature toggles, saved to config/pantry.json. Everything defaults to on except longer potions.
 * Item component settings (eat when full, fast snacks, bigger stacks, Golden Carrot nerf) are applied
 * once at startup, so they need a restart and have to match between client and server.
 */
public final class PantryConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(Pantry.MOD_ID + ".json");

	private static PantryConfig instance;

	public boolean foodBonuses = true;
	public boolean foodEffectTooltips = true;
	public boolean eatWhenFull = true;
	public boolean fastSnacks = true;
	public boolean biggerStacks = true;
	public boolean longerPotions = false;
	public boolean goldenCarrotNerf = true;

	public static PantryConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	private static PantryConfig load() {
		PantryConfig config = null;
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				config = GSON.fromJson(reader, PantryConfig.class);
			} catch (IOException | JsonParseException e) {
				Pantry.LOGGER.error("Couldn't read {}, using defaults", PATH, e);
			}
		}
		if (config == null) {
			config = new PantryConfig();
		}
		// Writes back any options missing from an older file.
		config.save();
		return config;
	}

	public void save() {
		try (Writer writer = Files.newBufferedWriter(PATH)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			Pantry.LOGGER.error("Couldn't save {}", PATH, e);
		}
	}
}
