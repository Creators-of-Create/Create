package com.simibubi.create.content.logistics.trains;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.trains.track.TrackBlock;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.simibubi.create.content.logistics.trains.TrackMaterialFactory.make;

public class TrackMaterial {
	public static final List<TrackMaterial> ALL = new ArrayList<>();

	public static final TrackMaterial ANDESITE = make(Create.asResource("andesite"))
			.lang("Andesite")
			.block(Lazy.of(() -> AllBlocks.TRACK))
			.particle(Create.asResource("block/palettes/stone_types/polished/andesite_cut_polished"))
			.setBuiltin()
			.build();

	public final ResourceLocation id;
	public final String langName;
	public final Supplier<BlockEntry<? extends TrackBlock>> trackBlock;
	public final Ingredient sleeperIngredient;
	public final Ingredient railsIngredient;
	public final ResourceLocation particle;
	public final TrackType trackType;
	public final TrackModelHolder modelHolder;

	public TrackMaterial(ResourceLocation id, String langName, Supplier<BlockEntry<? extends TrackBlock>> trackBlock, ResourceLocation particle, Ingredient sleeperIngredient, Ingredient railsIngredient, TrackType trackType, TrackModelHolder modelHolder) {
		this.id = id;
		this.langName = langName;
		this.trackBlock = trackBlock;
		this.sleeperIngredient = sleeperIngredient;
		this.railsIngredient = railsIngredient;
		this.particle = particle;
		this.trackType = trackType;
		this.modelHolder = modelHolder;
		ALL.add(this);
	}

	public BlockEntry<? extends TrackBlock> getTrackBlock() {
		return this.trackBlock.get();
	}

	public TrackBlock create(BlockBehaviour.Properties properties) {
		return this.trackType.factory.create(properties, this);
	}

	public boolean isCustom(String modId) {
		return this.id.getNamespace().equals(modId);
	}

	public static TrackMaterial[] allCustom(String modid) {
		return ALL.stream().filter(tm -> tm.isCustom(modid)).toArray(TrackMaterial[]::new);
	}

	public static List<BlockEntry<?>> allCustomBlocks(String modid) {
		List<BlockEntry<?>> list = new ArrayList<>();
		for (TrackMaterial material : allCustom(modid)) {
			list.add(material.getTrackBlock());
		}
		return list;
	}

	public static List<BlockEntry<?>> allBlocks() {
		List<BlockEntry<?>> list = new ArrayList<>();
		for (TrackMaterial material : ALL) {
			list.add(material.getTrackBlock());
		}
		return list;
	}

	public String resourceName() {
		return this.id.getPath();
	}

	public static TrackMaterial deserialize(String serializedName) {
		ResourceLocation id = new ResourceLocation(serializedName);
		for (TrackMaterial material : ALL) {
			if (material.id.equals(id))
				return material;
		}
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

	public record TrackModelHolder(PartialModel tie, PartialModel segment_left, PartialModel segment_right) {
		static final TrackModelHolder DEFAULT = new TrackModelHolder(AllBlockPartials.TRACK_TIE, AllBlockPartials.TRACK_SEGMENT_LEFT, AllBlockPartials.TRACK_SEGMENT_RIGHT);
	}
}
