package maihatsuu.satiated.food;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

/**
 * A group in {@code foods.json}: every member shares one Satiated tier and duration.
 * Members are item ids or item tags ({@code #c:foods/cooked_meats}).
 */
public record FoodGroup(int tier, int duration, List<ExtraCodecs.TagOrElementLocation> items) {
	public static final Codec<FoodGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.intRange(1, 256).fieldOf("tier").forGetter(FoodGroup::tier),
		ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(FoodGroup::duration),
		ExtraCodecs.TAG_OR_ELEMENT_ID.listOf().fieldOf("items").forGetter(FoodGroup::items)
	).apply(instance, FoodGroup::new));

	private static final StreamCodec<ByteBuf, ExtraCodecs.TagOrElementLocation> MEMBER_STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, ExtraCodecs.TagOrElementLocation::id,
		ByteBufCodecs.BOOL, ExtraCodecs.TagOrElementLocation::tag,
		ExtraCodecs.TagOrElementLocation::new
	);

	public static final StreamCodec<ByteBuf, FoodGroup> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, FoodGroup::tier,
		ByteBufCodecs.VAR_INT, FoodGroup::duration,
		MEMBER_STREAM_CODEC.apply(ByteBufCodecs.list()), FoodGroup::items,
		FoodGroup::new
	);
}
