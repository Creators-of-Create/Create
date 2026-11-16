package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(RabbitModel.class)
public interface RabbitModelAccessor {
	@Accessor("head")
	ModelPart create$getHead();
}
