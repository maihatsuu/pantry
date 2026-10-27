package maihatsuu.satiated.food;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import maihatsuu.satiated.healing.HealingQueue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.food.FoodProperties;

/**
 * A per-item row in {@code foods.json}. {@code speed} and {@code healing} override the item's group when set.
 *
 * @param healing               healing in HP; written as {@code "hearts"} in JSON
 * @param effects               bonus effects, in the same shape as vanilla food effects
 * @param replaceVanillaEffects if true, the food's vanilla effects (e.g. rotten flesh's Hunger) are not applied
 */
public record FoodItemEntry(
	Optional<Integer> speed, Optional<Integer> healing, List<FoodProperties.PossibleEffect> effects, boolean replaceVanillaEffects
) {
	public static final Codec<FoodItemEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HealingQueue.SPEED_CODEC.optionalFieldOf("speed").forGetter(FoodItemEntry::speed),
		HealingQueue.HEARTS_CODEC.optionalFieldOf("hearts").forGetter(FoodItemEntry::healing),
		FoodProperties.PossibleEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(FoodItemEntry::effects),
		Codec.BOOL.optionalFieldOf("replace_vanilla_effects", false).forGetter(FoodItemEntry::replaceVanillaEffects)
	).apply(instance, FoodItemEntry::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FoodItemEntry> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), FoodItemEntry::speed,
		ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), FoodItemEntry::healing,
		FoodProperties.PossibleEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), FoodItemEntry::effects,
		ByteBufCodecs.BOOL, FoodItemEntry::replaceVanillaEffects,
		FoodItemEntry::new
	);
}
