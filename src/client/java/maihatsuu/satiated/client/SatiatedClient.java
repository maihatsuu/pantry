package maihatsuu.satiated.client;

import java.util.List;

import maihatsuu.satiated.food.SatiatedFoods;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;

public class SatiatedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Lists a food's bonus effects using the same formatting as potion tooltips.
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
			List<MobEffectInstance> effects = SatiatedFoods.bonus(stack.getItem());
			if (!effects.isEmpty()) {
				PotionContents.addPotionTooltip(effects, lines::add, 1.0F, tooltipContext.tickRate());
			}
		});
	}
}
