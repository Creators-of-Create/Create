package com.simibubi.create.content.contraptions.relays.belt.transport;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelTileEntity;

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
				current.beltPosition = currentSegment + (beltInventory.beltMovementPositive ? .99f : -.01f);				
				return true;
			}
			if (!beltInventory.belt.getWorld().isRemote) {
				flapTunnel(beltInventory, currentSegment, movementFacing, false);
				flapTunnel(beltInventory, upcomingSegment, movementFacing.getOpposite(), true);
			}
		}

		return false;
	}

	public static boolean stuckAtTunnel(BeltInventory beltInventory, int offset, ItemStack stack,
		Direction movementDirection) {
		BeltTileEntity belt = beltInventory.belt;
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset)
			.up();
		if (!AllBlocks.BELT_TUNNEL.has(belt.getWorld()
			.getBlockState(pos)))
			return false;
		TileEntity te = belt.getWorld()
			.getTileEntity(pos);
		if (te == null || !(te instanceof BeltTunnelTileEntity))
			return false;

		Direction flapFacing = movementDirection.getOpposite();

		BeltTunnelTileEntity tunnel = (BeltTunnelTileEntity) te;
		if (!tunnel.flaps.containsKey(flapFacing))
			return false;
		if (!tunnel.syncedFlaps.containsKey(flapFacing))
			return false;
		ItemStack heldItem = tunnel.syncedFlaps.get(flapFacing);
		if (heldItem == null) {
			tunnel.syncedFlaps.put(flapFacing, ItemStack.EMPTY);
			belt.sendData();
			return false;
		}
		if (heldItem == ItemStack.EMPTY) {
			tunnel.syncedFlaps.put(flapFacing, stack);
			return true;
		}

		List<BeltTunnelTileEntity> group = BeltTunnelBlock.getSynchronizedGroup(belt.getWorld(), pos, flapFacing);
		for (BeltTunnelTileEntity otherTunnel : group)
			if (otherTunnel.syncedFlaps.get(flapFacing) == ItemStack.EMPTY)
				return true;
		for (BeltTunnelTileEntity otherTunnel : group)
			otherTunnel.syncedFlaps.put(flapFacing, null);

		return true;
	}

	public static void flapTunnel(BeltInventory beltInventory, int offset, Direction side, boolean inward) {
		BeltTileEntity belt = beltInventory.belt;
		if (belt.getBlockState()
			.get(BeltBlock.SLOPE) != Slope.HORIZONTAL)
			return;
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset)
			.up();
		if (!AllBlocks.BELT_TUNNEL.has(belt.getWorld()
			.getBlockState(pos)))
			return;
		TileEntity te = belt.getWorld()
			.getTileEntity(pos);
		if (te == null || !(te instanceof BeltTunnelTileEntity))
			return;
		((BeltTunnelTileEntity) te).flap(side, inward ^ side.getAxis() == Axis.Z);
	}

}
