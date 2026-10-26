package maihatsuu.satiated.food;

import java.util.List;

import maihatsuu.satiated.effect.ModEffects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class SatiatedFoods {
	public static final FoodTable TABLE = new FoodTable();

	// Ticks of Satiated per point of nutrition + saturation, for foods in no group.
	private static final int FALLBACK_TICKS_PER_POINT = 300;

	// Cake isn't a food item; vanilla feeds these values per slice in CakeBlock.eat.
	private static final int CAKE_SLICE_NUTRITION = 2;
	private static final float CAKE_SLICE_SATURATION_MODIFIER = 0.1F;

	private SatiatedFoods() {
	}

	/**
	 * What eating {@code item} grants. Tier and duration come from the item's own row if set,
	 * then its group, then a fallback computed from its vanilla food values.
	 */
	public static SatiatedFoodEntry resolve(Item item, int nutrition, float saturation) {
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
		FoodItemEntry row = TABLE.item(id);
		FoodGroup group = TABLE.group(item, id);
		SatiatedFoodEntry fallback = group == null ? fallback(nutrition, saturation) : null;

		int tier = row != null && row.tier().isPresent() ? row.tier().get() : group != null ? group.tier() : fallback.tier();
		int duration = row != null && row.duration().isPresent() ? row.duration().get() : group != null ? group.duration() : fallback.duration();
		return row == null
			? new SatiatedFoodEntry(tier, duration, List.of(), false)
			: new SatiatedFoodEntry(tier, duration, row.effects(), row.replaceVanillaEffects());
	}

	public static SatiatedFoodEntry resolve(Item item, FoodProperties food) {
		return resolve(item, food.nutrition(), food.saturation());
	}

	/** The entry for one cake slice, keyed as {@code minecraft:cake}. */
	public static SatiatedFoodEntry resolveCakeSlice() {
		return resolve(Items.CAKE, CAKE_SLICE_NUTRITION, FoodConstants.saturationByModifier(CAKE_SLICE_NUTRITION, CAKE_SLICE_SATURATION_MODIFIER));
	}

	public static SatiatedFoodEntry fallback(int nutrition, float saturation) {
		int tier = nutrition >= 8 ? 3 : nutrition >= 5 ? 2 : 1;
		int duration = Math.max(20, Math.round((nutrition + saturation) * FALLBACK_TICKS_PER_POINT / 20.0F) * 20);
		return new SatiatedFoodEntry(tier, duration, List.of(), false);
	}

	/** Grants Satiated and rolls the entry's bonus effects. Vanilla effect merging keeps the stronger Satiated. */
	public static void apply(Player player, SatiatedFoodEntry entry) {
		if (player.level().isClientSide()) {
			return;
		}

		player.addEffect(new MobEffectInstance(ModEffects.SATIATED, entry.duration(), entry.tier() - 1));
		for (FoodProperties.PossibleEffect effect : entry.effects()) {
			if (player.getRandom().nextFloat() < effect.probability()) {
				player.addEffect(effect.effect());
			}
		}
	}
}
