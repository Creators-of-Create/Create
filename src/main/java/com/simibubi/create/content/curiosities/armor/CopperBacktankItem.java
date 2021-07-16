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

import net.minecraft.item.Item.Properties;

public class CopperBacktankItem extends CopperArmorItem implements ICapacityEnchantable {

	public static final int DURABILITY_BAR = 0xefefef;
	private BlockItem blockItem;

	public CopperBacktankItem(Properties p_i48534_3_, BlockItem blockItem) {
		super(EquipmentSlotType.CHEST, p_i48534_3_);
		this.blockItem = blockItem;
	}

	@Override
	public ActionResultType useOn(ItemUseContext pContext) {
		return blockItem.useOn(pContext);
	}

	@Override
	public boolean canBeDepleted() {
		return false;
	}
	
	@Override
	public boolean isEnchantable(ItemStack pStack) {
		return true;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return DURABILITY_BAR;
	}

	@Override
	public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) {
		if (!allowdedIn(pGroup))
			return;
		
		ItemStack stack = new ItemStack(this);
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("Air", BackTankUtil.maxAirWithoutEnchants());
		stack.setTag(nbt);
		pItems.add(stack);
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
