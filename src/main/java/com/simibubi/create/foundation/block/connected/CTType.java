package com.simibubi.create.foundation.block.connected;

import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.ContextRequirement;

import net.minecraft.resources.Identifier;

public interface CTType {
	Identifier getId();

	int getSheetSize();

	ContextRequirement getContextRequirement();

	int getTextureIndex(CTContext context);
}
