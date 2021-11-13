package com.simibubi.create.lib.mixin.client;

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
public class HumanoidArmorLayerMixin {
	private static final ResourceLocation copperArmorLocation = new ResourceLocation("create", "textures/models/armor/copper.png");

	@Inject(at = @At("HEAD"), method = "getArmorLocation(Lnet/minecraft/world/item/ArmorItem;ZLjava/lang/String;)Lnet/minecraft/resources/ResourceLocation;", cancellable = true)
	private void getArmorLocation(ArmorItem armorItem, boolean bl, String string, CallbackInfoReturnable<ResourceLocation> cir) {
		if (armorItem.getMaterial().getName().equals("copper")) {
			cir.setReturnValue(copperArmorLocation);
		}
	}
}
