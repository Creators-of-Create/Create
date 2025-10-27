package com.simibubi.create.foundation.mixin.datafixer;

import java.util.Map;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.simibubi.create.foundation.utility.DataFixerHelper;

import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V1460;

@Mixin(V1460.class)
public class V1460Mixin {
	@Inject(at = @At("RETURN"), method = "registerEntities")
	private void create$registerEntitiesToBeFixed(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> ci) {
		Map<String, Supplier<TypeTemplate>> map = ci.getReturnValue();

		for (DataFixerHelper.BlockPosFixer fixer : DataFixerHelper.BLOCK_POS_FIXERS_VIEW)
			if (fixer.reference() == References.ENTITY)
				schema.registerSimple(map, fixer.id());
	}

	@Inject(at = @At("RETURN"), method = "registerBlockEntities")
	private void create$registerBlockEntitiesToBeFixed(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> ci) {
		Map<String, Supplier<TypeTemplate>> map = ci.getReturnValue();

		for (DataFixerHelper.BlockPosFixer fixer : DataFixerHelper.BLOCK_POS_FIXERS_VIEW)
			if (fixer.reference() == References.BLOCK_ENTITY)
				schema.registerSimple(map, fixer.id());
	}
}
