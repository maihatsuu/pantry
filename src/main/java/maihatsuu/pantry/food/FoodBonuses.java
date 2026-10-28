package maihatsuu.pantry.food;

import java.util.List;
import java.util.Map;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Bonus effects granted by certain foods, on top of their vanilla effects. */
public final class FoodBonuses {
	private static final int SECONDS = 20;
	// Instant bonuses apply at half strength, so Instant Health I heals 1 heart instead of 2.
	private static final double INSTANT_STRENGTH = 0.5;

	private static final Map<Item, List<MobEffectInstance>> BONUSES = Map.of(
		Items.PUMPKIN_PIE, List.of(new MobEffectInstance(MobEffects.DIG_SPEED, 30 * SECONDS, 1)),
		Items.BEETROOT_SOUP, List.of(new MobEffectInstance(MobEffects.ABSORPTION, 30 * SECONDS, 0)),
		Items.COOKIE, List.of(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * SECONDS, 1)),
		Items.GLOW_BERRIES, List.of(new MobEffectInstance(MobEffects.GLOWING, 30 * SECONDS, 0)),
		Items.RABBIT_STEW, List.of(new MobEffectInstance(MobEffects.LUCK, 30 * SECONDS, 1)),
		Items.GOLDEN_CARROT, List.of(new MobEffectInstance(MobEffects.NIGHT_VISION, 30 * SECONDS, 0)),
		Items.MUSHROOM_STEW, List.of(new MobEffectInstance(MobEffects.HEAL, 1, 0)),
		Items.BAKED_POTATO, List.of(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * SECONDS, 0))
	);

	private FoodBonuses() {
	}

	private static List<MobEffectInstance> bonus(Item item) {
		return BONUSES.getOrDefault(item, List.of());
	}

	public static void apply(Player player, Item item) {
		if (player.level().isClientSide()) {
			return;
		}

		for (MobEffectInstance effect : bonus(item)) {
			if (effect.getEffect().value().isInstantenous()) {
				// Like drinking an instant potion: apply now instead of adding a timed effect.
				effect.getEffect().value().applyInstantenousEffect(player, player, player, effect.getAmplifier(), INSTANT_STRENGTH);
			} else {
				// addEffect keeps the instance it's given, so hand it a copy.
				player.addEffect(new MobEffectInstance(effect));
			}
		}
	}
}
