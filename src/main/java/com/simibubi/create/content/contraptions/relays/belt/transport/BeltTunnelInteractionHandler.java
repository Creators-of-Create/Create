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
import com.simibubi.create.content.logistics.block.display.DisplayLinkBlock;
import com.simibubi.create.content.logistics.block.display.source.AccumulatedItemCountDisplaySource;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

		Level world = beltInventory.belt.getLevel();
		boolean onServer = !world.isClientSide || beltInventory.belt.isVirtual();
		boolean removed = false;
		BeltTunnelTileEntity nextTunnel = getTunnelOnSegment(beltInventory, upcomingSegment);
		int transferred = current.stack.getCount();

		if (nextTunnel instanceof BrassTunnelTileEntity) {
			BrassTunnelTileEntity brassTunnel = (BrassTunnelTileEntity) nextTunnel;
			if (brassTunnel.hasDistributionBehaviour()) {
				if (!brassTunnel.canTakeItems())
					return true;
				if (onServer) {
					brassTunnel.setStackToDistribute(current.stack, movementFacing.getOpposite());
					current.stack = ItemStack.EMPTY;
					beltInventory.belt.sendData();
					beltInventory.belt.setChanged();
				}
				removed = true;
			}
		} else if (nextTunnel != null) {
			BlockState blockState = nextTunnel.getBlockState();
			if (current.stack.getCount() > 1 && AllBlocks.ANDESITE_TUNNEL.has(blockState)
				&& BeltTunnelBlock.isJunction(blockState)
				&& movementFacing.getAxis() == blockState.getValue(BeltTunnelBlock.HORIZONTAL_AXIS)) {

				for (Direction d : Iterate.horizontalDirections) {
					if (d.getAxis() == blockState.getValue(BeltTunnelBlock.HORIZONTAL_AXIS))
						continue;
					if (!nextTunnel.flaps.containsKey(d))
						continue;
					BlockPos outpos = nextTunnel.getBlockPos()
						.below()
						.relative(d);
					if (!world.isLoaded(outpos))
						return true;
					DirectBeltInputBehaviour behaviour =
						TileEntityBehaviour.get(world, outpos, DirectBeltInputBehaviour.TYPE);
					if (behaviour == null)
						continue;
					if (!behaviour.canInsertFromSide(d))
						continue;

					ItemStack toinsert = ItemHandlerHelper.copyStackWithSize(current.stack, 1);
					if (!behaviour.handleInsertion(toinsert, d, false)
						.isEmpty())
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

			if (nextTunnel != null)
				DisplayLinkBlock.sendToGatherers(world, nextTunnel.getBlockPos(),
					(dgte, b) -> b.itemReceived(dgte, transferred), AccumulatedItemCountDisplaySource.class);
		}

		if (removed)
			return true;

		return false;
	}

	public static boolean stuckAtTunnel(BeltInventory beltInventory, int offset, ItemStack stack,
		Direction movementDirection) {
		BeltTileEntity belt = beltInventory.belt;
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset)
			.above();
		if (!(belt.getLevel()
			.getBlockState(pos)
			.getBlock() instanceof BrassTunnelBlock))
			return false;
		BlockEntity te = belt.getLevel()
			.getBlockEntity(pos);
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
			.getValue(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL)
			return null;
		return getTunnelOnPosition(belt.getLevel(), BeltHelper.getPositionForOffset(belt, offset));
	}

	public static BeltTunnelTileEntity getTunnelOnPosition(Level world, BlockPos pos) {
		pos = pos.above();
		if (!(world.getBlockState(pos)
			.getBlock() instanceof BeltTunnelBlock))
			return null;
		BlockEntity te = world.getBlockEntity(pos);
		if (te == null || !(te instanceof BeltTunnelTileEntity))
			return null;
		return ((BeltTunnelTileEntity) te);
	}

}
