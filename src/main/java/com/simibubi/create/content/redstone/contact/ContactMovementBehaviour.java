package com.simibubi.create.content.redstone.contact;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.foundation.utility.LegacyDirectionBridge;

import net.createmod.catnip.api.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;

public class ContactMovementBehaviour implements MovementBehaviour {

	@Override
	public Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.atLowerCornerOf(context.state.getValue(RedstoneContactBlock.FACING)
			.getUnitVec3i())
			.scale(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		BlockState block = context.state;
		Level world = context.world;

		if (world.isClientSide())
			return;
		if (context.firstMovement)
			return;

		deactivateLastVisitedContact(context);
		BlockState visitedState = world.getBlockState(pos);
		if (!AllBlocks.REDSTONE_CONTACT.has(visitedState) && !AllBlocks.ELEVATOR_CONTACT.has(visitedState))
			return;

		Vec3 contact = Vec3.atLowerCornerOf(block.getValue(RedstoneContactBlock.FACING)
			.getUnitVec3i());
		contact = context.rotation.apply(contact);
		Direction direction = LegacyDirectionBridge.nearest(contact.x, contact.y, contact.z, Direction.NORTH);

		if (visitedState.getValue(RedstoneContactBlock.FACING) != direction.getOpposite())
			return;

		if (AllBlocks.REDSTONE_CONTACT.has(visitedState))
			world.setBlockAndUpdate(pos, visitedState.setValue(RedstoneContactBlock.POWERED, true));
		if (AllBlocks.ELEVATOR_CONTACT.has(visitedState) && context.contraption instanceof ElevatorContraption ec)
			ec.broadcastFloorData(world, pos);

		context.data.put("lastContact", com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge.writeBlockPos(pos));
		return;
	}

	@Override
	public void stopMoving(MovementContext context) {
		deactivateLastVisitedContact(context);
	}

	@Override
	public void cancelStall(MovementContext context) {
		MovementBehaviour.super.cancelStall(context);
		deactivateLastVisitedContact(context);
	}

	public void deactivateLastVisitedContact(MovementContext context) {
		if (!context.data.contains("lastContact"))
			return;

		BlockPos last = NBTHelper.readBlockPos(context.data, "lastContact");
		context.data.remove("lastContact");
		BlockState blockState = context.world.getBlockState(last);

		if (AllBlocks.REDSTONE_CONTACT.has(blockState))
			context.world.scheduleTick(last, AllBlocks.REDSTONE_CONTACT.get(), 1, TickPriority.NORMAL);
	}

}
