package maihatsuu.satiated.food;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import maihatsuu.satiated.Satiated;
import maihatsuu.satiated.data.MergedJsonTable;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

/**
 * Loads {@code data/satiated/foods.json}: {@code groups} (shared tier/duration for a list of items and tags)
 * and {@code items} (per-item bonus effects and overrides). Datapacks merge per group id and per item id.
 */
public class FoodTable implements SimpleSynchronousResourceReloadListener {
	private static final ResourceLocation FILE = Satiated.id("foods.json");

	private static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("replace", false).forGetter(Layer::replace),
		Codec.unboundedMap(Codec.STRING, FoodGroup.CODEC).optionalFieldOf("groups", Map.of()).forGetter(Layer::groups),
		Codec.unboundedMap(ResourceLocation.CODEC, FoodItemEntry.CODEC).optionalFieldOf("items", Map.of()).forGetter(Layer::items)
	).apply(instance, Layer::new));

	private Map<String, FoodGroup> groups = Map.of();
	private Map<ResourceLocation, FoodItemEntry> items = Map.of();
	// Built from explicit item ids only; tags can't be resolved until the server binds them after reload.
	private Map<ResourceLocation, FoodGroup> groupsByItem = Map.of();
	private List<TaggedGroup> taggedGroups = List.of();

	public Map<String, FoodGroup> groups() {
		return this.groups;
	}

	public Map<ResourceLocation, FoodItemEntry> items() {
		return this.items;
	}

	public FoodItemEntry item(ResourceLocation id) {
		return this.items.get(id);
	}

	/**
	 * The item's group: a group that lists it by id wins over one that matches it by tag.
	 * If several match the same way, the last group loaded wins.
	 */
	public FoodGroup group(Item item, ResourceLocation id) {
		FoodGroup explicit = this.groupsByItem.get(id);
		if (explicit != null) {
			return explicit;
		}

		Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(item);
		for (int i = this.taggedGroups.size() - 1; i >= 0; i--) {
			TaggedGroup tagged = this.taggedGroups.get(i);
			if (tagged.tags().stream().anyMatch(holder::is)) {
				return tagged.group();
			}
		}

		return null;
	}

	/** Replaces the loaded table; used on the client when the server syncs it. */
	public void set(Map<String, FoodGroup> groups, Map<ResourceLocation, FoodItemEntry> items) {
		this.groups = Collections.unmodifiableMap(new LinkedHashMap<>(groups));
		this.items = Map.copyOf(items);

		Map<ResourceLocation, FoodGroup> byItem = new HashMap<>();
		Map<ResourceLocation, String> owners = new HashMap<>();
		List<TaggedGroup> tagged = new ArrayList<>();
		groups.forEach((name, group) -> {
			List<TagKey<Item>> tags = new ArrayList<>();
			for (ExtraCodecs.TagOrElementLocation member : group.items()) {
				if (member.tag()) {
					tags.add(TagKey.create(Registries.ITEM, member.id()));
				} else {
					String previous = owners.put(member.id(), name);
					if (previous != null && !previous.equals(name)) {
						Satiated.LOGGER.warn("{} is listed in food groups '{}' and '{}'; using '{}'", member.id(), previous, name, name);
					}
					byItem.put(member.id(), group);
				}
			}
			if (!tags.isEmpty()) {
				tagged.add(new TaggedGroup(group, List.copyOf(tags)));
			}
		});

		this.groupsByItem = byItem;
		this.taggedGroups = List.copyOf(tagged);
	}

	@Override
	public ResourceLocation getFabricId() {
		return FILE;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		// LinkedHashMap keeps load order, which decides ties between groups.
		Map<String, FoodGroup> groups = new LinkedHashMap<>();
		Map<ResourceLocation, FoodItemEntry> items = new HashMap<>();
		MergedJsonTable.readLayers(manager, FILE, CODEC, layer -> {
			if (layer.replace()) {
				groups.clear();
				items.clear();
			}
			layer.groups().forEach((name, group) -> {
				groups.remove(name);
				groups.put(name, group);
			});
			items.putAll(layer.items());
		});

		this.set(groups, items);
		Satiated.LOGGER.info("Loaded {} food groups and {} item entries from {}", groups.size(), items.size(), FILE);
	}

	private record Layer(boolean replace, Map<String, FoodGroup> groups, Map<ResourceLocation, FoodItemEntry> items) {
	}

	private record TaggedGroup(FoodGroup group, List<TagKey<Item>> tags) {
	}
}
