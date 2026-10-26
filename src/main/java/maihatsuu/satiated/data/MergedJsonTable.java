package maihatsuu.satiated.data;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

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
 * A single JSON file of {@code id -> value} entries, e.g. {@code data/satiated/foods.json}.
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
		for (Resource resource : manager.getResourceStack(this.file)) {
			try (Reader reader = resource.openAsReader()) {
				this.codec.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
					.resultOrPartial(error -> Satiated.LOGGER.error("Invalid {} in pack {}: {}", this.file, resource.sourcePackId(), error))
					.ifPresent(layer -> {
						if (layer.replace()) {
							merged.clear();
						}
						merged.putAll(layer.entries());
					});
			} catch (Exception e) {
				Satiated.LOGGER.error("Failed to read {} from pack {}", this.file, resource.sourcePackId(), e);
			}
		}

		this.set(merged);
		Satiated.LOGGER.info("Loaded {} entries from {}", merged.size(), this.file);
	}

	private record Layer<V>(boolean replace, Map<ResourceLocation, V> entries) {
	}
}
