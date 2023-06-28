package com.simibubi.create.foundation.item;

import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.mixin.accessor.HumanoidArmorLayerAccessor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface LayeredArmorItem extends CustomRenderedArmorItem {
	@OnlyIn(Dist.CLIENT)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	default void renderArmorPiece(HumanoidArmorLayer<?, ?, ?> layer, PoseStack poseStack,
			MultiBufferSource bufferSource, LivingEntity entity, EquipmentSlot slot, int light,
			HumanoidModel<?> originalModel, ItemStack stack) {
		if (!(stack.getItem() instanceof ArmorItem item)) {
			return;
		}
		if (!item.canEquip(stack, slot, entity)) {
			return;
		}

		HumanoidArmorLayerAccessor accessor = (HumanoidArmorLayerAccessor) layer;
		Map<String, ResourceLocation> locationCache = HumanoidArmorLayerAccessor.create$getArmorLocationCache();
		boolean glint = stack.hasFoil();

		HumanoidModel<?> innerModel = accessor.create$getInnerModel();
		layer.getParentModel().copyPropertiesTo((HumanoidModel) innerModel);
		accessor.create$callSetPartVisibility(innerModel, slot);
		String locationStr2 = getArmorTextureLocation(entity, slot, stack, 2);
		ResourceLocation location2 = locationCache.computeIfAbsent(locationStr2, ResourceLocation::new);
		accessor.create$callRenderModel(poseStack, bufferSource, light, glint, innerModel, 1.0F, 1.0F, 1.0F, location2);

		HumanoidModel<?> outerModel = accessor.create$getOuterModel();
		layer.getParentModel().copyPropertiesTo((HumanoidModel) outerModel);
		accessor.create$callSetPartVisibility(outerModel, slot);
		String locationStr1 = getArmorTextureLocation(entity, slot, stack, 1);
		ResourceLocation location1 = locationCache.computeIfAbsent(locationStr1, ResourceLocation::new);
		accessor.create$callRenderModel(poseStack, bufferSource, light, glint, outerModel, 1.0F, 1.0F, 1.0F, location1);
	}

	String getArmorTextureLocation(LivingEntity entity, EquipmentSlot slot, ItemStack stack, int layer);
}
