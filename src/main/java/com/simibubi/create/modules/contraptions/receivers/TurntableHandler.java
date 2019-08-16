package com.simibubi.create.modules.contraptions.receivers;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class TurntableHandler {

	@SubscribeEvent
	public static void onRenderTick(RenderTickEvent event) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.world == null || mc.player == null)
			return;

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
