package com.simibubi.create.content.contraptions.goggles;

import com.simibubi.create.AllItemsNew;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class GogglesItem extends Item {

	public GogglesItem(Properties properties) {
		super(properties);
		DispenserBlock.registerDispenseBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
	}

	@Override
	public EquipmentSlotType getEquipmentSlot(ItemStack stack) {
		return EquipmentSlotType.HEAD;
	}

	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		EquipmentSlotType equipmentslottype = MobEntity.getSlotForItemStack(itemstack);
		ItemStack itemstack1 = playerIn.getItemStackFromSlot(equipmentslottype);
		if (itemstack1.isEmpty()) {
			playerIn.setItemStackToSlot(equipmentslottype, itemstack.copy());
			itemstack.setCount(0);
			return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
		} else {
			return new ActionResult<>(ActionResultType.FAIL, itemstack);
		}
	}

	public static boolean canSeeParticles(PlayerEntity player) {
		for (ItemStack itemStack : player.getArmorInventoryList())
			if (AllItemsNew.typeOf(AllItemsNew.GOGGLES, itemStack))
				return true;
		return false;
	}

}
