package maihatsuu.satiated.mixin;

import maihatsuu.satiated.exhaustion.ExhaustionCosts;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Exhaustion cost of natural regeneration. Both regen paths in tick (fast while saturated, slow otherwise)
 * cost vanilla 6 exhaustion per HP healed, so one rescale covers both calls.
 */
@Mixin(FoodData.class)
public class FoodDataMixin {
	@ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"))
	private float satiated$heal(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.HEAL, exhaustion);
	}
}
