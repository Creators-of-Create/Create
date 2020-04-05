package com.simibubi.create.modules.contraptions;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;

public class KineticDebugger {

	public static void renderSourceOutline() {
		if (!isActive())
			return;
		KineticTileEntity te = getSelectedTE();
		if (te == null)
			return;

		World world = Minecraft.getInstance().world;
		BlockPos toOutline = te.hasSource() ? te.source : te.getPos();
		VoxelShape shape = world.getBlockState(toOutline).getShape(world, toOutline);

		TessellatorHelper.prepareForDrawing();
		GlStateManager.disableTexture();
		GlStateManager.lineWidth(3);
		GlStateManager.pushMatrix();
		GlStateManager.translated(toOutline.getX(), toOutline.getY(), toOutline.getZ());
		float f = 1 + 1 / 128f;
		GlStateManager.scaled(f, f, f);

		WorldRenderer.drawShape(shape, 0, 0, 0, te.hasSource() ? .5f : 1, .75f, .75f, 1);

		GlStateManager.popMatrix();
		GlStateManager.lineWidth(1);
		GlStateManager.enableTexture();
		TessellatorHelper.cleanUpAfterDrawing();
	}

	public static boolean isActive() {
		return Minecraft.getInstance().gameSettings.showDebugInfo && AllConfigs.CLIENT.rainbowDebug.get();
	}

	public static KineticTileEntity getSelectedTE() {
		RayTraceResult obj = Minecraft.getInstance().objectMouseOver;
		ClientWorld world = Minecraft.getInstance().world;
		if (obj == null)
			return null;
		if (world == null)
			return null;
		if (!(obj instanceof BlockRayTraceResult))
			return null;

		BlockRayTraceResult ray = (BlockRayTraceResult) obj;
		TileEntity te = world.getTileEntity(ray.getPos());
		if (!(te instanceof KineticTileEntity))
			return null;

		return (KineticTileEntity) te;
	}

}
