package com.simibubi.create.content.equipment.armor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public final class NetheriteDivingHandler {
	public static final String NETHERITE_DIVING_BITS_KEY = "CreateNetheriteDivingBits";
	public static final String FIRE_IMMUNE_KEY = "CreateFireImmune";

	@SubscribeEvent
	public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
		EquipmentSlot slot = event.getSlot();
		if (slot.getType() != EquipmentSlot.Type.ARMOR) {
			return;
		}

		LivingEntity entity = event.getEntityLiving();
		ItemStack to = event.getTo();

		if (slot == EquipmentSlot.HEAD) {
			if (to.getItem() instanceof DivingHelmetItem && isNetheriteArmor(to)) {
				setBit(entity, slot);
			} else {
				clearBit(entity, slot);
			}
		} else if (slot == EquipmentSlot.CHEST) {
			if (to.getItem() instanceof BacktankItem && isNetheriteArmor(to) && BacktankUtil.hasAirRemaining(to)) {
				setBit(entity, slot);
			} else {
				clearBit(entity, slot);
			}
		} else if (slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
			if (isNetheriteArmor(to)) {
				setBit(entity, slot);
			} else {
				clearBit(entity, slot);
			}
		}
	}

	public static boolean isNetheriteArmor(ItemStack stack) {
		return stack.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial() == ArmorMaterials.NETHERITE;
	}

	public static void setBit(LivingEntity entity, EquipmentSlot slot) {
		CompoundTag nbt = entity.getPersistentData();
		byte bits = nbt.getByte(NETHERITE_DIVING_BITS_KEY);
		if ((bits & 0b1111) == 0b1111) {
			return;
		}

		bits |= 1 << slot.getIndex();
		nbt.putByte(NETHERITE_DIVING_BITS_KEY, bits);

		if ((bits & 0b1111) == 0b1111) {
			setFireImmune(entity, true);
		}
	}

	public static void clearBit(LivingEntity entity, EquipmentSlot slot) {
		CompoundTag nbt = entity.getPersistentData();
		if (!nbt.contains(NETHERITE_DIVING_BITS_KEY)) {
			return;
		}

		byte bits = nbt.getByte(NETHERITE_DIVING_BITS_KEY);
		boolean prevFullSet = (bits & 0b1111) == 0b1111;
		bits &= ~(1 << slot.getIndex());
		nbt.putByte(NETHERITE_DIVING_BITS_KEY, bits);

		if (prevFullSet) {
			setFireImmune(entity, false);
		}
	}

	// TODO: sync to the client
	// The feature works without syncing because health and burning are calculated server-side and synced through vanilla code.
	// This method will not be called when the entity is wearing a full diving set on creation because the NBT values are persistent.
	public static void setFireImmune(LivingEntity entity, boolean fireImmune) {
		entity.getPersistentData().putBoolean(FIRE_IMMUNE_KEY, fireImmune);
	}
}
