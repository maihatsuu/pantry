package maihatsuu.satiated.mixin;

import maihatsuu.satiated.exhaustion.ExhaustionCosts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Exhaustion cost of the Hunger effect, per tick per level. HungerMobEffect is package-private, so it's targeted by name. */
@Mixin(targets = "net.minecraft.world.effect.HungerMobEffect")
public class HungerMobEffectMixin {
	@ModifyArg(
		method = "applyEffectTick",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V")
	)
	private float satiated$hungerEffect(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.HUNGER_EFFECT, exhaustion);
	}
}
