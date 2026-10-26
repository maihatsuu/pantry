package maihatsuu.satiated.mixin;

import maihatsuu.satiated.effect.SatiatedEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Saturation extends Satiated instead of filling the (removed) hunger bar. Applies every tick it is active. */
@Mixin(targets = "net.minecraft.world.effect.SaturationMobEffect")
public class SaturationMobEffectMixin {
	@Unique
	private static final int TICKS_PER_LEVEL = 100;

	@Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
	private void satiated$extendSatiated(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
		if (!entity.level().isClientSide() && entity instanceof Player) {
			SatiatedEffect.extend(entity, TICKS_PER_LEVEL * (amplifier + 1));
		}

		cir.setReturnValue(true);
	}
}
