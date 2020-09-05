package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.*;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;

public class ItemRequirement {

	public enum ItemUseType {
		CONSUME, DAMAGE
	}

	ItemUseType usage;
	List<ItemStack> requiredItems;

	public static ItemRequirement INVALID = new ItemRequirement();
	public static ItemRequirement NONE = new ItemRequirement();

	private ItemRequirement() {
	}

	public ItemRequirement(ItemUseType usage, Item item) {
		this(usage, Arrays.asList(new ItemStack(item)));
	}

	public ItemRequirement(ItemUseType usage, List<ItemStack> requiredItems) {
		this.usage = usage;
		this.requiredItems = requiredItems;
	}

	public static ItemRequirement of(BlockState state) {
		Block block = state.getBlock();
		if (block == Blocks.AIR)
			return NONE;
		if (block instanceof ISpecialBlockItemRequirement)
			return ((ISpecialBlockItemRequirement) block).getRequiredItems(state);

		Item item = BlockItem.BLOCK_TO_ITEM.getOrDefault(state.getBlock(), Items.AIR);

		// double slab needs two items
		if (state.has(BlockStateProperties.SLAB_TYPE) && state.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)
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

	public List<ItemStack> getRequiredItems() {
		return requiredItems;
	}

	public ItemUseType getUsage() {
		return usage;
	}

	public static boolean validate(ItemStack required, ItemStack present) {
		return required.isEmpty() || required.getItem() == present.getItem();
	}

}
