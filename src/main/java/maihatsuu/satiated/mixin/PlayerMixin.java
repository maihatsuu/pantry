package maihatsuu.satiated.mixin;

import maihatsuu.satiated.food.SatiatedFoods;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Grants food bonus effects. */
@Mixin(Player.class)
public class PlayerMixin {
	@Inject(method = "eat", at = @At("HEAD"))
	private void satiated$grantBonusEffects(Level level, ItemStack stack, FoodProperties food, CallbackInfoReturnable<ItemStack> cir) {
		SatiatedFoods.apply((Player) (Object) this, stack.getItem());
	}
}
