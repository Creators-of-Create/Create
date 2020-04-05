package com.simibubi.create.modules.contraptions;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;

public class KineticDebugger {

	public static void renderSourceOutline(MatrixStack ms, IRenderTypeBuffer buffer) {
		if (!isActive())
			return;
		KineticTileEntity te = getSelectedTE();
		if (te == null)
			return;

		World world = Minecraft.getInstance().world;
		BlockPos toOutline = te.hasSource() ? te.source : te.getPos();
		BlockState state = te.getBlockState();
		VoxelShape shape = world.getBlockState(toOutline).getShape(world, toOutline);

		IVertexBuilder vb = buffer.getBuffer(RenderType.getLines());

		ms.push();
		ms.translate(toOutline.getX(), toOutline.getY(), toOutline.getZ());
		float f = 1 + 1 / 128f;
		ms.scale(f, f, f);

		WorldRenderer.func_228431_a_(ms, vb, shape, 0, 0, 0, te.hasSource() ? .5f : 1, .75f, .75f, 1);
		
		Vec3i offset = te.getPos().subtract(toOutline);
		ms.translate(offset.getX(), offset.getY(), offset.getZ());
		
		if (state.getBlock() instanceof IRotate) {
			Axis axis = ((IRotate)state.getBlock()).getRotationAxis(state);
			switch (axis) {
			case X:
				vb.vertex(ms.peek().getModel(), 0, 0.5f, 0.5f).color(1f, 1f, 1f, 1f).endVertex();
				vb.vertex(ms.peek().getModel(), 1, 0.5f, 0.5f).color(1f, 1f, 1f, 1f).endVertex();
				break;
			case Y:
				vb.vertex(ms.peek().getModel(), 0.5f, 0, 0.5f).color(1f, 1f, 1f, 1f).endVertex();
				vb.vertex(ms.peek().getModel(), 0.5f, 1, 0.5f).color(1f, 1f, 1f, 1f).endVertex();
				break;
			case Z:
				vb.vertex(ms.peek().getModel(), 0.5f, 0.5f, 0).color(1f, 1f, 1f, 1f).endVertex();
				vb.vertex(ms.peek().getModel(), 0.5f, 0.5f, 1).color(1f, 1f, 1f, 1f).endVertex();
				break;
			}
		}

		ms.pop();
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
		if (te == null || !(te instanceof KineticTileEntity))
			return null;

		return (KineticTileEntity) te;
	}

}
