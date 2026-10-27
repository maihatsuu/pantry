package maihatsuu.satiated.potion;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

/**
 * Longer durations for drinkable beneficial potions: 5 minutes, 10 with redstone, 2.5 with glowstone.
 * Harmful potions, splash, lingering and tipped arrows keep vanilla durations.
 */
public final class PotionDurations {
	private static final int MINUTE = 60 * 20;
	private static final int BASE = 5 * MINUTE;
	private static final int LONG = 10 * MINUTE;
	private static final int STRONG = 5 * MINUTE / 2;

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
	 * The drink duration for a vanilla beneficial potion, picked from its id (long_ and strong_ variants),
	 * or null to keep vanilla's. A potion counts as beneficial if any of its effects is, so Turtle Master does.
	 */
	@Nullable
	private static Integer duration(Holder<Potion> potion) {
		ResourceLocation id = potion.unwrapKey().map(key -> key.location()).orElse(null);
		if (id == null || !id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
			return null;
		}

		boolean beneficial = potion.value().getEffects().stream()
			.anyMatch(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL);
		if (!beneficial) {
			return null;
		}

		String path = id.getPath();
		if (path.startsWith("long_")) {
			return LONG;
		}
		return path.startsWith("strong_") ? STRONG : BASE;
	}
}
