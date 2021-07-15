package com.simibubi.create.content.contraptions.components.actors.dispenser;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class MovedDefaultDispenseItemBehaviour implements IMovedDispenseItemBehaviour {
	private static final MovedDefaultDispenseItemBehaviour defaultInstance = new MovedDefaultDispenseItemBehaviour();

	public static void doDispense(World p_82486_0_, ItemStack p_82486_1_, int p_82486_2_, Vector3d facing, BlockPos p_82486_4_, MovementContext context) {
		double d0 = p_82486_4_.getX() + facing.x + .5;
		double d1 = p_82486_4_.getY() + facing.y + .5;
		double d2 = p_82486_4_.getZ() + facing.z + .5;
		if (Direction.getNearest(facing.x, facing.y, facing.z).getAxis() == Direction.Axis.Y) {
			d1 = d1 - 0.125D;
		} else {
			d1 = d1 - 0.15625D;
		}

		ItemEntity itementity = new ItemEntity(p_82486_0_, d0, d1, d2, p_82486_1_);
		double d3 = p_82486_0_.random.nextDouble() * 0.1D + 0.2D;
		itementity.setDeltaMovement(p_82486_0_.random.nextGaussian() * (double) 0.0075F * (double) p_82486_2_ + facing.x() * d3 + context.motion.x, p_82486_0_.random.nextGaussian() * (double) 0.0075F * (double) p_82486_2_ + facing.y() * d3 + context.motion.y, p_82486_0_.random.nextGaussian() * (double) 0.0075F * (double) p_82486_2_ + facing.z() * d3 + context.motion.z);
		p_82486_0_.addFreshEntity(itementity);
	}

	@Override
	public ItemStack dispense(ItemStack itemStack, MovementContext context, BlockPos pos) {
		Vector3d facingVec = Vector3d.atLowerCornerOf(context.state.getValue(DispenserBlock.FACING).getNormal());
		facingVec = context.rotation.apply(facingVec);
		facingVec.normalize();

		Direction closestToFacing = getClosestFacingDirection(facingVec);
		IInventory iinventory = HopperTileEntity.getContainerAt(context.world, pos.relative(closestToFacing));
		if (iinventory == null) {
			this.playDispenseSound(context.world, pos);
			this.spawnDispenseParticles(context.world, pos, closestToFacing);
			return this.dispenseStack(itemStack, context, pos, facingVec);
		} else {
			if (HopperTileEntity.addItem(null, iinventory, itemStack.copy().split(1), closestToFacing.getOpposite()).isEmpty())
				itemStack.shrink(1);
			return itemStack;
		}
	}

	/**
	 * Dispense the specified stack, play the dispense sound and spawn particles.
	 */
	protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vector3d facing) {
		ItemStack itemstack = itemStack.split(1);
		doDispense(context.world, itemstack, 6, facing, pos, context);
		return itemStack;
	}

	/**
	 * Play the dispense sound from the specified block.
	 */
	protected void playDispenseSound(IWorld world, BlockPos pos) {
		world.levelEvent(1000, pos, 0);
	}

	/**
	 * Order clients to display dispense particles from the specified block and facing.
	 */
	protected void spawnDispenseParticles(IWorld world, BlockPos pos, Vector3d facing) {
		spawnDispenseParticles(world, pos, getClosestFacingDirection(facing));
	}

	protected void spawnDispenseParticles(IWorld world, BlockPos pos, Direction direction) {
		world.levelEvent(2000, pos, direction.get3DDataValue());
	}

	protected Direction getClosestFacingDirection(Vector3d exactFacing) {
		return Direction.getNearest(exactFacing.x, exactFacing.y, exactFacing.z);
	}

	protected ItemStack placeItemInInventory(ItemStack consumedFrom, ItemStack output, MovementContext context, BlockPos pos, Vector3d facing) {
		consumedFrom.shrink(1);
		ItemStack remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, output.copy(), false);
		if (!remainder.isEmpty())
			defaultInstance.dispenseStack(output, context, pos, facing);
		return consumedFrom;
	}
}
