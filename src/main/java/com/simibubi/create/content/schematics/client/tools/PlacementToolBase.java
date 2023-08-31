package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public abstract class PlacementToolBase extends SchematicToolBase {

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void updateSelection() {
		super.updateSelection();
	}

	@Override
	public void renderTool(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
		super.renderTool(ms, buffer, camera);
	}

	@Override
	public void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
		super.renderOverlay(gui, graphics, partialTicks, width, height);
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		return false;
	}

	@Override
	public boolean handleRightClick() {
		return false;
	}

}
