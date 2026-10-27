package maihatsuu.satiated.mixin;

import maihatsuu.satiated.exhaustion.ExhaustionCosts;
import maihatsuu.satiated.exhaustion.Regeneration;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Natural regeneration: its exhaustion cost, the hunger it needs and how often it heals.
 * tick checks, in order: foodLevel >= 20 (fast regen), timer >= 10, foodLevel >= 18 (slow regen), timer >= 80,
 * then timer >= 80 again for starvation, which is left alone.
 */
@Mixin(FoodData.class)
public class FoodDataMixin {
	// Both regen paths cost vanilla 6 exhaustion per HP healed, so one rescale covers both calls.
	@ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"))
	private float satiated$heal(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.HEAL, exhaustion);
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
	private int satiated$fastRegenMinHunger(int vanilla) {
		return Regeneration.get(Regeneration.FAST_MIN_HUNGER);
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 10))
	private int satiated$fastRegenInterval(int vanilla) {
		return Regeneration.interval(Regeneration.FAST_INTERVAL);
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 18))
	private int satiated$slowRegenMinHunger(int vanilla) {
		return Regeneration.get(Regeneration.SLOW_MIN_HUNGER);
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 80, ordinal = 0))
	private int satiated$slowRegenInterval(int vanilla) {
		return Regeneration.interval(Regeneration.SLOW_INTERVAL);
	}
}
