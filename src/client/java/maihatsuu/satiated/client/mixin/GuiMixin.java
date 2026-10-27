package maihatsuu.satiated.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the hunger bar and moves the air bubbles down into its row, opposite the hearts. */
@Mixin(Gui.class)
public abstract class GuiMixin {
	// Vanilla draws the hearts and the hunger bar this far above the bottom of the screen.
	@Unique
	private static final int STATUS_ROW_OFFSET = 39;

	@Shadow
	@Final
	private static ResourceLocation AIR_SPRITE;

	@Shadow
	@Final
	private static ResourceLocation AIR_BURSTING_SPRITE;

	@Shadow
	@Nullable
	protected abstract LivingEntity getPlayerVehicleWithHealth();

	@Shadow
	protected abstract int getVehicleMaxHearts(@Nullable LivingEntity vehicle);

	@Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
	private void satiated$hideFood(GuiGraphics guiGraphics, Player player, int y, int right, CallbackInfo ci) {
		ci.cancel();
	}

	// A mount's hearts take the hunger bar's row, and vanilla already stacks the bubbles above them.
	@WrapOperation(
		method = "renderPlayerHealth",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V")
	)
	private void satiated$moveAirIntoFoodRow(
		GuiGraphics guiGraphics, ResourceLocation sprite, int x, int y, int width, int height, Operation<Void> original
	) {
		boolean air = sprite.equals(AIR_SPRITE) || sprite.equals(AIR_BURSTING_SPRITE);
		if (air && this.getVehicleMaxHearts(this.getPlayerVehicleWithHealth()) == 0) {
			y = guiGraphics.guiHeight() - STATUS_ROW_OFFSET;
		}

		original.call(guiGraphics, sprite, x, y, width, height);
	}
}
