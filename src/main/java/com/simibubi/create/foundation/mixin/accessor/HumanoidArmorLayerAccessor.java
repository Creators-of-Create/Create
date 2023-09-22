package com.simibubi.create.foundation.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

@Mixin(HumanoidArmorLayer.class)
public interface HumanoidArmorLayerAccessor {
	@Accessor("ARMOR_LOCATION_CACHE")
	static Map<String, ResourceLocation> create$getArmorLocationCache() {
		throw new RuntimeException();
	}

	@Accessor("innerModel")
	HumanoidModel<?> create$getInnerModel();

	@Accessor("outerModel")
	HumanoidModel<?> create$getOuterModel();

	@Invoker("setPartVisibility")
	void create$callSetPartVisibility(HumanoidModel<?> model, EquipmentSlot slot);
	
}
