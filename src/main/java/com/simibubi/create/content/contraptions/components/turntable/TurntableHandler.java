package com.simibubi.create.content.contraptions.components.turntable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

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

		BlockEntity tileEntity = mc.level.getBlockEntity(pos);
		if (!(tileEntity instanceof TurntableTileEntity))
			return;
		
		TurntableTileEntity turnTable = (TurntableTileEntity) tileEntity;
		float speed = turnTable.getSpeed() * 3/10;

		if (speed == 0)
			return;
		
		Vec3 origin = VecHelper.getCenterOf(pos);
		Vec3 offset = mc.player.position().subtract(origin);
		
		if (offset.length() > 1/4f)
			speed *= Mth.clamp((1/2f - offset.length()) * 2, 0, 1);

		mc.player.yRot = mc.player.yRotO - speed * AnimationTickHolder.getPartialTicks();
		mc.player.yBodyRot = mc.player.yRot;
	}

}
