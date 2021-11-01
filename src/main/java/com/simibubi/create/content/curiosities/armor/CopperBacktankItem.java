package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.content.curiosities.armor.CapacityEnchantment.ICapacityEnchantable;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;

import net.minecraft.world.item.Item.Properties;

public class CopperBacktankItem extends CopperArmorItem implements ICapacityEnchantable {

	public static final int DURABILITY_BAR = 0xefefef;
	private BlockItem blockItem;

	public CopperBacktankItem(Properties p_i48534_3_, BlockItem blockItem) {
		super(EquipmentSlot.CHEST, p_i48534_3_);
		this.blockItem = blockItem;
	}

	@Override
	public InteractionResult useOn(UseOnContext p_195939_1_) {
		return blockItem.useOn(p_195939_1_);
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
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return DURABILITY_BAR;
	}

	@Override
	public void fillItemCategory(CreativeModeTab p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
		if (!allowdedIn(p_150895_1_))
			return;
		
		ItemStack stack = new ItemStack(this);
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("Air", BackTankUtil.maxAirWithoutEnchants());
		stack.setTag(nbt);
		p_150895_2_.add(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1 - Mth
			.clamp(getRemainingAir(stack) / ((float) BackTankUtil.maxAir(stack)), 0, 1);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	public static int getRemainingAir(ItemStack stack) {
		CompoundTag orCreateTag = stack.getOrCreateTag();
		return orCreateTag.getInt("Air");
	}
	
}
