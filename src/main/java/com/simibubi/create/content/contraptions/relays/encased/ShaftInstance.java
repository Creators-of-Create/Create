package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class ShaftInstance extends SingleRotatingInstance {

	public static void register(TileEntityType<? extends KineticTileEntity> type) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
				InstancedTileRenderRegistry.instance.register(type, ShaftInstance::new));
	}

	public ShaftInstance(InstancedTileRenderer dispatcher, KineticTileEntity tile) {
		super(dispatcher, tile);
	}

	@Override
	protected BlockState getRenderedBlockState() {
		return shaft(getRotationAxis());
	}

}
