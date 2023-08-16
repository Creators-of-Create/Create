package com.simibubi.create.content.schematics.client.tools;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;

public interface ISchematicTool {

	public void init();
	public void updateSelection();

	public boolean handleRightClick();
	public boolean handleMouseWheel(double delta);

	public void renderTool(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera);
	public void renderOverlay(ForgeGui gui, PoseStack poseStack, float partialTicks, int width, int height);
	public void renderOnSchematic(PoseStack ms, SuperRenderTypeBuffer buffer);

}
