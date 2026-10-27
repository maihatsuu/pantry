package maihatsuu.satiated.mixin;

import java.util.List;

import maihatsuu.satiated.exhaustion.ExhaustionCosts;
import maihatsuu.satiated.food.FoodItemEntry;
import maihatsuu.satiated.food.SatiatedFoods;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Food bonus effects, plus the exhaustion costs of jumping, attacking and taking damage. */
@Mixin(Player.class)
public class PlayerMixin {
	@Unique
	private static final String EXHAUSTION = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V";

	@Inject(method = "eat", at = @At("HEAD"))
	private void satiated$grantBonusEffects(Level level, ItemStack stack, FoodProperties food, CallbackInfoReturnable<ItemStack> cir) {
		FoodItemEntry entry = SatiatedFoods.bonus(stack.getItem());
		if (entry != null) {
			SatiatedFoods.apply((Player) (Object) this, entry);
		}
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
		FoodItemEntry entry = SatiatedFoods.bonus(stack.getItem());
		if (entry == null || !entry.replaceVanillaEffects()) {
			return food;
		}

		return new FoodProperties(food.nutrition(), food.saturation(), food.canAlwaysEat(), food.eatSeconds(), food.usingConvertsTo(), List.of());
	}

	// jumpFromGround calls causeFoodExhaustion for a sprint jump first, then for a normal jump.
	@ModifyArg(method = "jumpFromGround", at = @At(value = "INVOKE", target = EXHAUSTION, ordinal = 0))
	private float satiated$sprintJump(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.SPRINT_JUMP, exhaustion);
	}

	@ModifyArg(method = "jumpFromGround", at = @At(value = "INVOKE", target = EXHAUSTION, ordinal = 1))
	private float satiated$jump(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.JUMP, exhaustion);
	}

	@ModifyArg(method = "attack", at = @At(value = "INVOKE", target = EXHAUSTION))
	private float satiated$attack(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.ATTACK, exhaustion);
	}

	@ModifyArg(method = "actuallyHurt", at = @At(value = "INVOKE", target = EXHAUSTION))
	private float satiated$damage(float exhaustion) {
		return ExhaustionCosts.scale(ExhaustionCosts.DAMAGE, exhaustion);
	}
}
