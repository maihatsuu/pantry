package maihatsuu.satiated.food;

import maihatsuu.satiated.Satiated;
import maihatsuu.satiated.data.MergedJsonTable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/** Per-food bonus effects from {@code data/satiated/foods.json}. Cake is keyed as {@code minecraft:cake} and applies per slice. */
public final class SatiatedFoods {
	public static final MergedJsonTable<FoodItemEntry> TABLE = new MergedJsonTable<>(
		Satiated.id("foods.json"), "items", FoodItemEntry.CODEC
	);

	private SatiatedFoods() {
	}

	@Nullable
	public static FoodItemEntry bonus(Item item) {
		return TABLE.entries().get(BuiltInRegistries.ITEM.getKey(item));
	}

	/** Rolls the entry's bonus effects. */
	public static void apply(Player player, FoodItemEntry entry) {
		if (player.level().isClientSide()) {
			return;
		}

		for (FoodProperties.PossibleEffect effect : entry.effects()) {
			if (player.getRandom().nextFloat() < effect.probability()) {
				player.addEffect(effect.effect());
			}
		}
	}
}
