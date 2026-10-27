package maihatsuu.satiated.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Saturation heals instantly instead of filling the (removed) hunger bar: half a heart per level
 * for every tick it is active. It doesn't touch the healing queue.
 */
@Mixin(targets = "net.minecraft.world.effect.SaturationMobEffect")
public class SaturationMobEffectMixin {
	@Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
	private void satiated$healInstantly(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
		if (!entity.level().isClientSide() && entity instanceof Player) {
			entity.heal(amplifier + 1);
		}

		cir.setReturnValue(true);
	}
}
