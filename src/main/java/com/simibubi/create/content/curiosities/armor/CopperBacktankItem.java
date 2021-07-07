package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.content.curiosities.armor.CapacityEnchantment.ICapacityEnchantable;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

public class CopperBacktankItem extends CopperArmorItem implements ICapacityEnchantable {

	public static final int DURABILITY_BAR = 0xefefef;
	private BlockItem blockItem;

	public CopperBacktankItem(Properties p_i48534_3_, BlockItem blockItem) {
		super(EquipmentSlotType.CHEST, p_i48534_3_);
		this.blockItem = blockItem;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext p_195939_1_) {
		return blockItem.onItemUse(p_195939_1_);
	}

	@Override
	public boolean isDamageable() {
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
	public void fillItemGroup(ItemGroup p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
		if (!isInGroup(p_150895_1_))
			return;
		
		ItemStack stack = new ItemStack(this);
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("Air", BackTankUtil.maxAirWithoutEnchants());
		stack.setTag(nbt);
		p_150895_2_.add(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1 - MathHelper
			.clamp(getRemainingAir(stack) / ((float) BackTankUtil.maxAir(stack)), 0, 1);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	public static int getRemainingAir(ItemStack stack) {
		CompoundNBT orCreateTag = stack.getOrCreateTag();
		return orCreateTag.getInt("Air");
	}
	
}
