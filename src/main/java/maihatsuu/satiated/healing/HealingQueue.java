package maihatsuu.satiated.healing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * Healing a player has queued from the last food they ate. Eating replaces the queue; it never adds to it.
 *
 * @param amount   queued healing in HP (half-hearts)
 * @param interval ticks between each half-heart heal
 */
public record HealingQueue(int amount, int interval) {
	public static final int MIN_SPEED = 1;
	public static final int MAX_SPEED = 3;

	// Ticks between half-heart heals, indexed by speed - 1.
	private static final int[] INTERVALS = {200, 100, 20};

	public static final Codec<HealingQueue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(HealingQueue::amount),
		ExtraCodecs.POSITIVE_INT.fieldOf("interval").forGetter(HealingQueue::interval)
	).apply(instance, HealingQueue::new));

	public static final StreamCodec<ByteBuf, HealingQueue> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, HealingQueue::amount,
		ByteBufCodecs.VAR_INT, HealingQueue::interval,
		HealingQueue::new
	);

	/** Speeds as written in the food table (1 = slowest). */
	public static final Codec<Integer> SPEED_CODEC = Codec.intRange(MIN_SPEED, MAX_SPEED);

	/** Healing as written in the food table, in hearts (multiples of 0.5), stored as HP. */
	public static final Codec<Integer> HEARTS_CODEC = Codec.FLOAT.comapFlatMap(
		hearts -> hearts > 0 && hearts * 2 == Math.round(hearts * 2)
			? DataResult.success(Math.round(hearts * 2))
			: DataResult.error(() -> "Hearts must be a positive multiple of 0.5, got " + hearts),
		hp -> hp / 2.0F
	);

	public static int intervalFor(int speed) {
		return INTERVALS[Math.clamp(speed, MIN_SPEED, MAX_SPEED) - 1];
	}

	public static HealingQueue of(int healing, int speed) {
		return new HealingQueue(healing, intervalFor(speed));
	}
}
