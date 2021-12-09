package com.simibubi.create.content.contraptions.relays.encased;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;

import net.minecraft.world.level.block.state.BlockState;

public class ShaftInstance extends SingleRotatingInstance {

	public ShaftInstance(MaterialManager dispatcher, KineticTileEntity tile) {
		super(dispatcher, tile);
	}

	@Override
	protected BlockState getRenderedBlockState() {
		return shaft();
	}

}
