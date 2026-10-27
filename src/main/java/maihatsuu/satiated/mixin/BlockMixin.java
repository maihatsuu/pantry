package maihatsuu.satiated.mixin;

import maihatsuu.satiated.exhaustion.ExhaustionCosts;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Exhaustion cost of breaking a block. */
@Mixin(Block.class)
public class BlockMixin {
	@ModifyArg(
		method = "playerDestroy",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V")
	)
	private float satiated$mine(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.MINE, exhaustion);
	}
}
