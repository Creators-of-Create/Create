package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;

public class ContactMovementBehaviour extends MovementBehaviour {

	@Override
	public Vector3d getActiveAreaOffset(MovementContext context) {
		return Vector3d.of(context.state.get(RedstoneContactBlock.FACING).getDirectionVec()).scale(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		BlockState block = context.state;
		World world = context.world;

		if (world.isRemote)
			return;
		if (context.firstMovement)
			return;

		deactivateLastVisitedContact(context);
		BlockState visitedState = world.getBlockState(pos);
		if (!AllBlocks.REDSTONE_CONTACT.has(visitedState))
			return;

		Vector3d contact = Vector3d.of(block.get(RedstoneContactBlock.FACING).getDirectionVec());
		contact = context.rotation.apply(contact);
		Direction direction = Direction.getFacingFromVector(contact.x, contact.y, contact.z);

		if (!RedstoneContactBlock.hasValidContact(world, pos.offset(direction.getOpposite()), direction))
			return;
		world.setBlockState(pos, visitedState.with(RedstoneContactBlock.POWERED, true));
		context.data.put("lastContact", NBTUtil.writeBlockPos(pos));
		return;
	}

	@Override
	public void stopMoving(MovementContext context) {
		deactivateLastVisitedContact(context);
	}

	public void deactivateLastVisitedContact(MovementContext context) {
		if (context.data.contains("lastContact")) {
			BlockPos last = NBTUtil.readBlockPos(context.data.getCompound("lastContact"));
			context.world.getPendingBlockTicks().scheduleTick(last, AllBlocks.REDSTONE_CONTACT.get(), 1, TickPriority.NORMAL);
			context.data.remove("lastContact");
		}
	}

}
