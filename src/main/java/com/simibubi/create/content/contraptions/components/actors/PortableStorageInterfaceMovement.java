package com.simibubi.create.content.contraptions.components.actors;

import java.util.Optional;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PortableStorageInterfaceMovement extends MovementBehaviour {

	static final String _workingPos_ = "WorkingPos";
	static final String _clientPrevPos_ = "ClientPrevPos";

	@Override
	public Vector3d getActiveAreaOffset(MovementContext context) {
		return Vector3d.atLowerCornerOf(context.state.getValue(PortableStorageInterfaceBlock.FACING)
			.getNormal()).scale(1.85f);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld,
		ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		PortableStorageInterfaceRenderer.renderInContraption(context, renderWorld, matrices, buffer);
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
		PortableStorageInterfaceTileEntity psi =
			findStationaryInterface(context.world, pos, context.state, currentFacing);
		if (psi == null)
			return false;

		if ((psi.isTransferring() || psi.isPowered()) && !context.world.isClientSide)
			return false;
		context.data.put(_workingPos_, NBTUtil.writeBlockPos(psi.getBlockPos()));
		if (!context.world.isClientSide) {
			Vector3d diff = VecHelper.getCenterOf(psi.getBlockPos())
				.subtract(context.position);
			diff = VecHelper.project(diff, Vector3d.atLowerCornerOf(currentFacing.getNormal()));
			float distance = (float) (diff.length() + 1.85f - 1);
			psi.startTransferringTo(context.contraption, distance);
		} else {
			context.data.put(_clientPrevPos_, NBTUtil.writeBlockPos(pos));
		}
		return true;
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world.isClientSide) {
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
			getStationaryInterfaceAt(context.world, pos, context.state, currentFacingIfValid.get());
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

	private PortableStorageInterfaceTileEntity findStationaryInterface(World world, BlockPos pos, BlockState state,
		Direction facing) {
		for (int i = 0; i < 2; i++) {
			PortableStorageInterfaceTileEntity interfaceAt =
				getStationaryInterfaceAt(world, pos.relative(facing, i), state, facing);
			if (interfaceAt == null)
				continue;
			return interfaceAt;
		}
		return null;
	}

	private PortableStorageInterfaceTileEntity getStationaryInterfaceAt(World world, BlockPos pos, BlockState state,
		Direction facing) {
		TileEntity te = world.getBlockEntity(pos);
		if (!(te instanceof PortableStorageInterfaceTileEntity))
			return null;
		BlockState blockState = world.getBlockState(pos);
		if (blockState.getBlock() != state.getBlock())
			return null;
		if (blockState.getValue(PortableStorageInterfaceBlock.FACING) != facing.getOpposite())
			return null;
		return (PortableStorageInterfaceTileEntity) te;
	}

	private Optional<Direction> getCurrentFacingIfValid(MovementContext context) {
		Vector3d directionVec = Vector3d.atLowerCornerOf(context.state.getValue(PortableStorageInterfaceBlock.FACING)
			.getNormal());
		directionVec = context.rotation.apply(directionVec);
		Direction facingFromVector = Direction.getNearest(directionVec.x, directionVec.y, directionVec.z);
		if (directionVec.distanceTo(Vector3d.atLowerCornerOf(facingFromVector.getNormal())) > 1 / 2f)
			return Optional.empty();
		return Optional.of(facingFromVector);
	}

}
