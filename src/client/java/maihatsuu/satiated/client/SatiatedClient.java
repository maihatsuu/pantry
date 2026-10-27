package maihatsuu.satiated.client;

import maihatsuu.satiated.food.FoodItemEntry;
import maihatsuu.satiated.food.SatiatedFoods;
import maihatsuu.satiated.network.SyncTablesPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.alchemy.PotionContents;

public class SatiatedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncTablesPayload.TYPE, (payload, context) -> payload.apply());

		// Lists a food's bonus effects using the same formatting as potion tooltips.
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
			FoodItemEntry entry = SatiatedFoods.bonus(stack.getItem());
			if (entry == null || entry.effects().isEmpty()) {
				return;
			}

			PotionContents.addPotionTooltip(
				entry.effects().stream().map(FoodProperties.PossibleEffect::effect).toList(), lines::add, 1.0F, tooltipContext.tickRate()
			);
		});
	}
}
