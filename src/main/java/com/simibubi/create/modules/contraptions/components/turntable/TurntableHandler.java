package com.simibubi.create.modules.contraptions.components.turntable;

import com.simibubi.create.AllBlocksNew;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TurntableHandler {

	public static void gameRenderTick() {
		Minecraft mc = Minecraft.getInstance();
		BlockPos pos = mc.player.getPosition();

		if (!AllBlocksNew.TURNTABLE.has(mc.world.getBlockState(pos)))
			return;
		if (!mc.player.onGround)
			return;
		if (mc.isGamePaused())
			return;

		TileEntity tileEntity = mc.world.getTileEntity(pos);
		if (!(tileEntity instanceof TurntableTileEntity))
			return;
		
		TurntableTileEntity turnTable = (TurntableTileEntity) tileEntity;
		float speed = turnTable.getSpeed() * 3/10;

		if (speed == 0)
			return;
		
		Vec3d origin = VecHelper.getCenterOf(pos);
		Vec3d offset = mc.player.getPositionVec().subtract(origin);
		
		if (offset.length() > 1/4f)
			speed *= MathHelper.clamp((1/2f - offset.length()) * 2, 0, 1);

		mc.player.rotationYaw = mc.player.prevRotationYaw - speed * mc.getRenderPartialTicks();
		mc.player.renderYawOffset = mc.player.rotationYaw;
	}

}
