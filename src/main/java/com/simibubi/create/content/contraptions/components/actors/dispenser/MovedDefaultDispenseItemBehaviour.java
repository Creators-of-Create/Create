package com.simibubi.create.content.contraptions.components.actors.dispenser;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class MovedDefaultDispenseItemBehaviour implements IMovedDispenseItemBehaviour {

	public static void doDispense(World p_82486_0_, ItemStack p_82486_1_, int p_82486_2_, Vec3d facing, BlockPos p_82486_4_, MovementContext context) {
		double d0 = p_82486_4_.getX() + facing.x + .5;
		double d1 = p_82486_4_.getY() + facing.y + .5;
		double d2 = p_82486_4_.getZ() + facing.z + .5;
		if (Direction.getFacingFromVector(facing.x, facing.y, facing.z).getAxis() == Direction.Axis.Y) {
			d1 = d1 - 0.125D;
		} else {
			d1 = d1 - 0.15625D;
		}

		ItemEntity itementity = new ItemEntity(p_82486_0_, d0, d1, d2, p_82486_1_);
		double d3 = p_82486_0_.rand.nextDouble() * 0.1D + 0.2D;
		itementity.setMotion(p_82486_0_.rand.nextGaussian() * (double) 0.0075F * (double) p_82486_2_ + facing.getX() * d3 + context.motion.x, p_82486_0_.rand.nextGaussian() * (double) 0.0075F * (double) p_82486_2_ + facing.getY() * d3 + context.motion.y, p_82486_0_.rand.nextGaussian() * (double) 0.0075F * (double) p_82486_2_ + facing.getZ() * d3 + context.motion.z);
		p_82486_0_.addEntity(itementity);
	}

	@Override
	public ItemStack dispense(ItemStack itemStack, MovementContext context, BlockPos pos) {
		Vec3d facingVec = new Vec3d(context.state.get(DispenserBlock.FACING).getDirectionVec());
		facingVec = VecHelper.rotate(facingVec, context.rotation.x, context.rotation.y, context.rotation.z);
		facingVec.normalize();

		Direction closestToFacing = getClosestFacingDirection(facingVec);
		IInventory iinventory = HopperTileEntity.getInventoryAtPosition(context.world, pos.offset(closestToFacing));
		if (iinventory == null) {
			this.playDispenseSound(context.world, pos);
			this.spawnDispenseParticles(context.world, pos, closestToFacing);
			return this.dispenseStack(itemStack, context, pos, facingVec);
		} else {
			if (HopperTileEntity.putStackInInventoryAllSlots(null, iinventory, itemStack.copy().split(1), closestToFacing.getOpposite()).isEmpty())
				itemStack.shrink(1);
			return itemStack;
		}
	}

	/**
	 * Dispense the specified stack, play the dispense sound and spawn particles.
	 */
	protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3d facing) {
		ItemStack itemstack = itemStack.split(1);
		doDispense(context.world, itemstack, 6, facing, pos, context);
		return itemStack;
	}

	/**
	 * Play the dispense sound from the specified block.
	 */
	protected void playDispenseSound(IWorld world, BlockPos pos) {
		world.playEvent(1000, pos, 0);
	}

	/**
	 * Order clients to display dispense particles from the specified block and facing.
	 */
	protected void spawnDispenseParticles(IWorld world, BlockPos pos, Vec3d facing) {
		spawnDispenseParticles(world, pos, getClosestFacingDirection(facing));
	}

	protected void spawnDispenseParticles(IWorld world, BlockPos pos, Direction direction) {
		world.playEvent(2000, pos, direction.getIndex());
	}

	protected Direction getClosestFacingDirection(Vec3d exactFacing) {
		return Direction.getFacingFromVector(exactFacing.x, exactFacing.y, exactFacing.z);
	}
}
