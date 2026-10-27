package maihatsuu.pantry;

import java.util.List;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ModItemComponents {
	public static final int STACK_SIZE = 16;

	private static final List<Item> STACK_TO_16 = List.of(
		Items.POTION,
		Items.SPLASH_POTION,
		Items.LINGERING_POTION,
		Items.MUSHROOM_STEW,
		Items.RABBIT_STEW,
		Items.BEETROOT_SOUP,
		Items.SUSPICIOUS_STEW
	);

	private ModItemComponents() {
	}

	public static void init() {
		DefaultItemComponentEvents.MODIFY.register(context ->
			context.modify(STACK_TO_16, (builder, item) -> builder.set(DataComponents.MAX_STACK_SIZE, STACK_SIZE))
		);
	}
}
