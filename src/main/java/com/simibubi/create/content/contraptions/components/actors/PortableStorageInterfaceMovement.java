package com.simibubi.create.content.contraptions.components.actors;

import java.util.Optional;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PortableStorageInterfaceMovement extends MovementBehaviour {

	static final String _workingPos_ = "WorkingPos";
	static final String _clientPrevPos_ = "ClientPrevPos";

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(PortableStorageInterfaceBlock.FACING)
			.getDirectionVec()).scale(1.85f);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		PortableStorageInterfaceRenderer.renderInContraption(context, ms, msLocal, buffer);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		context.data.remove(_workingPos_);
		if (findInterface(context, pos))
			context.stall = true;
	}

	protected boolean findInterface(MovementContext context, BlockPos pos) {
		Optional<Direction> currentFacingIfValid = getCurrentFacingIfValid(context);
		if (!currentFacingIfValid.isPresent())
			return false;

		Direction currentFacing = currentFacingIfValid.get();
		PortableStorageInterfaceTileEntity psi = findStationaryInterface(context.world, pos, currentFacing);
		if (psi == null)
			return false;

		if (psi.isTransferring() && !context.world.isRemote)
			return false;
		context.data.put(_workingPos_, NBTUtil.writeBlockPos(psi.getPos()));
		if (!context.world.isRemote) {
			Vec3d diff = VecHelper.getCenterOf(psi.getPos())
				.subtract(context.position);
			diff = VecHelper.project(diff, new Vec3d(currentFacing.getDirectionVec()));
			float distance = (float) (diff.length() + 1.85f - 1);
			psi.startTransferringTo(context.contraption, distance);
		} else {
			context.data.put(_clientPrevPos_, NBTUtil.writeBlockPos(pos));
		}
		return true;
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world.isRemote) {
			boolean stalled = context.contraption.stalled;
			if (stalled && !context.data.contains(_workingPos_)) {
				BlockPos pos = new BlockPos(context.position);
				if (!context.data.contains(_clientPrevPos_)
					|| !NBTUtil.readBlockPos(context.data.getCompound(_clientPrevPos_))
						.equals(pos))
					findInterface(context, pos);
			}
			if (!stalled)
				reset(context);
			return;
		}

		if (!context.data.contains(_workingPos_))
			return;

		BlockPos pos = NBTUtil.readBlockPos(context.data.getCompound(_workingPos_));
		Optional<Direction> currentFacingIfValid = getCurrentFacingIfValid(context);
		if (!currentFacingIfValid.isPresent())
			return;

		PortableStorageInterfaceTileEntity stationaryInterface =
			getStationaryInterfaceAt(context.world, pos, currentFacingIfValid.get());
		if (stationaryInterface == null || !stationaryInterface.isTransferring()) {
			reset(context);
			return;
		}
	}

	@Override
	public void stopMoving(MovementContext context) {
		reset(context);
	}

	public void reset(MovementContext context) {
		context.data.remove(_clientPrevPos_);
		context.data.remove(_workingPos_);
		context.stall = false;
	}

	private PortableStorageInterfaceTileEntity findStationaryInterface(World world, BlockPos pos, Direction facing) {
		for (int i = 0; i < 2; i++) {
			PortableStorageInterfaceTileEntity interfaceAt =
				getStationaryInterfaceAt(world, pos.offset(facing, i), facing);
			if (interfaceAt == null)
				continue;
			return interfaceAt;
		}
		return null;
	}

	private PortableStorageInterfaceTileEntity getStationaryInterfaceAt(World world, BlockPos pos, Direction facing) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof PortableStorageInterfaceTileEntity))
			return null;
		BlockState blockState = world.getBlockState(pos);
		if (!AllBlocks.PORTABLE_STORAGE_INTERFACE.has(blockState))
			return null;
		if (blockState.get(PortableStorageInterfaceBlock.FACING) != facing.getOpposite())
			return null;
		return (PortableStorageInterfaceTileEntity) te;
	}

	private Optional<Direction> getCurrentFacingIfValid(MovementContext context) {
		Vec3d directionVec = new Vec3d(context.state.get(PortableStorageInterfaceBlock.FACING)
			.getDirectionVec());
		directionVec = VecHelper.rotate(directionVec, context.rotation.x, context.rotation.y, context.rotation.z);
		Direction facingFromVector = Direction.getFacingFromVector(directionVec.x, directionVec.y, directionVec.z);
		if (directionVec.distanceTo(new Vec3d(facingFromVector.getDirectionVec())) > 1 / 8f)
			return Optional.empty();
		return Optional.of(facingFromVector);
	}

}
