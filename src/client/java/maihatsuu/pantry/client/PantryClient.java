package maihatsuu.pantry.client;

import java.util.ArrayList;
import java.util.List;

import maihatsuu.pantry.PantryConfig;
import maihatsuu.pantry.food.FoodBonuses;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public class PantryClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			// An empty list means the item hides its tooltip.
			if (!PantryConfig.get().foodEffectTooltips || lines.isEmpty()) {
				return;
			}
			List<Component> effectLines = new ArrayList<>();
			for (MobEffectInstance effect : shownEffects(stack)) {
				effectLines.add(describe(effect, context.tickRate()));
			}
			// Right below the name, so it stays above the advanced tooltip lines.
			lines.addAll(1, effectLines);
		});
	}

	/** The food's own effects plus Pantry's bonus, leaving out harmful ones. */
	private static List<MobEffectInstance> shownEffects(ItemStack stack) {
		FoodProperties food = stack.get(DataComponents.FOOD);
		if (food == null) {
			return List.of();
		}
		List<MobEffectInstance> effects = new ArrayList<>();
		food.effects().forEach(possible -> effects.add(possible.effect()));
		effects.addAll(FoodBonuses.bonus(stack.getItem()));
		effects.removeIf(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL);
		return effects;
	}

	private static Component describe(MobEffectInstance effect, float tickRate) {
		MutableComponent text = Component.translatable(effect.getDescriptionId());
		if (effect.getAmplifier() > 0) {
			text = Component.translatable("potion.withAmplifier", text, Component.translatable("potion.potency." + effect.getAmplifier()));
		}
		if (!effect.endsWithin(20)) {
			text = Component.translatable("potion.withDuration", text, MobEffectUtil.formatDuration(effect, 1.0F, tickRate));
		}
		return text.withStyle(effect.getEffect().value().getCategory().getTooltipFormatting());
	}
}
