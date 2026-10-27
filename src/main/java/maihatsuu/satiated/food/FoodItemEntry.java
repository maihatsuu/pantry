package maihatsuu.satiated.food;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.food.FoodProperties;

/**
 * A row of {@code foods.json}.
 *
 * @param effects               bonus effects, in the same shape as vanilla food effects
 * @param replaceVanillaEffects if true, the food's vanilla effects (e.g. rotten flesh's Hunger) are not applied
 */
public record FoodItemEntry(List<FoodProperties.PossibleEffect> effects, boolean replaceVanillaEffects) {
	public static final Codec<FoodItemEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		FoodProperties.PossibleEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(FoodItemEntry::effects),
		Codec.BOOL.optionalFieldOf("replace_vanilla_effects", false).forGetter(FoodItemEntry::replaceVanillaEffects)
	).apply(instance, FoodItemEntry::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FoodItemEntry> STREAM_CODEC = StreamCodec.composite(
		FoodProperties.PossibleEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), FoodItemEntry::effects,
		ByteBufCodecs.BOOL, FoodItemEntry::replaceVanillaEffects,
		FoodItemEntry::new
	);
}
