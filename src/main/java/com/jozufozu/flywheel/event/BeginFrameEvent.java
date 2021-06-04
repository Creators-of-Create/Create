package com.jozufozu.flywheel.event;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.eventbus.api.Event;

public class BeginFrameEvent extends Event {
	private final ClientWorld world;
	private final MatrixStack stack;
	private final ActiveRenderInfo info;
	private final GameRenderer gameRenderer;
	private final LightTexture lightTexture;

	public BeginFrameEvent(ClientWorld world, MatrixStack stack, ActiveRenderInfo info, GameRenderer gameRenderer, LightTexture lightTexture) {
		this.world = world;
		this.stack = stack;
		this.info = info;
		this.gameRenderer = gameRenderer;
		this.lightTexture = lightTexture;
	}

	public ClientWorld getWorld() {
		return world;
	}

	public MatrixStack getStack() {
		return stack;
	}

	public ActiveRenderInfo getInfo() {
		return info;
	}

	public GameRenderer getGameRenderer() {
		return gameRenderer;
	}

	public LightTexture getLightTexture() {
		return lightTexture;
	}
}
