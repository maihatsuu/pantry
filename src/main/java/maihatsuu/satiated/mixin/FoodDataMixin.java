package maihatsuu.satiated.mixin;

import maihatsuu.satiated.healing.HealingQueue;
import maihatsuu.satiated.healing.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The hunger bar no longer exists: food stays pinned at full and never drains, regenerates or starves.
 * Its tick (server-side only, called from Player.tick) drives the healing queue instead.
 */
@Mixin(FoodData.class)
public class FoodDataMixin {
	@Shadow
	private int foodLevel;

	@Shadow
	private float exhaustionLevel;

	// Not saved: after a relog the current half-heart restarts its interval.
	@Unique
	private int satiated$healTimer;

	@Unique
	private HealingQueue satiated$timedQueue;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void satiated$tickHealingQueue(Player player, CallbackInfo ci) {
		ci.cancel();

		HealingQueue queue = player.getAttached(ModAttachments.HEALING_QUEUE);
		if (queue != this.satiated$timedQueue) {
			// Eating (or the previous heal step) set a new queue, so its interval starts now.
			this.satiated$timedQueue = queue;
			this.satiated$healTimer = 0;
		}

		if (queue == null || player.hasEffect(MobEffects.HUNGER)) {
			return;
		}

		// The queue runs down on schedule even at full health; it only heals when there's damage to heal.
		if (++this.satiated$healTimer >= queue.interval()) {
			if (player.isHurt()) {
				player.heal(1.0F);
			}

			if (queue.amount() > 1) {
				player.setAttached(ModAttachments.HEALING_QUEUE, new HealingQueue(queue.amount() - 1, queue.interval()));
			} else {
				player.removeAttached(ModAttachments.HEALING_QUEUE);
			}
		}
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
