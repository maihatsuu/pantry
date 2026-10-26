package maihatsuu.satiated.food;

import java.util.List;

import net.minecraft.world.food.FoodProperties;

/**
 * What eating one item grants, after resolving its group, per-item row and fallback.
 *
 * @param tier                  Satiated tier, starting at 1 (amplifier 0)
 * @param duration              Satiated duration in ticks
 * @param effects               bonus effects
 * @param replaceVanillaEffects if true, the food's vanilla effects are not applied
 */
public record SatiatedFoodEntry(int tier, int duration, List<FoodProperties.PossibleEffect> effects, boolean replaceVanillaEffects) {
}
