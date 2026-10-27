package maihatsuu.pantry.mixin;

import java.util.function.Consumer;

import maihatsuu.pantry.potion.PotionDurations;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Drinkable potions use the longer durations from PotionDurations. */
@Mixin(PotionItem.class)
public class PotionItemMixin {
	@Redirect(
		method = "finishUsingItem",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;forEachEffect(Ljava/util/function/Consumer;)V")
	)
	private void pantry$applyDrinkDurations(PotionContents contents, Consumer<MobEffectInstance> consumer) {
		PotionDurations.drinkEffects(contents).forEach(consumer);
	}

	// Splash potions inherit this tooltip, so only swap durations for the drinkable item.
	@Redirect(
		method = "appendHoverText",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;addPotionTooltip(Ljava/util/function/Consumer;FF)V")
	)
	private void pantry$showDrinkDurations(PotionContents contents, Consumer<Component> tooltip, float durationFactor, float tickRate) {
		if ((Object) this instanceof ThrowablePotionItem) {
			contents.addPotionTooltip(tooltip, durationFactor, tickRate);
		} else {
			PotionContents.addPotionTooltip(PotionDurations.drinkEffects(contents), tooltip, durationFactor, tickRate);
		}
	}
}
