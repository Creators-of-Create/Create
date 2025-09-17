package com.simibubi.create.content.schematics.requirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.schematic.requirement.SchematicRequirementRegistries;
import com.simibubi.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.api.schematic.requirement.SpecialEntityItemRequirement;
import com.simibubi.create.compat.framedblocks.FramedBlocksInSchematics;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.mixin.accessor.ItemFrameAccessor;

import net.createmod.catnip.components.ComponentProcessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

public class ItemRequirement {
	public static final ItemRequirement NONE = new ItemRequirement(Collections.emptyList());
	public static final ItemRequirement INVALID = new ItemRequirement(Collections.emptyList());

	protected List<StackRequirement> requiredItems;

	public ItemRequirement(List<StackRequirement> requiredItems) {
		this.requiredItems = requiredItems;
	}

	public ItemRequirement(StackRequirement stackRequirement) {
		this(List.of(stackRequirement));
	}

	public ItemRequirement(ItemUseType usage, ItemStack stack) {
		this(new StackRequirement(stack, usage));
	}

	public ItemRequirement(ItemUseType usage, Item item) {
		this(usage, new ItemStack(item));
	}

	public ItemRequirement(ItemUseType usage, List<ItemStack> requiredItems) {
		this(requiredItems.stream()
			.map(req -> new StackRequirement(req, usage))
			.collect(Collectors.toList()));
	}

	public static ItemRequirement of(BlockState state, @Nullable BlockEntity be) {
		Block block = state.getBlock();

		ItemRequirement requirement;
		SchematicRequirementRegistries.BlockRequirement blockRequirement = SchematicRequirementRegistries.BLOCKS.get(block);
		if (blockRequirement != null) {
			requirement = blockRequirement.getRequiredItems(state, be);
		} else if (block instanceof SpecialBlockItemRequirement specialBlock) {
			requirement = specialBlock.getRequiredItems(state, be);
		} else {
			requirement = defaultOf(state, be);
		}

		if (be != null) {
			SchematicRequirementRegistries.BlockEntityRequirement beRequirement = SchematicRequirementRegistries.BLOCK_ENTITIES.get(be.getType());
			if (beRequirement != null) {
				requirement = requirement.union(beRequirement.getRequiredItems(be, state));
			} else if (be instanceof SpecialBlockEntityItemRequirement specialBE) {
				requirement = requirement.union(specialBE.getRequiredItems(state));
			} else if (com.simibubi.create.compat.Mods.FRAMEDBLOCKS.contains(block)) {
				requirement = requirement.union(FramedBlocksInSchematics.getRequiredItems(state, be));
			}
		}

		return requirement;
	}

	private static ItemRequirement defaultOf(BlockState state, BlockEntity be) {
		Block block = state.getBlock();
		if (block == Blocks.AIR)
			return NONE;

		Item item = block.asItem();
		if (item == Items.AIR)
			return INVALID;

		// double slab needs two items
		if (state.hasProperty(BlockStateProperties.SLAB_TYPE)
			&& state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)
			return new ItemRequirement(ItemUseType.CONSUME, new ItemStack(item, 2));
		if (block instanceof TurtleEggBlock)
			return new ItemRequirement(ItemUseType.CONSUME, new ItemStack(item, state.getValue(TurtleEggBlock.EGGS)
				.intValue()));
		if (block instanceof SeaPickleBlock)
			return new ItemRequirement(ItemUseType.CONSUME, new ItemStack(item, state.getValue(SeaPickleBlock.PICKLES)
				.intValue()));
		if (block instanceof SnowLayerBlock)
			return new ItemRequirement(ItemUseType.CONSUME, new ItemStack(item, state.getValue(SnowLayerBlock.LAYERS)
				.intValue()));
		// FD's rich soil extends FarmBlock so this is to make sure the cost is correct (it should be rich soil not dirt)
		if (block == BuiltInRegistries.BLOCK.get(Mods.FD.asResource("rich_soil_farmland")))
			return new ItemRequirement(ItemUseType.CONSUME, BuiltInRegistries.ITEM.get(Mods.FD.asResource("rich_soil")));
		if (block instanceof FarmBlock || block instanceof DirtPathBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Items.DIRT);
		if (block instanceof AbstractBannerBlock && be instanceof BannerBlockEntity bannerBE)
			return new ItemRequirement(new StrictNbtStackRequirement(bannerBE.getItem(), ItemUseType.CONSUME));
		// Tall grass doesnt exist as a block so use 2 grass blades
		if (block == Blocks.TALL_GRASS)
			return new ItemRequirement(ItemUseType.CONSUME, new ItemStack(Items.SHORT_GRASS, 2));
		// Large ferns don't exist as blocks so use 2 ferns instead
		if (block == Blocks.LARGE_FERN)
			return new ItemRequirement(ItemUseType.CONSUME, new ItemStack(Items.FERN, 2));

		return new ItemRequirement(ItemUseType.CONSUME, item);
	}

	public static ItemRequirement of(Entity entity) {
		SchematicRequirementRegistries.EntityRequirement requirement = SchematicRequirementRegistries.ENTITIES.get(entity.getType());
		if (requirement != null) {
			return requirement.getRequiredItems(entity);
		} else if (entity instanceof SpecialEntityItemRequirement specialEntity) {
			return specialEntity.getRequiredItems();
		}

		if (entity instanceof ItemFrame itemFrame) {
			ItemStack frame = ((ItemFrameAccessor) itemFrame).create$getFrameItemStack();
			ItemStack displayedItem = ComponentProcessors.withUnsafeComponentsDiscarded(itemFrame.getItem());
			if (displayedItem.isEmpty())
				return new ItemRequirement(ItemUseType.CONSUME, frame);
			return new ItemRequirement(List.of(new ItemRequirement.StackRequirement(frame, ItemUseType.CONSUME),
				new ItemRequirement.StrictNbtStackRequirement(displayedItem, ItemUseType.CONSUME)));
		}

		if (entity instanceof ArmorStand armorStand) {
			List<StackRequirement> requirements = new ArrayList<>();
			requirements.add(new StackRequirement(new ItemStack(Items.ARMOR_STAND), ItemUseType.CONSUME));
			armorStand.getAllSlots()
				.forEach(s -> requirements
					.add(new StrictNbtStackRequirement(ComponentProcessors.withUnsafeComponentsDiscarded(s), ItemUseType.CONSUME)));
			return new ItemRequirement(requirements);
		}

		return INVALID;
	}

	public boolean isEmpty() {
		return NONE == this;
	}

	public boolean isInvalid() {
		return INVALID == this;
	}

	public List<StackRequirement> getRequiredItems() {
		return requiredItems;
	}

	public ItemRequirement union(ItemRequirement other) {
		if (this.isInvalid() || other.isInvalid())
			return INVALID;
		if (this.isEmpty())
			return other;
		if (other.isEmpty())
			return this;

		return new ItemRequirement(Stream.concat(requiredItems.stream(), other.requiredItems.stream())
			.collect(Collectors.toList()));
	}

	public enum ItemUseType {
		CONSUME, DAMAGE
	}

	public static class StackRequirement {
		public final ItemStack stack;
		public final ItemUseType usage;

		public StackRequirement(ItemStack stack, ItemUseType usage) {
			this.stack = stack;
			this.usage = usage;
		}

		public boolean matches(ItemStack other) {
			return ItemStack.isSameItem(stack, other);
		}
	}

	public static class StrictNbtStackRequirement extends StackRequirement {
		public StrictNbtStackRequirement(ItemStack stack, ItemUseType usage) {
			super(stack, usage);
		}

		@Override
		public boolean matches(ItemStack other) {
			return ItemStack.isSameItemSameComponents(stack, other);
		}
	}
}
