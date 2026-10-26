package maihatsuu.satiated.client;

import java.util.List;

import maihatsuu.satiated.effect.ModEffects;
import maihatsuu.satiated.food.SatiatedFoodEntry;
import maihatsuu.satiated.food.SatiatedFoods;
import maihatsuu.satiated.network.SyncTablesPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public class SatiatedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncTablesPayload.TYPE, (payload, context) -> payload.apply());

		// Shows what a food grants, e.g. "Satiated II (2:00)", using the same formatting as potion tooltips.
		ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
			SatiatedFoodEntry entry;
			FoodProperties food = stack.get(DataComponents.FOOD);
			if (food != null) {
				entry = SatiatedFoods.resolve(stack.getItem(), food);
			} else if (stack.is(Items.CAKE)) {
				entry = SatiatedFoods.resolveCakeSlice();
			} else {
				return;
			}

			MobEffectInstance satiated = new MobEffectInstance(ModEffects.SATIATED, entry.duration(), entry.tier() - 1);
			PotionContents.addPotionTooltip(List.of(satiated), lines::add, 1.0F, tooltipContext.tickRate());
		});
	}
}
