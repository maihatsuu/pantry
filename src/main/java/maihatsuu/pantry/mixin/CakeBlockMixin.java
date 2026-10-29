package maihatsuu.pantry.mixin;

import net.minecraft.world.level.block.CakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Cake slices bypass the FOOD component, so let them be eaten when full like every other food. */
@Mixin(CakeBlock.class)
public class CakeBlockMixin {
	@ModifyArg(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canEat(Z)Z"))
	private static boolean pantry$alwaysEdible(boolean canAlwaysEat) {
		return true;
	}
}
