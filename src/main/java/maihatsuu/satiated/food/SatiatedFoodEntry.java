package maihatsuu.satiated.food;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.food.FoodProperties;

/**
 * One row of {@code foods.json}.
 *
 * @param tier                  Satiated tier, starting at 1 (amplifier 0)
 * @param duration              Satiated duration in ticks
 * @param effects               extra effects, in the same shape as vanilla food effects
 * @param replaceVanillaEffects if true, the food's vanilla effects (e.g. rotten flesh's Hunger) are not applied
 */
public record SatiatedFoodEntry(int tier, int duration, List<FoodProperties.PossibleEffect> effects, boolean replaceVanillaEffects) {
	public static final Codec<SatiatedFoodEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.intRange(1, 256).fieldOf("tier").forGetter(SatiatedFoodEntry::tier),
		ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(SatiatedFoodEntry::duration),
		FoodProperties.PossibleEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(SatiatedFoodEntry::effects),
		Codec.BOOL.optionalFieldOf("replace_vanilla_effects", false).forGetter(SatiatedFoodEntry::replaceVanillaEffects)
	).apply(instance, SatiatedFoodEntry::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SatiatedFoodEntry> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, SatiatedFoodEntry::tier,
		ByteBufCodecs.VAR_INT, SatiatedFoodEntry::duration,
		FoodProperties.PossibleEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), SatiatedFoodEntry::effects,
		ByteBufCodecs.BOOL, SatiatedFoodEntry::replaceVanillaEffects,
		SatiatedFoodEntry::new
	);
}
