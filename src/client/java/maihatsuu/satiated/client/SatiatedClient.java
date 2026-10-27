package maihatsuu.satiated.client;

import maihatsuu.satiated.food.SatiatedFoodEntry;
import maihatsuu.satiated.food.SatiatedFoods;
import maihatsuu.satiated.healing.HealingQueue;
import maihatsuu.satiated.network.SyncTablesPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public class SatiatedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(SyncTablesPayload.TYPE, (payload, context) -> payload.apply());

		// Shows what a food heals, e.g. "Heals 6 ❤ · ½ ❤ every 5s", then its bonus effects as potion lines.
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

			String hearts = entry.healing() % 2 == 0 ? Integer.toString(entry.healing() / 2) : (entry.healing() / 2) + ".5";
			int seconds = HealingQueue.intervalFor(entry.speed()) / 20;
			lines.add(Component.translatable("tooltip.satiated.heals", hearts, seconds).withStyle(ChatFormatting.BLUE));

			if (!entry.effects().isEmpty()) {
				PotionContents.addPotionTooltip(
					entry.effects().stream().map(FoodProperties.PossibleEffect::effect).toList(), lines::add, 1.0F, tooltipContext.tickRate()
				);
			}
		});
	}
}
