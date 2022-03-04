package com.simibubi.create.lib.mixin.client;

import com.simibubi.create.content.curiosities.armor.AllArmorMaterials;
import com.simibubi.create.content.curiosities.armor.CopperArmorItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;

@Environment(EnvType.CLIENT)
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
	@Inject(method = "getArmorLocation", at = @At("HEAD"), cancellable = true)
	private void create$getArmorLocation(ArmorItem armorItem, boolean bl, String string, CallbackInfoReturnable<ResourceLocation> cir) {
		if (armorItem.getMaterial() == AllArmorMaterials.COPPER) {
			cir.setReturnValue(CopperArmorItem.TEXTURE);
		}
	}
}
