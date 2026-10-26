package maihatsuu.satiated.mixin;

import java.util.List;

import maihatsuu.satiated.food.SatiatedFoods;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
	// Food is always edible; vanilla effect merging keeps whichever Satiated is stronger.
	@Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
	private void satiated$alwaysEdible(boolean canAlwaysEat, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

	@Inject(method = "eat", at = @At("HEAD"))
	private void satiated$grantSatiated(Level level, ItemStack stack, FoodProperties food, CallbackInfoReturnable<ItemStack> cir) {
		SatiatedFoods.apply((Player) (Object) this, SatiatedFoods.resolve(stack.getItem(), food));
	}

	// Drops the food's vanilla effects before LivingEntity.eat applies them, when the table entry asks for it.
	@ModifyArg(
		method = "eat",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;"
		),
		index = 2
	)
	private FoodProperties satiated$replaceVanillaEffects(Level level, ItemStack stack, FoodProperties food) {
		if (!SatiatedFoods.resolve(stack.getItem(), food).replaceVanillaEffects()) {
			return food;
		}

		return new FoodProperties(food.nutrition(), food.saturation(), food.canAlwaysEat(), food.eatSeconds(), food.usingConvertsTo(), List.of());
	}
}
