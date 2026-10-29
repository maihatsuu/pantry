package maihatsuu.pantry.mixin;

import maihatsuu.pantry.ModItemComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Soups and stews are drunk like a Honey Bottle: drinking animation and sound instead of eating. */
@Mixin(Item.class)
public class ItemMixin {
	@Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
	private void pantry$drinkSoups(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
		if (ModItemComponents.SOUPS.contains(stack.getItem())) {
			cir.setReturnValue(UseAnim.DRINK);
		}
	}

	// Also played once when finishing, so it needs to match the drinking sound.
	@Inject(method = "getEatingSound", at = @At("HEAD"), cancellable = true)
	private void pantry$soupSipSound(CallbackInfoReturnable<SoundEvent> cir) {
		if (ModItemComponents.SOUPS.contains((Item) (Object) this)) {
			cir.setReturnValue(SoundEvents.GENERIC_DRINK);
		}
	}
}
