package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BlockItem;
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
			baseRequirement = ofBlockState(state, block);
		}

		// Behaviours can add additional required items
		if (te instanceof SmartTileEntity)
			baseRequirement = baseRequirement.with(((SmartTileEntity) te).getRequiredItems());

		return baseRequirement;
	}

	private static ItemRequirement ofBlockState(BlockState state, Block block) {
		if (block == Blocks.AIR)
			return NONE;

		Item item = BlockItem.BY_BLOCK.getOrDefault(state.getBlock(), Items.AIR);

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

		return item == Items.AIR ? INVALID : new ItemRequirement(ItemUseType.CONSUME, item);
	}

	public static ItemRequirement of(Entity entity) {
		EntityType<?> type = entity.getType();

		if (entity instanceof ISpecialEntityItemRequirement)
			return ((ISpecialEntityItemRequirement) entity).getRequiredItems();

		if (type == EntityType.ITEM_FRAME) {
			ItemFrame ife = (ItemFrame) entity;
			ItemStack frame = new ItemStack(Items.ITEM_FRAME);
			ItemStack displayedItem = ife.getItem();
			if (displayedItem.isEmpty())
				return new ItemRequirement(ItemUseType.CONSUME, Items.ITEM_FRAME);
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(frame, displayedItem));
		}

		if (type == EntityType.PAINTING)
			return new ItemRequirement(ItemUseType.CONSUME, Items.PAINTING);

		if (type == EntityType.ARMOR_STAND) {
			List<ItemStack> requirements = new ArrayList<>();
			ArmorStand armorStandEntity = (ArmorStand) entity;
			armorStandEntity.getAllSlots().forEach(requirements::add);
			requirements.add(new ItemStack(Items.ARMOR_STAND));
			return new ItemRequirement(ItemUseType.CONSUME, requirements);
		}

		if (entity instanceof AbstractMinecart) {
			AbstractMinecart minecartEntity = (AbstractMinecart) entity;
			return new ItemRequirement(ItemUseType.CONSUME, minecartEntity.getCartItem().getItem());
		}

		if (entity instanceof Boat) {
			Boat boatEntity = (Boat) entity;
			return new ItemRequirement(ItemUseType.CONSUME, boatEntity.getDropItem());
		}

		if (type == EntityType.END_CRYSTAL)
			return new ItemRequirement(ItemUseType.CONSUME, Items.END_CRYSTAL);

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
