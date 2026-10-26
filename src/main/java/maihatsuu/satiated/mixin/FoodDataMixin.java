package maihatsuu.satiated.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The hunger bar no longer exists: food stays pinned at full and never drains, regenerates or starves. */
@Mixin(FoodData.class)
public class FoodDataMixin {
	@Shadow
	private int foodLevel;

	@Shadow
	private float exhaustionLevel;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void satiated$disableTick(Player player, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "eat(IF)V", at = @At("HEAD"), cancellable = true)
	private void satiated$disableEat(int nutrition, float saturationModifier, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V", at = @At("HEAD"), cancellable = true)
	private void satiated$disableEatFood(FoodProperties food, CallbackInfo ci) {
		ci.cancel();
	}

	// Normalizes players saved before the mod was installed.
	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void satiated$pinFoodLevel(CompoundTag tag, CallbackInfo ci) {
		this.foodLevel = 20;
		this.exhaustionLevel = 0.0F;
	}
}
