package maihatsuu.pantry.client;

import java.util.function.Consumer;

import maihatsuu.pantry.PantryConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** The Cloth Config screen behind Mod Menu's config button. */
final class PantryConfigScreen {
	private PantryConfigScreen() {
	}

	static Screen create(Screen parent) {
		PantryConfig config = PantryConfig.get();
		PantryConfig defaults = new PantryConfig();
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Component.translatable("config.pantry.title"))
			.setSavingRunnable(config::save);
		ConfigEntryBuilder entries = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.pantry.general"));

		toggle(general, entries, "foodBonuses", config.foodBonuses, defaults.foodBonuses, false, value -> config.foodBonuses = value);
		toggle(general, entries, "foodEffectTooltips", config.foodEffectTooltips, defaults.foodEffectTooltips, false, value -> config.foodEffectTooltips = value);
		toggle(general, entries, "eatWhenFull", config.eatWhenFull, defaults.eatWhenFull, true, value -> config.eatWhenFull = value);
		toggle(general, entries, "fastSnacks", config.fastSnacks, defaults.fastSnacks, true, value -> config.fastSnacks = value);
		toggle(general, entries, "biggerStacks", config.biggerStacks, defaults.biggerStacks, true, value -> config.biggerStacks = value);
		toggle(general, entries, "longerPotions", config.longerPotions, defaults.longerPotions, false, value -> config.longerPotions = value);
		toggle(general, entries, "goldenCarrotNerf", config.goldenCarrotNerf, defaults.goldenCarrotNerf, true, value -> config.goldenCarrotNerf = value);

		return builder.build();
	}

	/** Options that change item components are only read at startup, so they ask for a restart. */
	private static void toggle(
		ConfigCategory category, ConfigEntryBuilder entries, String key, boolean value, boolean defaultValue, boolean needsRestart, Consumer<Boolean> save
	) {
		BooleanListEntry entry = entries.startBooleanToggle(Component.translatable("config.pantry." + key), value)
			.setDefaultValue(defaultValue)
			.setTooltip(Component.translatable("config.pantry." + key + ".tooltip"))
			.setSaveConsumer(save)
			.build();
		entry.setRequiresRestart(needsRestart);
		category.addEntry(entry);
	}
}
