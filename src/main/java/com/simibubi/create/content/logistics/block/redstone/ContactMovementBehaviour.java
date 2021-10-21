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
		return Vector3d.atLowerCornerOf(context.state.getValue(RedstoneContactBlock.FACING).getNormal()).scale(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		BlockState block = context.state;
		World world = context.world;

		if (world.isClientSide)
			return;
		if (context.firstMovement)
			return;

		deactivateLastVisitedContact(context);
		BlockState visitedState = world.getBlockState(pos);
		if (!AllBlocks.REDSTONE_CONTACT.has(visitedState))
			return;

		Vector3d contact = Vector3d.atLowerCornerOf(block.getValue(RedstoneContactBlock.FACING).getNormal());
		contact = context.rotation.apply(contact);
		Direction direction = Direction.getNearest(contact.x, contact.y, contact.z);

		if (!RedstoneContactBlock.hasValidContact(world, pos.relative(direction.getOpposite()), direction))
			return;
		world.setBlockAndUpdate(pos, visitedState.setValue(RedstoneContactBlock.POWERED, true));
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
			context.world.getBlockTicks().scheduleTick(last, AllBlocks.REDSTONE_CONTACT.get(), 1, TickPriority.NORMAL);
			context.data.remove("lastContact");
		}
	}

}
