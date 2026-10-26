package maihatsuu.satiated.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sprinting is gated on health instead of food. Vanilla uses the same check both to start sprinting
 * and to stop an active sprint, so sprinting ends as soon as health drops to 3 hearts or below.
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Unique
	private static final float MIN_SPRINT_HEALTH = 6.0F;

	@Inject(method = "hasEnoughFoodToStartSprinting", at = @At("HEAD"), cancellable = true)
	private void satiated$sprintByHealth(CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		cir.setReturnValue(player.isPassenger() || player.getHealth() > MIN_SPRINT_HEALTH || player.getAbilities().mayfly);
	}
}
