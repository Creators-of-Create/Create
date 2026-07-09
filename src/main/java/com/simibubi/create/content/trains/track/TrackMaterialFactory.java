package com.simibubi.create.content.trains.track;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.core.Holder;
import net.createmod.catnip.api.platform.CatnipServices;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

public class TrackMaterialFactory {
	private final Identifier id;
	private String langName;
	private NonNullSupplier<NonNullSupplier<? extends TrackBlock>> trackBlock;
	private Ingredient sleeperIngredient = emptyIngredient();
	private Ingredient railsIngredient = Ingredient.of(Items.IRON_NUGGET);
	private Identifier particle;
	private TrackMaterial.TrackType trackType = TrackMaterial.TrackType.STANDARD;

	@Nullable
	private TrackMaterial.TrackType.TrackBlockFactory customFactory = null;

	private TrackMaterial.TrackModelHolder modelHolder;
	private PartialModel tieModel;
	private PartialModel leftSegmentModel;
	private PartialModel rightSegmentModel;

	public TrackMaterialFactory(Identifier id) {
		this.id = id;
	}

	public static TrackMaterialFactory make(Identifier id) {  // Convenience function for static import
		return new TrackMaterialFactory(id);
	}

	private static Ingredient emptyIngredient() {
		return new Ingredient(EmptyTrackIngredient.INSTANCE);
	}

	private enum EmptyTrackIngredient implements ICustomIngredient {
		INSTANCE;

		@Override
		public boolean test(ItemStack stack) {
			return false;
		}

		@Override
		public Stream<Holder<Item>> items() {
			return Stream.empty();
		}

		@Override
		public boolean isSimple() {
			return true;
		}

		@Override
		public IngredientType<?> getType() {
			throw new UnsupportedOperationException("Empty track ingredients are recipe-generation sentinels and cannot be serialized");
		}

		@Override
		public SlotDisplay display() {
			return SlotDisplay.Empty.INSTANCE;
		}
	}

	public TrackMaterialFactory lang(String langName) {
		this.langName = langName;
		return this;
	}

	public TrackMaterialFactory block(NonNullSupplier<NonNullSupplier<? extends TrackBlock>> trackBlock) {
		this.trackBlock = trackBlock;
		return this;
	}

	public TrackMaterialFactory defaultModels() { // was setBuiltin
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> this.modelHolder = TrackMaterial.TrackModelHolder.DEFAULT);
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
		this.railsIngredient = emptyIngredient();
		this.sleeperIngredient = emptyIngredient();
		return this;
	}

	public TrackMaterialFactory particle(Identifier particle) {
		this.particle = particle;
		return this;
	}

	public TrackMaterialFactory trackType(TrackMaterial.TrackType trackType) {
		this.trackType = trackType;
		return this;
	}

	public TrackMaterialFactory standardModels() { // was defaultModels
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> {
			String namespace = id.getNamespace();
			String prefix = "block/track/" + id.getPath() + "/";
			tieModel = PartialModel.of(Identifier.fromNamespaceAndPath(namespace, prefix + "tie"));
			leftSegmentModel = PartialModel.of(Identifier.fromNamespaceAndPath(namespace, prefix + "segment_left"));
			rightSegmentModel = PartialModel.of(Identifier.fromNamespaceAndPath(namespace, prefix + "segment_right"));
		});
		return this;
	}

	public TrackMaterialFactory customModels(Supplier<Supplier<PartialModel>> tieModel, Supplier<Supplier<PartialModel>> leftSegmentModel, Supplier<Supplier<PartialModel>> rightSegmentModel) {
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> {
			this.tieModel = tieModel.get().get();
			this.leftSegmentModel = leftSegmentModel.get().get();
			this.rightSegmentModel = rightSegmentModel.get().get();
		});
		return this;
	}

	public TrackMaterialFactory customBlockFactory(TrackMaterial.TrackType.TrackBlockFactory factory) {
		this.customFactory = factory;
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
		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> {
			assert modelHolder != null;
			if (tieModel != null || leftSegmentModel != null || rightSegmentModel != null) {
				assert tieModel != null && leftSegmentModel != null && rightSegmentModel != null;
				modelHolder = new TrackMaterial.TrackModelHolder(tieModel, leftSegmentModel, rightSegmentModel);
			}
		});
		return new TrackMaterial(id, langName, trackBlock, particle, sleeperIngredient, railsIngredient, trackType, () -> () -> modelHolder, customFactory);
	}
}
