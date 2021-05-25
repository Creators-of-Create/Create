package com.simibubi.create.content.contraptions;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class KineticDebugger {

	public static void tick() {
		if (!isActive()) {
			if (KineticTileEntityRenderer.rainbowMode) {
				KineticTileEntityRenderer.rainbowMode = false;
				CreateClient.BUFFER_CACHE.invalidate();
			}			
			return;
		}
		
		KineticTileEntity te = getSelectedTE();
		if (te == null)
			return;

		World world = Minecraft.getInstance().world;
		BlockPos toOutline = te.hasSource() ? te.source : te.getPos();
		BlockState state = te.getBlockState();
		VoxelShape shape = world.getBlockState(toOutline)
			.getRenderShape(world, toOutline);

		if (te.getTheoreticalSpeed() != 0 && !shape.isEmpty())
			CreateClient.OUTLINER.chaseAABB("kineticSource", shape.getBoundingBox()
				.offset(toOutline))
				.lineWidth(1 / 16f)
				.colored(te.hasSource() ? ColorHelper.colorFromLong(te.network) : 0xffcc00);

		if (state.getBlock() instanceof IRotate) {
			Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
			Vector3d vec = Vector3d.of(Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis)
				.getDirectionVec());
			Vector3d center = VecHelper.getCenterOf(te.getPos());
			CreateClient.OUTLINER.showLine("rotationAxis", center.add(vec), center.subtract(vec))
				.lineWidth(1 / 16f);
		}

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
