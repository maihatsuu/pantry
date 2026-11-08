package maihatsuu.pantry;

import java.util.List;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ModItemComponents {
	public static final int STACK_SIZE = 16;
	// Same as vanilla Dried Kelp, twice as fast as the default 1.6s.
	private static final float FAST_EAT_SECONDS = 0.8F;
	// Down from vanilla's 14.4.
	private static final float GOLDEN_CARROT_SATURATION = 8.0F;

	private static final List<Item> POTIONS = List.of(
		Items.POTION,
		Items.SPLASH_POTION,
		Items.LINGERING_POTION
	);

	private static final List<Item> SOUPS = List.of(
		Items.MUSHROOM_STEW,
		Items.RABBIT_STEW,
		Items.BEETROOT_SOUP,
		Items.SUSPICIOUS_STEW
	);

	private static final List<Item> FAST_FOODS = List.of(
		Items.COOKIE,
		Items.SWEET_BERRIES,
		Items.GLOW_BERRIES,
		Items.APPLE,
		Items.CARROT,
		Items.MELON_SLICE
	);

	private ModItemComponents() {
	}

	public static void init() {
		PantryConfig config = PantryConfig.get();

		DefaultItemComponentEvents.MODIFY.register(context -> {
			if (!config.biggerStacks) {
				return;
			}
			context.modify(POTIONS, (builder, item) -> builder.set(DataComponents.MAX_STACK_SIZE, STACK_SIZE));
			context.modify(SOUPS, (builder, item) -> builder.set(DataComponents.MAX_STACK_SIZE, STACK_SIZE));
			context.modify(Items.CAKE, builder -> builder.set(DataComponents.MAX_STACK_SIZE, STACK_SIZE));
		});

		// Every food can be eaten when full, like golden apples and chorus fruit in vanilla, and small snacks eat faster.
		DefaultItemComponentEvents.MODIFY.register(context ->
			context.modify(item -> item.components().has(DataComponents.FOOD), (builder, item) -> {
				FoodProperties food = item.components().get(DataComponents.FOOD);
				boolean nerfCarrot = config.goldenCarrotNerf && item == Items.GOLDEN_CARROT;
				float saturation = nerfCarrot ? GOLDEN_CARROT_SATURATION : food.saturation();
				float eatSeconds = config.fastSnacks && FAST_FOODS.contains(item) ? FAST_EAT_SECONDS : food.eatSeconds();
				boolean canAlwaysEat = config.eatWhenFull || food.canAlwaysEat();
				builder.set(DataComponents.FOOD, new FoodProperties(
					food.nutrition(), saturation, canAlwaysEat, eatSeconds, food.usingConvertsTo(), food.effects()
				));
			})
		);
	}
}
