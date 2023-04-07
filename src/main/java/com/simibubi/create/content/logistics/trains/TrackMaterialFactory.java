package com.simibubi.create.content.logistics.trains;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.logistics.trains.track.TrackBlock;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class TrackMaterialFactory {
	private final ResourceLocation id;
	private String langName;
	private Supplier<BlockEntry<? extends TrackBlock>> trackBlock;
	private Ingredient sleeperIngredient = Ingredient.EMPTY;
	private Ingredient railsIngredient = Ingredient.fromValues(Stream.of(new Ingredient.TagValue(AllTags.forgeItemTag("nuggets/iron")), new Ingredient.TagValue(AllTags.forgeItemTag("nuggets/zinc"))));
	private ResourceLocation particle;
	private TrackMaterial.TrackType trackType = TrackMaterial.TrackType.STANDARD;
	private TrackMaterial.TrackModelHolder modelHolder = null;
	private PartialModel tieModel = null;
	private PartialModel leftSegmentModel = null;
	private PartialModel rightSegmentModel = null;

	public TrackMaterialFactory(ResourceLocation id) {
		this.id = id;
	}

	public static TrackMaterialFactory make(ResourceLocation id) {  // Convenience function for static import
		return new TrackMaterialFactory(id);
	}

	public TrackMaterialFactory lang(String langName) {
		this.langName = langName;
		return this;
	}

	public TrackMaterialFactory block(Supplier<BlockEntry<? extends TrackBlock>> trackBlock) {
		this.trackBlock = trackBlock;
		return this;
	}

	public TrackMaterialFactory setBuiltin() {
		this.modelHolder = TrackMaterial.TrackModelHolder.DEFAULT;
		return this;
	}

	public TrackMaterialFactory sleeper(Ingredient sleeperIngredient) {
		this.sleeperIngredient = sleeperIngredient;
		return this;
	}

	public TrackMaterialFactory sleeper(ItemLike... items) {
		this.sleeperIngredient = Ingredient.of(items);
		return this;
	}

	public TrackMaterialFactory rails(Ingredient railsIngredient) {
		this.railsIngredient = railsIngredient;
		return this;
	}

	public TrackMaterialFactory rails(ItemLike... items) {
		this.railsIngredient = Ingredient.of(items);
		return this;
	}

	public TrackMaterialFactory noRecipeGen() {
		this.railsIngredient = Ingredient.EMPTY;
		this.sleeperIngredient = Ingredient.EMPTY;
		return this;
	}

	public TrackMaterialFactory particle(ResourceLocation particle) {
		this.particle = particle;
		return this;
	}

	public TrackMaterialFactory trackType(TrackMaterial.TrackType trackType) {
		this.trackType = trackType;
		return this;
	}

	public TrackMaterialFactory defaultModels() {
		String namespace = id.getNamespace();
		String prefix = "block/track/" + id.getPath() + "/";
		tieModel = new PartialModel(new ResourceLocation(namespace, prefix + "tie"));
		leftSegmentModel = new PartialModel(new ResourceLocation(namespace, prefix + "segment_left"));
		rightSegmentModel = new PartialModel(new ResourceLocation(namespace, prefix + "segment_right"));
		return this;
	}

	public TrackMaterialFactory customModels(PartialModel tieModel, PartialModel leftSegmentModel, PartialModel rightSegmentModel) {
		this.tieModel = tieModel;
		this.leftSegmentModel = leftSegmentModel;
		this.rightSegmentModel = rightSegmentModel;
		return this;
	}

	public TrackMaterial build() {
		assert trackBlock != null;
		assert langName != null;
		assert particle != null;
		assert trackType != null;
		assert sleeperIngredient != null;
		assert railsIngredient != null;
		assert id != null;
		assert modelHolder != null;
		if (tieModel != null || leftSegmentModel != null || rightSegmentModel != null) {
			assert tieModel != null && leftSegmentModel != null && rightSegmentModel != null;
			modelHolder = new TrackMaterial.TrackModelHolder(tieModel, leftSegmentModel, rightSegmentModel);
		}
		return new TrackMaterial(id, langName, trackBlock, particle, sleeperIngredient, railsIngredient, trackType, modelHolder);
	}
}
