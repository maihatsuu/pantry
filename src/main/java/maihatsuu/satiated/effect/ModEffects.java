package maihatsuu.satiated.effect;

import maihatsuu.satiated.Satiated;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {
	public static final Holder<MobEffect> SATIATED = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT, Satiated.id("satiated"), new SatiatedEffect()
	);

	private ModEffects() {
	}

	// Forces class loading so the static registrations above run during mod init.
	public static void init() {
	}
}
