package maihatsuu.satiated.network;

import java.util.HashMap;
import java.util.Map;

import maihatsuu.satiated.Satiated;
import maihatsuu.satiated.food.SatiatedFoodEntry;
import maihatsuu.satiated.food.SatiatedFoods;
import maihatsuu.satiated.potion.PotionDurations;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sends the server's food and potion tables to the client so tooltips match what the server applies. */
public record SyncTablesPayload(Map<ResourceLocation, SatiatedFoodEntry> foods, Map<ResourceLocation, Integer> potionDurations)
	implements CustomPacketPayload {
	public static final Type<SyncTablesPayload> TYPE = new Type<>(Satiated.id("sync_tables"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncTablesPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, SatiatedFoodEntry.STREAM_CODEC), SyncTablesPayload::foods,
		ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT), SyncTablesPayload::potionDurations,
		SyncTablesPayload::new
	);

	public static SyncTablesPayload current() {
		return new SyncTablesPayload(SatiatedFoods.TABLE.entries(), PotionDurations.TABLE.entries());
	}

	public void apply() {
		SatiatedFoods.TABLE.set(this.foods);
		PotionDurations.TABLE.set(this.potionDurations);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
