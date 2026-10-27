package maihatsuu.pantry;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pantry implements ModInitializer {
	public static final String MOD_ID = "pantry";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemComponents.init();
	}
}
