package maihatsuu.satiated.mixin;

import maihatsuu.satiated.effect.SatiatedEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hunger drains Satiated instead of adding exhaustion: one extra second per second, per level. */
@Mixin(targets = "net.minecraft.world.effect.HungerMobEffect")
public class HungerMobEffectMixin {
	@Unique
	private static final int DRAIN_INTERVAL = 20;

	@Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
	private void satiated$drainSatiated(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
		if (!entity.level().isClientSide() && entity.tickCount % DRAIN_INTERVAL == 0) {
			SatiatedEffect.drain(entity, DRAIN_INTERVAL * (amplifier + 1));
		}

		cir.setReturnValue(true);
	}
}
