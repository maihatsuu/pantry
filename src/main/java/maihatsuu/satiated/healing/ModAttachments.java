package maihatsuu.satiated.healing;

import maihatsuu.satiated.Satiated;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachments {
	/**
	 * The player's healing queue. Saved with the player and cleared on death (not copyOnDeath).
	 * Synced to that player only, for the HUD overlay.
	 */
	public static final AttachmentType<HealingQueue> HEALING_QUEUE = AttachmentRegistry.create(
		Satiated.id("healing_queue"),
		builder -> builder.persistent(HealingQueue.CODEC).syncWith(HealingQueue.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
	);

	private ModAttachments() {
	}

	// Forces class loading so the static registrations above run during mod init.
	public static void init() {
	}
}
