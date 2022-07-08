package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

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
		return Vec3.atLowerCornerOf(context.state.getValue(RedstoneContactBlock.FACING).getNormal()).scale(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		BlockState block = context.state;
		Level world = context.world;

		if (world.isClientSide)
			return;
		if (context.firstMovement)
			return;

		deactivateLastVisitedContact(context);
		BlockState visitedState = world.getBlockState(pos);
		if (!AllBlocks.REDSTONE_CONTACT.has(visitedState))
			return;

		Vec3 contact = Vec3.atLowerCornerOf(block.getValue(RedstoneContactBlock.FACING).getNormal());
		contact = context.rotation.apply(contact);
		Direction direction = Direction.getNearest(contact.x, contact.y, contact.z);

		if (!RedstoneContactBlock.hasValidContact(world, pos.relative(direction.getOpposite()), direction))
			return;
		world.setBlockAndUpdate(pos, visitedState.setValue(RedstoneContactBlock.POWERED, true));
		context.data.put("lastContact", NbtUtils.writeBlockPos(pos));
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
		if (context.data.contains("lastContact")) {
			BlockPos last = NbtUtils.readBlockPos(context.data.getCompound("lastContact"));
			context.world.scheduleTick(last, AllBlocks.REDSTONE_CONTACT.get(), 1, TickPriority.NORMAL);
			context.data.remove("lastContact");
		}
	}

}
