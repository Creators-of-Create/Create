package com.simibubi.create.content.contraptions;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Color;
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

		World world = Minecraft.getInstance().level;
		BlockPos toOutline = te.hasSource() ? te.source : te.getBlockPos();
		BlockState state = te.getBlockState();
		VoxelShape shape = world.getBlockState(toOutline)
			.getBlockSupportShape(world, toOutline);

		if (te.getTheoreticalSpeed() != 0 && !shape.isEmpty())
			CreateClient.OUTLINER.chaseAABB("kineticSource", shape.bounds()
					.move(toOutline))
					.lineWidth(1 / 16f)
					.colored(te.hasSource() ? Color.generateFromLong(te.network).getRGB() : 0xffcc00);

		if (state.getBlock() instanceof IRotate) {
			Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
			Vector3d vec = Vector3d.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, axis)
					.getNormal());
			Vector3d center = VecHelper.getCenterOf(te.getBlockPos());
			CreateClient.OUTLINER.showLine("rotationAxis", center.add(vec), center.subtract(vec))
					.lineWidth(1 / 16f);
		}

	}

	public static boolean isActive() {
		return Minecraft.getInstance().options.renderDebug && AllConfigs.CLIENT.rainbowDebug.get();
	}

	public static KineticTileEntity getSelectedTE() {
		RayTraceResult obj = Minecraft.getInstance().hitResult;
		ClientWorld world = Minecraft.getInstance().level;
		if (obj == null)
			return null;
		if (world == null)
			return null;
		if (!(obj instanceof BlockRayTraceResult))
			return null;

		BlockRayTraceResult ray = (BlockRayTraceResult) obj;
		TileEntity te = world.getBlockEntity(ray.getBlockPos());
		if (!(te instanceof KineticTileEntity))
			return null;

		return (KineticTileEntity) te;
	}

}
