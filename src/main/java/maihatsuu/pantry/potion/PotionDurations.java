package maihatsuu.pantry.potion;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

/**
 * Longer durations for drinkable potions: 5 minutes, 10 with redstone, and glowstone doesn't shorten them.
 * Turtle Master, splash, lingering and tipped arrows keep vanilla durations.
 */
public final class PotionDurations {
	private static final int MINUTE = 60 * 20;
	private static final int BASE = 5 * MINUTE;
	private static final int LONG = 10 * MINUTE;

	private PotionDurations() {
	}

	/**
	 * The effects a drinkable potion applies: the base potion's effects with the new duration,
	 * followed by any custom effects unchanged. Instant effects are left alone.
	 */
	public static List<MobEffectInstance> drinkEffects(PotionContents contents) {
		List<MobEffectInstance> effects = new ArrayList<>();
		contents.potion().ifPresent(potion -> {
			Integer duration = duration(potion);
			for (MobEffectInstance effect : potion.value().getEffects()) {
				if (duration == null || effect.getEffect().value().isInstantenous()) {
					effects.add(new MobEffectInstance(effect));
				} else {
					effects.add(new MobEffectInstance(
						effect.getEffect(), duration, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()
					));
				}
			}
		});

		for (MobEffectInstance effect : contents.customEffects()) {
			effects.add(new MobEffectInstance(effect));
		}

		return effects;
	}

	/**
	 * The drink duration for a vanilla potion, picked from its id (long_ variants), or null to keep vanilla's.
	 * Turtle Master is excluded, since its Slowness would otherwise last minutes.
	 */
	@Nullable
	private static Integer duration(Holder<Potion> potion) {
		ResourceLocation id = potion.unwrapKey().map(key -> key.location()).orElse(null);
		if (id == null || !id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
			return null;
		}

		String path = id.getPath();
		if (path.endsWith("turtle_master")) {
			return null;
		}
		return path.startsWith("long_") ? LONG : BASE;
	}
}
