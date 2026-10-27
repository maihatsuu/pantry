package maihatsuu.satiated.food;

import java.util.List;
import java.util.Map;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Bonus effects granted by certain foods, on top of their vanilla effects. Cake applies per slice. */
public final class SatiatedFoods {
	private static final int SECONDS = 20;

	private static final Map<Item, List<MobEffectInstance>> BONUSES = Map.of(
		Items.PUMPKIN_PIE, List.of(new MobEffectInstance(MobEffects.DIG_SPEED, 30 * SECONDS, 1)),
		Items.CAKE, List.of(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * SECONDS, 0)),
		Items.COOKIE, List.of(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * SECONDS, 1)),
		Items.GLOW_BERRIES, List.of(new MobEffectInstance(MobEffects.GLOWING, 60 * SECONDS, 0)),
		Items.RABBIT_STEW, List.of(new MobEffectInstance(MobEffects.LUCK, 30 * SECONDS, 1)),
		Items.GOLDEN_CARROT, List.of(new MobEffectInstance(MobEffects.NIGHT_VISION, 60 * SECONDS, 0))
	);

	private SatiatedFoods() {
	}

	public static List<MobEffectInstance> bonus(Item item) {
		return BONUSES.getOrDefault(item, List.of());
	}

	public static void apply(Player player, Item item) {
		if (player.level().isClientSide()) {
			return;
		}

		// addEffect keeps the instance it's given, so hand it a copy.
		for (MobEffectInstance effect : bonus(item)) {
			player.addEffect(new MobEffectInstance(effect));
		}
	}
}
