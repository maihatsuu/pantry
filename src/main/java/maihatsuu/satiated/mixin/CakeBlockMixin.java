package maihatsuu.satiated.mixin;

import maihatsuu.satiated.food.SatiatedFoods;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Cake slices bypass Player.eat, so grant Satiated where the slice feeds the player (keyed as minecraft:cake). */
@Mixin(CakeBlock.class)
public class CakeBlockMixin {
	@Inject(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
	private static void satiated$grantSatiated(LevelAccessor level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<InteractionResult> cir) {
		SatiatedFoods.apply(player, SatiatedFoods.resolveCakeSlice());
	}
}
