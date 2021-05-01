package com.simibubi.create.content.contraptions.relays.encased;

import com.jozufozu.flywheel.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

import net.minecraft.block.BlockState;

public class ShaftInstance extends SingleRotatingInstance {

	public ShaftInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
		super(dispatcher, tile);
	}

	@Override
	protected BlockState getRenderedBlockState() {
		return shaft();
	}

}
