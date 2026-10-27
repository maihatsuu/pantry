package maihatsuu.satiated.exhaustion;

import java.util.Map;

import com.mojang.serialization.Codec;
import maihatsuu.satiated.Satiated;
import maihatsuu.satiated.data.MergedJsonTable;
import net.minecraft.resources.ResourceLocation;

/**
 * How much exhaustion each action costs, from {@code data/satiated/exhaustion.json}. Every 4 exhaustion
 * drains a point of saturation, or of hunger once saturation is empty. Sources missing from the file
 * keep their vanilla cost. Exhaustion is server-authoritative, so this table isn't synced.
 */
public final class ExhaustionCosts {
	public static final MergedJsonTable<Float> TABLE = new MergedJsonTable<>(
		Satiated.id("exhaustion.json"), "costs", Codec.floatRange(0.0F, Float.MAX_VALUE)
	);

	public static final String WALK = "walk";
	public static final String CROUCH = "crouch";
	public static final String SPRINT = "sprint";
	public static final String WADE = "wade";
	public static final String WALK_UNDERWATER = "walk_underwater";
	public static final String SWIM = "swim";
	public static final String JUMP = "jump";
	public static final String SPRINT_JUMP = "sprint_jump";
	public static final String MINE = "mine";
	public static final String ATTACK = "attack";
	public static final String DAMAGE = "damage";
	public static final String HEAL = "heal";
	public static final String HUNGER_EFFECT = "hunger_effect";

	/** Vanilla's cost per unit (meter, jump, block, hit, half-heart healed, or tick per Hunger level). */
	public static final Map<String, Float> VANILLA = Map.ofEntries(
		Map.entry(WALK, 0.0F),
		Map.entry(CROUCH, 0.0F),
		Map.entry(SPRINT, 0.1F),
		Map.entry(WADE, 0.01F),
		Map.entry(WALK_UNDERWATER, 0.01F),
		Map.entry(SWIM, 0.01F),
		Map.entry(JUMP, 0.05F),
		Map.entry(SPRINT_JUMP, 0.2F),
		Map.entry(MINE, 0.005F),
		Map.entry(ATTACK, 0.1F),
		Map.entry(DAMAGE, 0.1F),
		Map.entry(HEAL, 6.0F),
		Map.entry(HUNGER_EFFECT, 0.005F)
	);

	private ExhaustionCosts() {
	}

	public static float cost(String source) {
		// The table keys entries by ResourceLocation, so a bare "sprint" key is read as minecraft:sprint.
		Float cost = TABLE.entries().get(ResourceLocation.withDefaultNamespace(source));
		return cost != null ? cost : VANILLA.get(source);
	}

	/**
	 * Rescales vanilla's exhaustion for {@code source} to the configured cost, keeping vanilla's own scaling
	 * (distance, Hunger level, damage type). Only valid for sources whose vanilla cost isn't 0.
	 */
	public static float scale(String source, float vanillaExhaustion) {
		return vanillaExhaustion * cost(source) / VANILLA.get(source);
	}
}
