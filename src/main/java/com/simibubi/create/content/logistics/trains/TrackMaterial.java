package com.simibubi.create.content.logistics.trains;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.track.TrackBlock;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.simibubi.create.content.logistics.trains.TrackMaterialFactory.make;

public class TrackMaterial {
	public static final Map<ResourceLocation, TrackMaterial> ALL = new HashMap<>();

	public static final TrackMaterial ANDESITE = make(Create.asResource("andesite"))
			.lang("Andesite")
			.block(NonNullSupplier.lazy(() -> AllBlocks.TRACK))
			.particle(Create.asResource("block/palettes/stone_types/polished/andesite_cut_polished"))
			.defaultModels()
			.build();

	public final ResourceLocation id;
	public final String langName;
	public final NonNullSupplier<NonNullSupplier<? extends TrackBlock>> trackBlock;
	public final Ingredient sleeperIngredient;
	public final Ingredient railsIngredient;
	public final ResourceLocation particle;
	public final TrackType trackType;

	@OnlyIn(Dist.CLIENT)
	protected TrackModelHolder modelHolder;

	@OnlyIn(Dist.CLIENT)
	public TrackModelHolder getModelHolder() {
		return modelHolder;
	}

	public TrackMaterial(ResourceLocation id, String langName, NonNullSupplier<NonNullSupplier<? extends TrackBlock>> trackBlock,
						 ResourceLocation particle, Ingredient sleeperIngredient, Ingredient railsIngredient,
						 TrackType trackType, Supplier<Supplier<TrackModelHolder>> modelHolder) {
		this.id = id;
		this.langName = langName;
		this.trackBlock = trackBlock;
		this.sleeperIngredient = sleeperIngredient;
		this.railsIngredient = railsIngredient;
		this.particle = particle;
		this.trackType = trackType;
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.modelHolder = modelHolder.get().get());
		ALL.put(this.id, this);
	}

	public NonNullSupplier<? extends TrackBlock> getTrackBlock() {
		return this.trackBlock.get();
	}

	public TrackBlock createBlock(BlockBehaviour.Properties properties) {
		return this.trackType.factory.create(properties, this);
	}

	public boolean isCustom(String modId) {
		return this.id.getNamespace().equals(modId);
	}

	public static TrackMaterial[] allCustom(String modid) {
		return ALL.values().stream().filter(tm -> tm.isCustom(modid)).toArray(TrackMaterial[]::new);
	}

	public static List<NonNullSupplier<? extends TrackBlock>> allCustomBlocks(String modid) {
		List<NonNullSupplier<? extends TrackBlock>> list = new ArrayList<>();
		for (TrackMaterial material : allCustom(modid)) {
			list.add(material.getTrackBlock());
		}
		return list;
	}

	public static List<NonNullSupplier<? extends TrackBlock>> allBlocks() {
		List<NonNullSupplier<? extends TrackBlock>> list = new ArrayList<>();
		for (TrackMaterial material : ALL.values()) {
			list.add(material.getTrackBlock());
		}
		return list;
	}

	public String resourceName() {
		return this.id.getPath();
	}

	public static TrackMaterial deserialize(String serializedName) {
		ResourceLocation id = ResourceLocation.tryParse(serializedName);
		if (id == null) {
			Create.LOGGER.error("Failed to parse serialized track material: "+serializedName);
			return ANDESITE;
		}
		for (TrackMaterial material : ALL.values()) {
			if (material.id.equals(id))
				return material;
		}
		Create.LOGGER.error("Failed to locate serialized track material: "+serializedName);
		return ANDESITE;
	}

	public static class TrackType {
		@FunctionalInterface
		protected interface TrackBlockFactory {
			TrackBlock create(BlockBehaviour.Properties properties, TrackMaterial material);
		}

		public static final TrackType STANDARD = new TrackType(Create.asResource("standard"), TrackBlock::new);

		public final ResourceLocation id;
		protected final TrackBlockFactory factory;

		public TrackType(ResourceLocation id, TrackBlockFactory factory) {
			this.id = id;
			this.factory = factory;
		}
	}

	public static TrackMaterial fromItem(Item item) {
		if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ITrackBlock trackBlock)
			return trackBlock.getMaterial();
		return TrackMaterial.ANDESITE;
	}

	@OnlyIn(Dist.CLIENT)
	public record TrackModelHolder(PartialModel tie, PartialModel segment_left, PartialModel segment_right) {
		static final TrackModelHolder DEFAULT = new TrackModelHolder(AllBlockPartials.TRACK_TIE, AllBlockPartials.TRACK_SEGMENT_LEFT, AllBlockPartials.TRACK_SEGMENT_RIGHT);
	}
}
