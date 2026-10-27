package maihatsuu.satiated.food;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import maihatsuu.satiated.healing.HealingQueue;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

/**
 * A group in {@code foods.json}: every member heals the same amount at the same speed.
 * Members are item ids or item tags ({@code #c:foods/cooked_meats}).
 *
 * @param speed   heal speed, 1 (slowest) to 3
 * @param healing healing in HP; written as {@code "hearts"} in JSON
 */
public record FoodGroup(int speed, int healing, List<ExtraCodecs.TagOrElementLocation> items) {
	public static final Codec<FoodGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		HealingQueue.SPEED_CODEC.fieldOf("speed").forGetter(FoodGroup::speed),
		HealingQueue.HEARTS_CODEC.fieldOf("hearts").forGetter(FoodGroup::healing),
		ExtraCodecs.TAG_OR_ELEMENT_ID.listOf().fieldOf("items").forGetter(FoodGroup::items)
	).apply(instance, FoodGroup::new));

	private static final StreamCodec<ByteBuf, ExtraCodecs.TagOrElementLocation> MEMBER_STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, ExtraCodecs.TagOrElementLocation::id,
		ByteBufCodecs.BOOL, ExtraCodecs.TagOrElementLocation::tag,
		ExtraCodecs.TagOrElementLocation::new
	);

	public static final StreamCodec<ByteBuf, FoodGroup> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, FoodGroup::speed,
		ByteBufCodecs.VAR_INT, FoodGroup::healing,
		MEMBER_STREAM_CODEC.apply(ByteBufCodecs.list()), FoodGroup::items,
		FoodGroup::new
	);
}
