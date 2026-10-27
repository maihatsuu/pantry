package maihatsuu.satiated;

import maihatsuu.satiated.food.SatiatedFoods;
import maihatsuu.satiated.healing.ModAttachments;
import maihatsuu.satiated.network.SyncTablesPayload;
import maihatsuu.satiated.potion.PotionDurations;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Satiated implements ModInitializer {
	public static final String MOD_ID = "satiated";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModAttachments.init();
		ModItemComponents.init();

		ResourceManagerHelper serverData = ResourceManagerHelper.get(PackType.SERVER_DATA);
		serverData.registerReloadListener(SatiatedFoods.TABLE);
		serverData.registerReloadListener(PotionDurations.TABLE);

		// Clients need both tables for tooltips, so resend them on join and after /reload.
		PayloadTypeRegistry.playS2C().register(SyncTablesPayload.TYPE, SyncTablesPayload.STREAM_CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(SyncTablesPayload.current()));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			SyncTablesPayload payload = SyncTablesPayload.current();
			server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, payload));
		});
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
