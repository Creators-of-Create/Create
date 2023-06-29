package com.simibubi.create.foundation.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

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

	@Invoker("renderModel")
	void create$callRenderModel(PoseStack poseStack, MultiBufferSource bufferSource, int light, ArmorItem item, Model model, boolean glint, float red, float green, float blue, ResourceLocation armorResource);
}
