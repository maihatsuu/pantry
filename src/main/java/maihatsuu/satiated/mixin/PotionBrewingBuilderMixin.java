package maihatsuu.satiated.mixin;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Healing and regeneration can't be brewed. They stay available in creative and through commands. */
@Mixin(PotionBrewing.Builder.class)
public class PotionBrewingBuilderMixin {
	@Unique
	private static final List<Holder<Potion>> UNBREWABLE = List.of(
		Potions.HEALING,
		Potions.STRONG_HEALING,
		Potions.REGENERATION,
		Potions.LONG_REGENERATION,
		Potions.STRONG_REGENERATION
	);

	@Inject(method = "addMix", at = @At("HEAD"), cancellable = true)
	private void satiated$skipUnbrewable(Holder<Potion> input, Item ingredient, Holder<Potion> output, CallbackInfo ci) {
		if (UNBREWABLE.contains(output)) {
			ci.cancel();
		}
	}
}
