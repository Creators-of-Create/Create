package com.simibubi.create.modules.contraptions.relays.belt;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class BeltTunnelTileEntity extends SyncedTileEntity {

	public BeltTunnelTileEntity() {
		super(AllTileEntities.BELT_TUNNEL.type);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {

		if (AllBlocks.BELT.typeOf(world.getBlockState(pos.down()))) {
			TileEntity teBelow = world.getTileEntity(pos.down());
			if (teBelow != null)
				return teBelow.getCapability(cap, Direction.UP);
		}

		return super.getCapability(cap, side);
	}

}
