package com.simibubi.create.foundation.block;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public class BreakProgressHook {

	public static void whenBreaking(ClientLevel world, LevelRenderer renderer, int playerEntityId, BlockPos pos, int progress) {
		if (AllBlocks.BELT.has(world.getBlockState(pos))) {
			BeltTileEntity belt = (BeltTileEntity) world.getBlockEntity(pos);

			for (BlockPos beltPos : BeltBlock.getBeltChain(world, belt.getController())) {
				renderer.destroyBlockProgress(beltPos.hashCode(), beltPos, progress);
			}
		}
	}
}
