package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(AgeableListModel.class)
public interface AgeableListModelAccessor {
	@Invoker("headParts")
	Iterable<ModelPart> create$callHeadParts();

	@Invoker("bodyParts")
	Iterable<ModelPart> create$callBodyParts();
}
