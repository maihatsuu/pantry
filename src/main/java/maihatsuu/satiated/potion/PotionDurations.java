package maihatsuu.satiated.potion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import maihatsuu.satiated.Satiated;
import maihatsuu.satiated.data.MergedJsonTable;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * Durations for drinkable potions only. Splash, lingering and tipped arrows keep vanilla durations.
 */
public final class PotionDurations {
	public static final MergedJsonTable<Integer> TABLE = new MergedJsonTable<>(
		Satiated.id("potion_durations.json"), "potions", ExtraCodecs.POSITIVE_INT
	);

	private PotionDurations() {
	}

	/**
	 * The effects a drinkable potion applies: the base potion's effects with the table duration,
	 * followed by any custom effects unchanged. Instant effects are left alone.
	 */
	public static List<MobEffectInstance> drinkEffects(PotionContents contents) {
		List<MobEffectInstance> effects = new ArrayList<>();
		contents.potion().ifPresent(potion -> {
			Integer duration = lookup(potion);
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

	private static Integer lookup(Holder<Potion> potion) {
		Optional<ResourceKey<Potion>> key = potion.unwrapKey();
		return key.map(k -> TABLE.entries().get(k.location())).orElse(null);
	}
}
