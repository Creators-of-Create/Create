package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;

public class ItemRequirement {

	public enum ItemUseType {
		CONSUME, DAMAGE
	}

	public static class StackRequirement {
		public final ItemStack item;
		public final ItemUseType usage;

		public StackRequirement(ItemUseType usage, ItemStack item) {
			this.item = item;
			this.usage = usage;
		}
	}

	List<StackRequirement> requiredItems;

	public static final ItemRequirement INVALID = new ItemRequirement();
	public static final ItemRequirement NONE = new ItemRequirement();

	private ItemRequirement() {
	}

	public ItemRequirement(List<StackRequirement> requiredItems) {
		this.requiredItems = requiredItems;
	}

	public ItemRequirement(ItemUseType usage, ItemStack items) {
		this(Arrays.asList(new StackRequirement(usage, items)));
	}

	public ItemRequirement(ItemUseType usage, Item item) {
		this(usage, new ItemStack(item));
	}

	public ItemRequirement(ItemUseType usage, List<ItemStack> requiredItems) {
		this(requiredItems.stream().map(req -> new StackRequirement(usage, req)).collect(Collectors.toList()));
	}


	public static ItemRequirement of(BlockState state, BlockEntity te) {
		Block block = state.getBlock();

		ItemRequirement baseRequirement;
		if (block instanceof ISpecialBlockItemRequirement) {
			baseRequirement = ((ISpecialBlockItemRequirement) block).getRequiredItems(state, te);
		} else {
			baseRequirement = ofBlockState(state);
		}

		// Behaviours can add additional required items
		if (te instanceof SmartTileEntity)
			baseRequirement = baseRequirement.with(((SmartTileEntity) te).getRequiredItems());

		return baseRequirement;
	}

	private static ItemRequirement ofBlockState(BlockState state) {
		Block block = state.getBlock();
		if (block == Blocks.AIR)
			return NONE;

		Item item = block.asItem();
		if (item == Items.AIR)
			return INVALID;

		// double slab needs two items
		if (state.hasProperty(BlockStateProperties.SLAB_TYPE) && state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, 2)));
		if (block instanceof TurtleEggBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, state.getValue(TurtleEggBlock.EGGS).intValue())));
		if (block instanceof SeaPickleBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, state.getValue(SeaPickleBlock.PICKLES).intValue())));
		if (block instanceof SnowLayerBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, state.getValue(SnowLayerBlock.LAYERS).intValue())));
		if (block instanceof FarmBlock || block instanceof DirtPathBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(Items.DIRT)));

		return new ItemRequirement(ItemUseType.CONSUME, item);
	}

	public static ItemRequirement of(Entity entity) {
		if (entity instanceof ISpecialEntityItemRequirement)
			return ((ISpecialEntityItemRequirement) entity).getRequiredItems();

		if (entity instanceof ItemFrame itemFrame) {
			ItemStack frame = new ItemStack(Items.ITEM_FRAME);
			ItemStack displayedItem = itemFrame.getItem();
			if (displayedItem.isEmpty())
				return new ItemRequirement(ItemUseType.CONSUME, Items.ITEM_FRAME);
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(frame, displayedItem));
		}

		if (entity instanceof ArmorStand armorStand) {
			List<ItemStack> requirements = new ArrayList<>();
			requirements.add(new ItemStack(Items.ARMOR_STAND));
			armorStand.getAllSlots().forEach(requirements::add);
			return new ItemRequirement(ItemUseType.CONSUME, requirements);
		}

		ItemStack pickedStack = entity.getPickResult();
		if (pickedStack != null) {
			return new ItemRequirement(ItemUseType.CONSUME, pickedStack);
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

	public static boolean validate(ItemStack required, ItemStack present) {
		return required.isEmpty() || required.getItem() == present.getItem();
	}

	public ItemRequirement with(ItemRequirement other) {
		if (this.isInvalid() || other.isInvalid())
			return INVALID;
		if (this.isEmpty())
			return other;
		if (other.isEmpty())
			return this;

		return new ItemRequirement(
				Stream.concat(requiredItems.stream(), other.requiredItems.stream()).collect(Collectors.toList())
		);
	}

}
