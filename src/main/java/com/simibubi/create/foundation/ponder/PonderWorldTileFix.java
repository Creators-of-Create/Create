package com.simibubi.create.foundation.ponder;

import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.foundation.tileEntity.IMultiTileContainer;

import net.createmod.ponder.foundation.PonderWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PonderWorldTileFix {

	public static void fixControllerTiles(PonderWorld world) {
		for (BlockEntity tileEntity : world.getTileEntities().values()) {

			if (tileEntity instanceof BeltTileEntity beltTileEntity) {
				if (!beltTileEntity.isController())
					continue;
				BlockPos controllerPos = tileEntity.getBlockPos();
				for (BlockPos blockPos : BeltBlock.getBeltChain(world, controllerPos)) {
					BlockEntity tileEntity2 = world.getBlockEntity(blockPos);
					if (!(tileEntity2 instanceof BeltTileEntity belt2))
						continue;
					belt2.setController(controllerPos);
				}
			}

			if (tileEntity instanceof IMultiTileContainer multiTile) {
				BlockPos lastKnown = multiTile.getLastKnownPos();
				BlockPos current = tileEntity.getBlockPos();
				if (lastKnown == null)
					continue;
				if (multiTile.isController())
					continue;
				if (!lastKnown.equals(current)) {
					BlockPos newControllerPos = multiTile.getController()
							.offset(current.subtract(lastKnown));
					multiTile.setController(newControllerPos);
				}
			}

		}
	}

}
