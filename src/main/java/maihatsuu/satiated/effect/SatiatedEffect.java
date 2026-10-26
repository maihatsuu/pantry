package maihatsuu.satiated.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Replaces the hunger bar: heals 1 HP every {@link #HEAL_INTERVALS} ticks, faster at higher tiers.
 * Tier 1 is amplifier 0.
 */
public class SatiatedEffect extends MobEffect {
	// Ticks between heals, indexed by amplifier. Higher amplifiers use the last value.
	private static final int[] HEAL_INTERVALS = {200, 100, 20};

	public SatiatedEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xE8A23A);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity.getHealth() < entity.getMaxHealth()) {
			entity.heal(1.0F);
		}

		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		int interval = HEAL_INTERVALS[Math.min(amplifier, HEAL_INTERVALS.length - 1)];
		return duration % interval == 0;
	}

	/** Shortens the active Satiated effect by {@code ticks}, never removing it outright. */
	public static void drain(LivingEntity entity, int ticks) {
		MobEffectInstance current = entity.getEffect(ModEffects.SATIATED);
		if (current == null || current.isInfiniteDuration()) {
			return;
		}

		entity.forceAddEffect(withDuration(current, Math.max(1, current.getDuration() - ticks)), null);
	}

	/** Lengthens the active Satiated effect by {@code ticks}, or starts a tier 1 effect if none is active. */
	public static void extend(LivingEntity entity, int ticks) {
		MobEffectInstance current = entity.getEffect(ModEffects.SATIATED);
		if (current == null) {
			entity.addEffect(new MobEffectInstance(ModEffects.SATIATED, ticks, 0));
		} else if (!current.isInfiniteDuration()) {
			entity.forceAddEffect(withDuration(current, current.getDuration() + ticks), null);
		}
	}

	// forceAddEffect replaces the map value in place, which is safe while effects are being ticked.
	private static MobEffectInstance withDuration(MobEffectInstance current, int duration) {
		return new MobEffectInstance(
			ModEffects.SATIATED, duration, current.getAmplifier(), current.isAmbient(), current.isVisible(), current.showIcon()
		);
	}
}
