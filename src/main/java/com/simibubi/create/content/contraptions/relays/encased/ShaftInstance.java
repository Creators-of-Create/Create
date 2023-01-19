package com.simibubi.create.content.contraptions.relays.encased;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

import net.minecraft.world.level.block.state.BlockState;

public class ShaftInstance extends SingleRotatingInstance {

	public ShaftInstance(MaterialManager materialManager, KineticBlockEntity blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected BlockState getRenderedBlockState() {
		return shaft();
	}

}
