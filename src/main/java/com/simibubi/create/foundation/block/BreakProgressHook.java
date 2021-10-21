package com.simibubi.create.foundation.block;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class BreakProgressHook {

	public static void whenBreaking(ClientWorld world, WorldRenderer renderer, int playerEntityId, BlockPos pos, int progress) {
		if (AllBlocks.BELT.has(world.getBlockState(pos))) {
			BeltTileEntity belt = (BeltTileEntity) world.getBlockEntity(pos);

			for (BlockPos beltPos : BeltBlock.getBeltChain(world, belt.getController())) {
				renderer.destroyBlockProgress(beltPos.hashCode(), beltPos, progress);
			}
		}
	}
}
