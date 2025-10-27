package com.simibubi.create.foundation.mixin;

import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.AllArmorMaterials;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;

@Mixin(ArmorTrim.class)
public abstract class ArmorTrimMixin {
	@Shadow
	@Final
	private Holder<TrimMaterial> material;

	@Shadow
	@Final
	private Holder<TrimPattern> pattern;

	@Shadow
	private static String getColorPaletteSuffix(Holder<TrimMaterial> trimMaterial, Holder<ArmorMaterial> armorMaterial) {
		throw new AssertionError();
	}

	@Unique
	private final BiFunction<Boolean, Holder<ArmorMaterial>, ResourceLocation> create$textureCardboard = Util.memoize((inner, armorMaterial) -> {
		String assetPath = pattern.value().assetId().getPath();
		String colorSuffix = getColorPaletteSuffix(material, armorMaterial);
		return Create.asResource("trims/models/armor/card_" + assetPath + (inner ? "_leggings_" : "_") + colorSuffix);
	});

	@Inject(method = "innerTexture", at = @At("HEAD"), cancellable = true)
	private void create$swapTexturesForCardboardTrimsInner(Holder<ArmorMaterial> armorMaterial, CallbackInfoReturnable<ResourceLocation> cir) {
		if (armorMaterial == AllArmorMaterials.CARDBOARD) {
			cir.setReturnValue(create$textureCardboard.apply(true, armorMaterial));
		}
	}

	@Inject(method = "outerTexture", at = @At("HEAD"), cancellable = true)
	private void create$swapTexturesForCardboardTrimsOuter(Holder<ArmorMaterial> armorMaterial, CallbackInfoReturnable<ResourceLocation> cir) {
		if (armorMaterial == AllArmorMaterials.CARDBOARD) {
			cir.setReturnValue(create$textureCardboard.apply(false, armorMaterial));
		}
	}
}
