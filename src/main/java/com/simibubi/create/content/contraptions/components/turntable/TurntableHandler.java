package com.simibubi.create.content.contraptions.components.turntable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class TurntableHandler {

	public static void gameRenderTick() {
		Minecraft mc = Minecraft.getInstance();
		BlockPos pos = mc.player.blockPosition();

		if (!AllBlocks.TURNTABLE.has(mc.level.getBlockState(pos)))
			return;
		if (!mc.player.isOnGround())
			return;
		if (mc.isPaused())
			return;

		TileEntity tileEntity = mc.level.getBlockEntity(pos);
		if (!(tileEntity instanceof TurntableTileEntity))
			return;
		
		TurntableTileEntity turnTable = (TurntableTileEntity) tileEntity;
		float speed = turnTable.getSpeed() * 3/10;

		if (speed == 0)
			return;
		
		Vector3d origin = VecHelper.getCenterOf(pos);
		Vector3d offset = mc.player.position().subtract(origin);
		
		if (offset.length() > 1/4f)
			speed *= MathHelper.clamp((1/2f - offset.length()) * 2, 0, 1);

		mc.player.yRot = mc.player.yRotO - speed * AnimationTickHolder.getPartialTicks();
		mc.player.yBodyRot = mc.player.yRot;
	}

}
