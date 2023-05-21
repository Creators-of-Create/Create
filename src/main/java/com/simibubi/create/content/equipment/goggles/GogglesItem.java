package com.simibubi.create.content.equipment.goggles;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllItems;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class GogglesItem extends Item {

	private static final List<Predicate<Player>> IS_WEARING_PREDICATES = new ArrayList<>();
	static {
		addIsWearingPredicate(player -> AllItems.GOGGLES.isIn(player.getItemBySlot(EquipmentSlot.HEAD)));
	}

	public GogglesItem(Properties properties) {
		super(properties);
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EquipmentSlot.HEAD;
	}

	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		EquipmentSlot equipmentslottype = Mob.getEquipmentSlotForItem(itemstack);
		ItemStack itemstack1 = playerIn.getItemBySlot(equipmentslottype);
		if (itemstack1.isEmpty()) {
			playerIn.setItemSlot(equipmentslottype, itemstack.copy());
			itemstack.setCount(0);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
		} else {
			return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
		}
	}

	public static boolean isWearingGoggles(Player player) {
		for (Predicate<Player> predicate : IS_WEARING_PREDICATES) {
			if (predicate.test(player)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Use this method to add custom entry points to the goggles overlay, e.g. custom
	 * armor, handheld alternatives, etc.
	 */
	public static void addIsWearingPredicate(Predicate<Player> predicate) {
		IS_WEARING_PREDICATES.add(predicate);
	}

}
