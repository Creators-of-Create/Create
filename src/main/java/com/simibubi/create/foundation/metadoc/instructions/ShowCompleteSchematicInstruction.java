package com.simibubi.create.foundation.metadoc.instructions;

import com.simibubi.create.foundation.metadoc.MetaDocInstruction;
import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.WorldSectionElement;

import net.minecraft.util.math.BlockPos;

public class ShowCompleteSchematicInstruction extends MetaDocInstruction {

	@Override
	public void tick(MetaDocScene scene) {
		scene.addElement(new WorldSectionElement.Cuboid(BlockPos.ZERO, scene.getBounds()
			.getLength()));
	}

	@Override
	public boolean isComplete() {
		return true;
	}

}
