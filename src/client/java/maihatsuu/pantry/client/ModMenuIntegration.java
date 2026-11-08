package maihatsuu.pantry.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

/** Mod Menu's config button. Cloth Config is optional, so without it Pantry has no config screen. */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
			return ModMenuApi.super.getModConfigScreenFactory();
		}
		// Kept in its own class so Cloth's classes only load when Cloth is installed.
		return PantryConfigScreen::create;
	}
}
