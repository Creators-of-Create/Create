package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.content.curiosities.armor.CapacityEnchantment.ICapacityEnchantable;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class CopperBacktankItem extends CopperArmorItem implements ICapacityEnchantable {

	public static final int DURABILITY_BAR = 0xEFEFEF;
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
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * Mth.clamp(getRemainingAir(stack) / ((float) BackTankUtil.maxAir(stack)), 0, 1));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return DURABILITY_BAR;
	}

	public static int getRemainingAir(ItemStack stack) {
		CompoundTag orCreateTag = stack.getOrCreateTag();
		return orCreateTag.getInt("Air");
	}
	
}
