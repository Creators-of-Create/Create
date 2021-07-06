package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class BeltTunnelInteractionHandler {

	public static boolean flapTunnelsAndCheckIfStuck(BeltInventory beltInventory, TransportedItemStack current,
		float nextOffset) {

		int currentSegment = (int) current.beltPosition;
		int upcomingSegment = (int) nextOffset;

		Direction movementFacing = beltInventory.belt.getMovementFacing();
		if (!beltInventory.beltMovementPositive && nextOffset == 0)
			upcomingSegment = -1;
		if (currentSegment == upcomingSegment)
			return false;

		if (stuckAtTunnel(beltInventory, upcomingSegment, current.stack, movementFacing)) {
			current.beltPosition = currentSegment + (beltInventory.beltMovementPositive ? .99f : .01f);
			return true;
		}

		World world = beltInventory.belt.getWorld();
		boolean onServer = !world.isRemote || beltInventory.belt.isVirtual();
		boolean removed = false;
		BeltTunnelTileEntity nextTunnel = getTunnelOnSegment(beltInventory, upcomingSegment);

		if (nextTunnel instanceof BrassTunnelTileEntity) {
			BrassTunnelTileEntity brassTunnel = (BrassTunnelTileEntity) nextTunnel;
			if (brassTunnel.hasDistributionBehaviour()) {
				if (!brassTunnel.canTakeItems())
					return true;
				if (onServer) {
					brassTunnel.setStackToDistribute(current.stack);
					current.stack = ItemStack.EMPTY;
					beltInventory.belt.sendData();
					beltInventory.belt.markDirty();
				}
				removed = true;
			}
		} else if (nextTunnel != null) {
			BlockState blockState = nextTunnel.getBlockState();
			if (current.stack.getCount() > 1 && AllBlocks.ANDESITE_TUNNEL.has(blockState)
				&& BeltTunnelBlock.isJunction(blockState)
				&& movementFacing.getAxis() == blockState.get(BeltTunnelBlock.HORIZONTAL_AXIS)) {

				for (Direction d : Iterate.horizontalDirections) {
					if (d.getAxis() == blockState.get(BeltTunnelBlock.HORIZONTAL_AXIS))
						continue;
					if (!nextTunnel.flaps.containsKey(d))
						continue;
					BlockPos outpos = nextTunnel.getPos()
						.down()
						.offset(d);
					if (!world.isBlockPresent(outpos))
						return true;
					DirectBeltInputBehaviour behaviour =
						TileEntityBehaviour.get(world, outpos, DirectBeltInputBehaviour.TYPE);
					if (behaviour == null)
						continue;
					if (!behaviour.canInsertFromSide(d))
						continue;

					ItemStack toinsert = ItemHandlerHelper.copyStackWithSize(current.stack, 1);
					if (!behaviour.handleInsertion(toinsert, d, false).isEmpty())
						return true;
					if (onServer)
						flapTunnel(beltInventory, upcomingSegment, d, false);

					current.stack.shrink(1);
					beltInventory.belt.sendData();
					if (current.stack.getCount() <= 1)
						break;
				}
			}
		}

		if (onServer) {
			flapTunnel(beltInventory, currentSegment, movementFacing, false);
			flapTunnel(beltInventory, upcomingSegment, movementFacing.getOpposite(), true);
		}

		if (removed)
			return true;

		return false;
	}

	public static boolean stuckAtTunnel(BeltInventory beltInventory, int offset, ItemStack stack,
		Direction movementDirection) {
		BeltTileEntity belt = beltInventory.belt;
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset)
			.up();
		if (!(belt.getWorld()
			.getBlockState(pos)
			.getBlock() instanceof BrassTunnelBlock))
			return false;
		TileEntity te = belt.getWorld()
			.getTileEntity(pos);
		if (te == null || !(te instanceof BrassTunnelTileEntity))
			return false;
		BrassTunnelTileEntity tunnel = (BrassTunnelTileEntity) te;
		return !tunnel.canInsert(movementDirection.getOpposite(), stack);
	}

	public static void flapTunnel(BeltInventory beltInventory, int offset, Direction side, boolean inward) {
		BeltTunnelTileEntity te = getTunnelOnSegment(beltInventory, offset);
		if (te == null)
			return;
		te.flap(side, inward);
	}

	protected static BeltTunnelTileEntity getTunnelOnSegment(BeltInventory beltInventory, int offset) {
		BeltTileEntity belt = beltInventory.belt;
		if (belt.getBlockState()
			.get(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL)
			return null;
		return getTunnelOnPosition(belt.getWorld(), BeltHelper.getPositionForOffset(belt, offset));
	}

	public static BeltTunnelTileEntity getTunnelOnPosition(World world, BlockPos pos) {
		pos = pos.up();
		if (!(world.getBlockState(pos).getBlock() instanceof BeltTunnelBlock))
			return null;
		TileEntity te = world.getTileEntity(pos);
		if (te == null || !(te instanceof BeltTunnelTileEntity))
			return null;
		return ((BeltTunnelTileEntity) te);
	}

}
