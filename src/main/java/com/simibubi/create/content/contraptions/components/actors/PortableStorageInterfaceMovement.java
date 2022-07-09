package com.simibubi.create.content.contraptions.components.actors;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraption;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PortableStorageInterfaceMovement implements MovementBehaviour {

	static final String _workingPos_ = "WorkingPos";
	static final String _clientPrevPos_ = "ClientPrevPos";

	@Override
	public Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.atLowerCornerOf(context.state.getValue(PortableStorageInterfaceBlock.FACING)
			.getNormal())
			.scale(1.85f);
	}

	@Override
	public boolean hasSpecialInstancedRendering() {
		return true;
	}

	@Nullable
	@Override
	public ActorInstance createInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld,
		MovementContext context) {
		return new PSIActorInstance(materialManager, simulationWorld, context);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		if (!ContraptionRenderDispatcher.canInstance())
			PortableStorageInterfaceRenderer.renderInContraption(context, renderWorld, matrices, buffer);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		boolean onCarriage = context.contraption instanceof CarriageContraption;
		if (onCarriage && context.motion.length() > 1 / 4f)
			return;
		if (!findInterface(context, pos))
			context.data.remove(_workingPos_);
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world.isClientSide)
			getAnimation(context).tickChaser();

		boolean onCarriage = context.contraption instanceof CarriageContraption;
		if (onCarriage && context.motion.length() > 1 / 4f)
			return;

		if (context.world.isClientSide) {
			BlockPos pos = new BlockPos(context.position);
			if (!findInterface(context, pos))
				reset(context);
			return;
		}

		if (!context.data.contains(_workingPos_))
			return;

		BlockPos pos = NbtUtils.readBlockPos(context.data.getCompound(_workingPos_));
		Vec3 target = VecHelper.getCenterOf(pos);

		if (!context.stall && !onCarriage
			&& context.position.closerThan(target, target.distanceTo(context.position.add(context.motion))))
			context.stall = true;

		Optional<Direction> currentFacingIfValid = getCurrentFacingIfValid(context);
		if (!currentFacingIfValid.isPresent())
			return;

		PortableStorageInterfaceTileEntity stationaryInterface =
			getStationaryInterfaceAt(context.world, pos, context.state, currentFacingIfValid.get());
		if (stationaryInterface == null) {
			reset(context);
			return;
		}

		if (stationaryInterface.connectedEntity == null)
			stationaryInterface.startTransferringTo(context.contraption, stationaryInterface.distance);

		boolean timerBelow = stationaryInterface.transferTimer <= PortableStorageInterfaceTileEntity.ANIMATION;
		stationaryInterface.keepAlive = 2;
		if (context.stall && timerBelow) {
			context.stall = false;
		}
	}

	protected boolean findInterface(MovementContext context, BlockPos pos) {
		if (context.contraption instanceof CarriageContraption cc && !cc.notInPortal())
			return false;
		Optional<Direction> currentFacingIfValid = getCurrentFacingIfValid(context);
		if (!currentFacingIfValid.isPresent())
			return false;

		Direction currentFacing = currentFacingIfValid.get();
		PortableStorageInterfaceTileEntity psi =
			findStationaryInterface(context.world, pos, context.state, currentFacing);

		if (psi == null)
			return false;
		if (psi.isPowered())
			return false;

		context.data.put(_workingPos_, NbtUtils.writeBlockPos(psi.getBlockPos()));
		if (!context.world.isClientSide) {
			Vec3 diff = VecHelper.getCenterOf(psi.getBlockPos())
				.subtract(context.position);
			diff = VecHelper.project(diff, Vec3.atLowerCornerOf(currentFacing.getNormal()));
			float distance = (float) (diff.length() + 1.85f - 1);
			psi.startTransferringTo(context.contraption, distance);
		} else {
			context.data.put(_clientPrevPos_, NbtUtils.writeBlockPos(pos));
			if (context.contraption instanceof CarriageContraption || context.contraption.entity.isStalled()
				|| context.motion.lengthSqr() == 0)
				getAnimation(context).chase(psi.getConnectionDistance() / 2, 0.25f, Chaser.LINEAR);
		}

		return true;
	}

	@Override
	public void stopMoving(MovementContext context) {
//		reset(context);
	}

	@Override
	public void cancelStall(MovementContext context) {
		reset(context);
	}

	public void reset(MovementContext context) {
		context.data.remove(_clientPrevPos_);
		context.data.remove(_workingPos_);
		context.stall = false;
		getAnimation(context).chase(0, 0.25f, Chaser.LINEAR);
	}

	private PortableStorageInterfaceTileEntity findStationaryInterface(Level world, BlockPos pos, BlockState state,
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

	private PortableStorageInterfaceTileEntity getStationaryInterfaceAt(Level world, BlockPos pos, BlockState state,
		Direction facing) {
		BlockEntity te = world.getBlockEntity(pos);
		if (!(te instanceof PortableStorageInterfaceTileEntity psi))
			return null;
		BlockState blockState = world.getBlockState(pos);
		if (blockState.getBlock() != state.getBlock())
			return null;
		if (blockState.getValue(PortableStorageInterfaceBlock.FACING) != facing.getOpposite())
			return null;
		if (psi.isPowered())
			return null;
		return psi;
	}

	private Optional<Direction> getCurrentFacingIfValid(MovementContext context) {
		Vec3 directionVec = Vec3.atLowerCornerOf(context.state.getValue(PortableStorageInterfaceBlock.FACING)
			.getNormal());
		directionVec = context.rotation.apply(directionVec);
		Direction facingFromVector = Direction.getNearest(directionVec.x, directionVec.y, directionVec.z);
		if (directionVec.distanceTo(Vec3.atLowerCornerOf(facingFromVector.getNormal())) > 1 / 2f)
			return Optional.empty();
		return Optional.of(facingFromVector);
	}

	public static LerpedFloat getAnimation(MovementContext context) {
		if (!(context.temporaryData instanceof LerpedFloat lf)) {
			LerpedFloat nlf = LerpedFloat.linear();
			context.temporaryData = nlf;
			return nlf;
		}
		return lf;
	}

}
