package com.simibubi.create.foundation.blockEntity.behaviour.scrollValue;


import net.createmod.catnip.utility.animation.PhysicalFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ScrollValueHandler {

	private static float lastPassiveScroll = 0.0f;
	private static float passiveScroll = 0.0f;
	private static float passiveScrollDirection = 1f;
	public static final PhysicalFloat wrenchCog = PhysicalFloat.create()
		.withDrag(0.3);

	public static float getScroll(float partialTicks) {
		return wrenchCog.getValue(partialTicks) + Mth.lerp(partialTicks, lastPassiveScroll, passiveScroll);
	}

	@OnlyIn(Dist.CLIENT)
	public static void tick() {
		if (!Minecraft.getInstance()
			.isPaused()) {
			lastPassiveScroll = passiveScroll;
			wrenchCog.tick();
			passiveScroll += passiveScrollDirection * 0.5;
		}
	}

}
