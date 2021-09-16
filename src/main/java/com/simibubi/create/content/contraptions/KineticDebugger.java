package com.simibubi.create.content.contraptions;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

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

		Level world = Minecraft.getInstance().level;
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
			Vec3 vec = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, axis)
					.getNormal());
			Vec3 center = VecHelper.getCenterOf(te.getBlockPos());
			CreateClient.OUTLINER.showLine("rotationAxis", center.add(vec), center.subtract(vec))
					.lineWidth(1 / 16f);
		}

	}

	public static boolean isActive() {
		return Minecraft.getInstance().options.renderDebug && AllConfigs.CLIENT.rainbowDebug.get();
	}

	public static KineticTileEntity getSelectedTE() {
		HitResult obj = Minecraft.getInstance().hitResult;
		ClientLevel world = Minecraft.getInstance().level;
		if (obj == null)
			return null;
		if (world == null)
			return null;
		if (!(obj instanceof BlockHitResult))
			return null;

		BlockHitResult ray = (BlockHitResult) obj;
		BlockEntity te = world.getBlockEntity(ray.getBlockPos());
		if (!(te instanceof KineticTileEntity))
			return null;

		return (KineticTileEntity) te;
	}

}
