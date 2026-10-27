package maihatsuu.satiated.exhaustion;

import java.util.Map;

import com.mojang.serialization.Codec;
import maihatsuu.satiated.Satiated;
import maihatsuu.satiated.data.MergedJsonTable;
import net.minecraft.resources.ResourceLocation;

/**
 * When natural regeneration runs and how fast, from {@code data/satiated/regeneration.json}. Hunger values are
 * in hunger points (20 = full bar), intervals in ticks. Settings missing from the file keep their vanilla value.
 * Server-only, like {@link ExhaustionCosts}.
 */
public final class Regeneration {
	public static final MergedJsonTable<Integer> TABLE = new MergedJsonTable<>(
		Satiated.id("regeneration.json"), "settings", Codec.intRange(0, Integer.MAX_VALUE)
	);

	/** Fast regen (paid from saturation) needs at least this much hunger. */
	public static final String FAST_MIN_HUNGER = "fast_regen_min_hunger";
	/** Ticks between fast-regen heals. */
	public static final String FAST_INTERVAL = "fast_regen_interval";
	/** Slow regen (half a heart at a time) needs at least this much hunger. */
	public static final String SLOW_MIN_HUNGER = "slow_regen_min_hunger";
	/** Ticks between slow-regen heals. */
	public static final String SLOW_INTERVAL = "slow_regen_interval";

	public static final Map<String, Integer> VANILLA = Map.of(
		FAST_MIN_HUNGER, 20,
		FAST_INTERVAL, 10,
		SLOW_MIN_HUNGER, 18,
		SLOW_INTERVAL, 80
	);

	private Regeneration() {
	}

	public static int get(String setting) {
		Integer value = TABLE.entries().get(ResourceLocation.withDefaultNamespace(setting));
		return value != null ? value : VANILLA.get(setting);
	}

	/** Intervals are clamped to at least one tick, so a 0 can't stall the timer. */
	public static int interval(String setting) {
		return Math.max(1, get(setting));
	}
}
