package maihatsuu.satiated.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import maihatsuu.satiated.exhaustion.ExhaustionCosts;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Movement exhaustion. checkMovementStatistics calls causeFoodExhaustion in this order: swim, underwater, wade, sprint, crouch, walk. */
@Mixin(ServerPlayer.class)
public class ServerPlayerExhaustionMixin {
	@Unique
	private static final String TARGET = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V";

	@ModifyArg(method = "checkMovementStatistics", at = @At(value = "INVOKE", target = TARGET, ordinal = 0))
	private float satiated$swim(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.SWIM, exhaustion);
	}

	@ModifyArg(method = "checkMovementStatistics", at = @At(value = "INVOKE", target = TARGET, ordinal = 1))
	private float satiated$walkUnderwater(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.WALK_UNDERWATER, exhaustion);
	}

	@ModifyArg(method = "checkMovementStatistics", at = @At(value = "INVOKE", target = TARGET, ordinal = 2))
	private float satiated$wade(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.WADE, exhaustion);
	}

	@ModifyArg(method = "checkMovementStatistics", at = @At(value = "INVOKE", target = TARGET, ordinal = 3))
	private float satiated$sprint(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.SPRINT, exhaustion);
	}

	// Walking and crouching cost 0 in vanilla, so there's nothing to rescale: recompute the distance instead.
	@ModifyArg(method = "checkMovementStatistics", at = @At(value = "INVOKE", target = TARGET, ordinal = 4))
	private float satiated$crouch(float exhaustion, @Local(argsOnly = true, ordinal = 0) double dx, @Local(argsOnly = true, ordinal = 2) double dz) {
		return ExhaustionCosts.cost(ExhaustionCosts.CROUCH) * satiated$groundMeters(dx, dz);
	}

	@ModifyArg(method = "checkMovementStatistics", at = @At(value = "INVOKE", target = TARGET, ordinal = 5))
	private float satiated$walk(float exhaustion, @Local(argsOnly = true, ordinal = 0) double dx, @Local(argsOnly = true, ordinal = 2) double dz) {
		return ExhaustionCosts.cost(ExhaustionCosts.WALK) * satiated$groundMeters(dx, dz);
	}

	// Matches vanilla's rounding: whole centimeters, converted back to meters.
	@Unique
	private static float satiated$groundMeters(double dx, double dz) {
		return Math.round((float) Math.sqrt(dx * dx + dz * dz) * 100.0F) * 0.01F;
	}
}
