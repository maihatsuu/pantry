package maihatsuu.satiated.network;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import maihatsuu.satiated.Satiated;
import maihatsuu.satiated.food.FoodGroup;
import maihatsuu.satiated.food.FoodItemEntry;
import maihatsuu.satiated.food.SatiatedFoods;
import maihatsuu.satiated.potion.PotionDurations;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sends the server's food and potion tables to the client so tooltips match what the server applies.
 * Food groups are sent unresolved; the client matches tags itself using the tags the server sent it.
 */
public record SyncTablesPayload(
	Map<String, FoodGroup> foodGroups, Map<ResourceLocation, FoodItemEntry> foodItems, Map<ResourceLocation, Integer> potionDurations
) implements CustomPacketPayload {
	public static final Type<SyncTablesPayload> TYPE = new Type<>(Satiated.id("sync_tables"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SyncTablesPayload> STREAM_CODEC = StreamCodec.composite(
		// LinkedHashMap keeps group order, which decides ties between groups.
		ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, FoodGroup.STREAM_CODEC), SyncTablesPayload::foodGroups,
		ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, FoodItemEntry.STREAM_CODEC), SyncTablesPayload::foodItems,
		ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT), SyncTablesPayload::potionDurations,
		SyncTablesPayload::new
	);

	public static SyncTablesPayload current() {
		return new SyncTablesPayload(SatiatedFoods.TABLE.groups(), SatiatedFoods.TABLE.items(), PotionDurations.TABLE.entries());
	}

	public void apply() {
		SatiatedFoods.TABLE.set(this.foodGroups, this.foodItems);
		PotionDurations.TABLE.set(this.potionDurations);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
