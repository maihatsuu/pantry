package maihatsuu.satiated.food;

import java.util.List;

import net.minecraft.world.food.FoodProperties;

/**
 * What eating one item grants, after resolving its group, per-item row and fallback.
 *
 * @param speed                 heal speed, 1 (slowest) to 3
 * @param healing               healing in HP (half-hearts)
 * @param effects               bonus effects
 * @param replaceVanillaEffects if true, the food's vanilla effects are not applied
 */
public record SatiatedFoodEntry(int speed, int healing, List<FoodProperties.PossibleEffect> effects, boolean replaceVanillaEffects) {
}
