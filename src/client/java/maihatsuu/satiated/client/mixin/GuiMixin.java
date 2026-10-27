package maihatsuu.satiated.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import maihatsuu.satiated.healing.HealingQueue;
import maihatsuu.satiated.healing.ModAttachments;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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

/**
 * Hides the hunger bar, moves the air bubbles down into its row, and draws the healing queue
 * as faded hearts in the slots it will refill.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
	// Vanilla draws the hearts and the hunger bar this far above the bottom of the screen.
	@Unique
	private static final int STATUS_ROW_OFFSET = 39;

	@Unique
	private static final float QUEUED_HEART_ALPHA = 0.4F;

	@Unique
	private static final ResourceLocation HEART_FULL = ResourceLocation.withDefaultNamespace("hud/heart/full");
	@Unique
	private static final ResourceLocation HEART_HALF = ResourceLocation.withDefaultNamespace("hud/heart/half");
	@Unique
	private static final ResourceLocation HARDCORE_HEART_FULL = ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full");
	@Unique
	private static final ResourceLocation HARDCORE_HEART_HALF = ResourceLocation.withDefaultNamespace("hud/heart/hardcore_half");

	@Shadow
	@Final
	private static ResourceLocation AIR_SPRITE;

	@Shadow
	@Final
	private static ResourceLocation AIR_BURSTING_SPRITE;

	@Shadow
	private int tickCount;

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

	/**
	 * Draws queued healing over the empty heart slots it will fill, capped at max health.
	 * Slot positions repeat vanilla's renderHearts loop, including the low-health shake (same seeded
	 * random, consumed in the same order) and the regeneration bounce, so the faded hearts move with the real ones.
	 */
	@Inject(method = "renderHearts", at = @At("TAIL"))
	private void satiated$renderHealingQueue(
		GuiGraphics guiGraphics, Player player, int left, int top, int rowHeight, int regenSlot,
		float maxHealthShown, int health, int displayHealth, int absorption, boolean blinking, CallbackInfo ci
	) {
		HealingQueue queue = player.getAttached(ModAttachments.HEALING_QUEUE);
		if (queue == null) {
			return;
		}

		int queuedEnd = Math.min(health + queue.amount(), Mth.ceil(player.getMaxHealth()));
		if (queuedEnd <= health) {
			return;
		}

		boolean hardcore = player.level().getLevelData().isHardcore();
		int healthSlots = Mth.ceil(maxHealthShown / 2.0);
		int absorptionSlots = Mth.ceil(absorption / 2.0);
		RandomSource shake = RandomSource.create(this.tickCount * 312871);

		RenderSystem.enableBlend();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, QUEUED_HEART_ALPHA);
		for (int slot = healthSlots + absorptionSlots - 1; slot >= 0; slot--) {
			int x = left + slot % 10 * 8;
			int y = top - slot / 10 * rowHeight;
			if (health + absorption <= 4) {
				y += shake.nextInt(2);
			}

			if (slot < healthSlots && slot == regenSlot) {
				y -= 2;
			}

			// This slot covers HP [2*slot, 2*slot + 2). Draw the part of it the queue will fill.
			int from = Math.max(slot * 2, health);
			int to = Math.min(slot * 2 + 2, queuedEnd);
			if (slot < healthSlots && to > from) {
				// A queue ending mid-heart shows a half; one finishing a half-filled heart shows a full,
				// whose left half sits under the real half heart.
				boolean half = to == slot * 2 + 1;
				ResourceLocation sprite = hardcore ? (half ? HARDCORE_HEART_HALF : HARDCORE_HEART_FULL) : (half ? HEART_HALF : HEART_FULL);
				guiGraphics.blitSprite(sprite, x, y, 9, 9);
			}
		}

		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}
}
