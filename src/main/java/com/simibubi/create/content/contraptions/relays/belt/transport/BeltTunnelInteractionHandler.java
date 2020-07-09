package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class BeltTunnelInteractionHandler {

	public static boolean flapTunnelsAndCheckIfStuck(BeltInventory beltInventory, TransportedItemStack current,
		float nextOffset) {

		int currentSegment = (int) current.beltPosition;
		int upcomingSegment = (int) nextOffset;

		Direction movementFacing = beltInventory.belt.getMovementFacing();
		if (!beltInventory.beltMovementPositive && nextOffset == 0)
			upcomingSegment = -1;
		if (currentSegment != upcomingSegment) {
			if (stuckAtTunnel(beltInventory, upcomingSegment, current.stack, movementFacing)) {
				current.beltPosition = currentSegment + (beltInventory.beltMovementPositive ? .99f : .01f);
				return true;
			}
			boolean onServer = !beltInventory.belt.getWorld().isRemote;
			boolean removed = false;
			BeltTunnelTileEntity nextTunnel = getTunnelOnSegement(beltInventory, upcomingSegment);
			if (nextTunnel instanceof BrassTunnelTileEntity) {
				BrassTunnelTileEntity brassTunnel = (BrassTunnelTileEntity) nextTunnel;
				if (brassTunnel.hasDistributionBehaviour()) {
					if (!brassTunnel.getStackToDistribute()
						.isEmpty())
						return true;
					if (onServer) {
						brassTunnel.setStackToDistribute(current.stack);
						current.stack = ItemStack.EMPTY;
						beltInventory.belt.sendData();
						beltInventory.belt.markDirty();
					}
					removed = true;
				}
			}

			if (onServer) {
				flapTunnel(beltInventory, currentSegment, movementFacing, false);
				flapTunnel(beltInventory, upcomingSegment, movementFacing.getOpposite(), true);
			}

			if (removed)
				return true;
		}

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
		BeltTunnelTileEntity te = getTunnelOnSegement(beltInventory, offset);
		if (te == null)
			return;
		te.flap(side, inward ^ side.getAxis() == Axis.Z);
	}

	protected static BeltTunnelTileEntity getTunnelOnSegement(BeltInventory beltInventory, int offset) {
		BeltTileEntity belt = beltInventory.belt;
		if (belt.getBlockState()
			.get(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL)
			return null;
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset)
			.up();
		if (!(belt.getWorld()
			.getBlockState(pos)
			.getBlock() instanceof BeltTunnelBlock))
			return null;
		TileEntity te = belt.getWorld()
			.getTileEntity(pos);
		if (te == null || !(te instanceof BeltTunnelTileEntity))
			return null;
		return ((BeltTunnelTileEntity) te);
	}

}
