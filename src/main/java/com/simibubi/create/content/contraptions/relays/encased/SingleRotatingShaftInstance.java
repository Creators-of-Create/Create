package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;

public class SingleRotatingShaftInstance extends SingleRotatingInstance {

	public static void register(TileEntityType<? extends KineticTileEntity> type) {
		InstancedTileRenderRegistry.instance.register(type, SingleRotatingShaftInstance::new);
	}

	public SingleRotatingShaftInstance(InstancedTileRenderDispatcher dispatcher, KineticTileEntity tile) {
		super(dispatcher, tile);
	}

	@Override
	protected BlockState getRenderedBlockState() {
		return shaft(getRotationAxisOf(tile));
	}

}
