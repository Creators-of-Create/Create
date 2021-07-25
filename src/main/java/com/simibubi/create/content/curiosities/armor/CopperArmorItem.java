package com.simibubi.create.content.curiosities.armor;

import com.simibubi.create.Create;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public class CopperArmorItem extends ArmorItem {

	public CopperArmorItem(EquipmentSlotType p_i48534_2_, Properties p_i48534_3_) {
		super(AllArmorMaterials.COPPER, p_i48534_2_, p_i48534_3_.stacksTo(1));
	}

	public boolean isWornBy(Entity entity) {
		if (!(entity instanceof LivingEntity))
			return false;
		LivingEntity livingEntity = (LivingEntity) entity;
		return livingEntity.getItemBySlot(slot).getItem() == this;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
		return String.format("%s:textures/models/armor/copper.png", Create.ID);
	}

}
