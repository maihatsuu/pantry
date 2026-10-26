package maihatsuu.satiated.data;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import maihatsuu.satiated.Satiated;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * A single JSON file of {@code id -> value} entries, e.g. {@code data/satiated/potion_durations.json}.
 * Every datapack's copy is merged in pack order, so higher-priority packs override individual entries.
 * A file with {@code "replace": true} discards everything loaded before it.
 */
public class MergedJsonTable<V> implements SimpleSynchronousResourceReloadListener {
	private final ResourceLocation file;
	private final Codec<Layer<V>> codec;
	private Map<ResourceLocation, V> entries = Map.of();

	public MergedJsonTable(ResourceLocation file, String entriesField, Codec<V> valueCodec) {
		this.file = file;
		this.codec = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("replace", false).forGetter(Layer::replace),
			Codec.unboundedMap(ResourceLocation.CODEC, valueCodec).fieldOf(entriesField).forGetter(Layer::entries)
		).apply(instance, Layer::new));
	}

	public Map<ResourceLocation, V> entries() {
		return this.entries;
	}

	/** Replaces the loaded entries; used on the client when the server syncs its tables. */
	public void set(Map<ResourceLocation, V> entries) {
		this.entries = Map.copyOf(entries);
	}

	@Override
	public ResourceLocation getFabricId() {
		return this.file;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		Map<ResourceLocation, V> merged = new HashMap<>();
		readLayers(manager, this.file, this.codec, layer -> {
			if (layer.replace()) {
				merged.clear();
			}
			merged.putAll(layer.entries());
		});

		this.set(merged);
		Satiated.LOGGER.info("Loaded {} entries from {}", merged.size(), this.file);
	}

	/** Parses every pack's copy of {@code file}, lowest priority first, logging and skipping invalid ones. */
	public static <L> void readLayers(ResourceManager manager, ResourceLocation file, Codec<L> codec, Consumer<L> consumer) {
		for (Resource resource : manager.getResourceStack(file)) {
			try (Reader reader = resource.openAsReader()) {
				codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
					.resultOrPartial(error -> Satiated.LOGGER.error("Invalid {} in pack {}: {}", file, resource.sourcePackId(), error))
					.ifPresent(consumer);
			} catch (Exception e) {
				Satiated.LOGGER.error("Failed to read {} from pack {}", file, resource.sourcePackId(), e);
			}
		}
	}

	private record Layer<V>(boolean replace, Map<ResourceLocation, V> entries) {
	}
}
