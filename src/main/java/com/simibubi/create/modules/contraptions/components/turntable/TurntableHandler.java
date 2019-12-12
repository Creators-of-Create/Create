package com.simibubi.create.modules.contraptions.components.turntable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.client.Minecraft;

public class TurntableHandler {

	public static void gameRenderTick() {
		Minecraft mc = Minecraft.getInstance();

		if (!AllBlocks.TURNTABLE.typeOf(mc.world.getBlockState(mc.player.getPosition())))
			return;

		if (!mc.player.onGround)
			return;

		if (mc.isGamePaused())
			return;

		KineticTileEntity te = (KineticTileEntity) mc.world.getTileEntity(mc.player.getPosition());
		float speed = te.getSpeed() / 19;
		mc.player.rotationYaw = mc.player.prevRotationYaw - speed * mc.getRenderPartialTicks();
		mc.player.renderYawOffset = mc.player.rotationYaw;
	}
	
}
