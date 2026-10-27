package maihatsuu.satiated.food;

import java.util.List;

import maihatsuu.satiated.healing.HealingQueue;
import maihatsuu.satiated.healing.ModAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class SatiatedFoods {
	public static final FoodTable TABLE = new FoodTable();

	// Cake isn't a food item; vanilla feeds this much per slice in CakeBlock.eat.
	private static final int CAKE_SLICE_NUTRITION = 2;

	private SatiatedFoods() {
	}

	/**
	 * What eating {@code item} grants. Speed and healing come from the item's own row if set,
	 * then its group, then a fallback computed from its vanilla food values.
	 */
	public static SatiatedFoodEntry resolve(Item item, int nutrition) {
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
		FoodItemEntry row = TABLE.item(id);
		FoodGroup group = TABLE.group(item, id);
		SatiatedFoodEntry fallback = group == null ? fallback(nutrition) : null;

		int speed = row != null && row.speed().isPresent() ? row.speed().get() : group != null ? group.speed() : fallback.speed();
		int healing = row != null && row.healing().isPresent() ? row.healing().get() : group != null ? group.healing() : fallback.healing();
		return row == null
			? new SatiatedFoodEntry(speed, healing, List.of(), false)
			: new SatiatedFoodEntry(speed, healing, row.effects(), row.replaceVanillaEffects());
	}

	public static SatiatedFoodEntry resolve(Item item, FoodProperties food) {
		return resolve(item, food.nutrition());
	}

	/** The entry for one cake slice, keyed as {@code minecraft:cake}. */
	public static SatiatedFoodEntry resolveCakeSlice() {
		return resolve(Items.CAKE, CAKE_SLICE_NUTRITION);
	}

	/** For foods in no group: heals its nutrition in HP, faster for more filling foods. */
	public static SatiatedFoodEntry fallback(int nutrition) {
		int speed = nutrition >= 8 ? 3 : nutrition >= 5 ? 2 : 1;
		return new SatiatedFoodEntry(speed, Math.max(1, nutrition), List.of(), false);
	}

	/** Replaces the player's healing queue with this food's, then rolls its bonus effects. */
	public static void apply(Player player, SatiatedFoodEntry entry) {
		if (player.level().isClientSide()) {
			return;
		}

		player.setAttached(ModAttachments.HEALING_QUEUE, HealingQueue.of(entry.healing(), entry.speed()));
		for (FoodProperties.PossibleEffect effect : entry.effects()) {
			if (player.getRandom().nextFloat() < effect.probability()) {
				player.addEffect(effect.effect());
			}
		}
	}
}
