package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.GrassPathBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.TileEntity;

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

	public static ItemRequirement INVALID = new ItemRequirement();
	public static ItemRequirement NONE = new ItemRequirement();

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


	public static ItemRequirement of(BlockState state, TileEntity te) {
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

		Item item = BlockItem.BLOCK_TO_ITEM.getOrDefault(state.getBlock(), Items.AIR);

		// double slab needs two items
		if (state.contains(BlockStateProperties.SLAB_TYPE) && state.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, 2)));
		if (block instanceof TurtleEggBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, state.get(TurtleEggBlock.EGGS).intValue())));
		if (block instanceof SeaPickleBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, state.get(SeaPickleBlock.PICKLES).intValue())));
		if (block instanceof SnowBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(item, state.get(SnowBlock.LAYERS).intValue())));
		if (block instanceof GrassPathBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(Items.GRASS_BLOCK)));
		if (block instanceof FarmlandBlock)
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(new ItemStack(Items.DIRT)));

		return item == Items.AIR ? INVALID : new ItemRequirement(ItemUseType.CONSUME, item);
	}

	public static ItemRequirement of(Entity entity) {
		EntityType<?> type = entity.getType();

		if (entity instanceof ISpecialEntityItemRequirement)
			return ((ISpecialEntityItemRequirement) entity).getRequiredItems();

		if (type == EntityType.ITEM_FRAME) {
			ItemFrameEntity ife = (ItemFrameEntity) entity;
			ItemStack frame = new ItemStack(Items.ITEM_FRAME);
			ItemStack displayedItem = ife.getDisplayedItem();
			if (displayedItem.isEmpty())
				return new ItemRequirement(ItemUseType.CONSUME, Items.ITEM_FRAME);
			return new ItemRequirement(ItemUseType.CONSUME, Arrays.asList(frame, displayedItem));
		}

		if (type == EntityType.PAINTING)
			return new ItemRequirement(ItemUseType.CONSUME, Items.PAINTING);

		if (type == EntityType.ARMOR_STAND) {
			List<ItemStack> requirements = new ArrayList<>();
			ArmorStandEntity armorStandEntity = (ArmorStandEntity) entity;
			armorStandEntity.getEquipmentAndArmor().forEach(requirements::add);
			requirements.add(new ItemStack(Items.ARMOR_STAND));
			return new ItemRequirement(ItemUseType.CONSUME, requirements);
		}

		if (entity instanceof AbstractMinecartEntity) {
			AbstractMinecartEntity minecartEntity = (AbstractMinecartEntity) entity;
			return new ItemRequirement(ItemUseType.CONSUME, minecartEntity.getCartItem().getItem());
		}

		if (entity instanceof BoatEntity) {
			BoatEntity boatEntity = (BoatEntity) entity;
			return new ItemRequirement(ItemUseType.CONSUME, boatEntity.getItemBoat().getItem());
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
