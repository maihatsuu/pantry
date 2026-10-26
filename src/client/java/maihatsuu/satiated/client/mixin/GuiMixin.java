package maihatsuu.satiated.client.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the hunger bar. The air bubbles keep their vanilla position above it. */
@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
	private void satiated$hideFood(GuiGraphics guiGraphics, Player player, int y, int right, CallbackInfo ci) {
		ci.cancel();
	}
}
