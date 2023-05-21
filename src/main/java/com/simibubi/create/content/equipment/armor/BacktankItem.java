package com.simibubi.create.content.equipment.armor;

import java.util.Locale;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.equipment.armor.CapacityEnchantment.ICapacityEnchantable;
import com.simibubi.create.foundation.item.LayeredArmorItem;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public class BacktankItem extends BaseArmorItem implements ICapacityEnchantable {
	public static final EquipmentSlot SLOT = EquipmentSlot.CHEST;
	public static final int BAR_COLOR = 0xEFEFEF;
	
	private final Supplier<BacktankBlockItem> blockItem;

	public BacktankItem(ArmorMaterial material, Properties properties, ResourceLocation textureLoc, Supplier<BacktankBlockItem> placeable) {
		super(material, SLOT, properties, textureLoc);
		this.blockItem = placeable;
	}

	@Nullable
	public static BacktankItem getWornBy(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity)) {
			return null;
		}
		if (!(livingEntity.getItemBySlot(SLOT).getItem() instanceof BacktankItem item)) {
			return null;
		}
		return item;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		return blockItem.get()
			.useOn(ctx);
	}

	@Override
	public boolean canBeDepleted() {
		return false;
	}

	@Override
	public boolean isEnchantable(ItemStack p_77616_1_) {
		return true;
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
		if (!allowedIn(tab))
			return;

		ItemStack stack = new ItemStack(this);
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("Air", BacktankUtil.maxAirWithoutEnchants());
		stack.setTag(nbt);
		items.add(stack);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * Mth.clamp(getRemainingAir(stack) / ((float) BacktankUtil.maxAir(stack)), 0, 1));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return BAR_COLOR;
	}

	public Block getBlock() {
		return blockItem.get().getBlock();
	}

	public static int getRemainingAir(ItemStack stack) {
		CompoundTag orCreateTag = stack.getOrCreateTag();
		return orCreateTag.getInt("Air");
	}

	public static class BacktankBlockItem extends BlockItem {
		private final Supplier<Item> actualItem;

		public BacktankBlockItem(Block block, Supplier<Item> actualItem, Properties properties) {
			super(block, properties);
			this.actualItem = actualItem;
		}

		@Override
		public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {}

		@Override
		public String getDescriptionId() {
			return this.getOrCreateDescriptionId();
		}

		public Item getActualItem() {
			return actualItem.get();
		}
	}

	public static class Layered extends BacktankItem implements LayeredArmorItem {
		public Layered(ArmorMaterial material, Properties properties, ResourceLocation textureLoc, Supplier<BacktankBlockItem> placeable) {
			super(material, properties, textureLoc, placeable);
		}

		@Override
		public String getArmorTextureLocation(LivingEntity entity, EquipmentSlot slot, ItemStack stack, int layer) {
			return String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d.png", textureLoc.getNamespace(), textureLoc.getPath(), layer);
		}
	}
}
