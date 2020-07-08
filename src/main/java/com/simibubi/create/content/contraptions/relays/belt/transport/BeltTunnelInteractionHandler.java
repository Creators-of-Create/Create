package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
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
		BeltTileEntity belt = beltInventory.belt;
		if (belt.getBlockState()
			.get(BeltBlock.SLOPE) != Slope.HORIZONTAL)
			return;
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset)
			.up();
		if (!(belt.getWorld()
			.getBlockState(pos)
			.getBlock() instanceof BeltTunnelBlock))
			return;
		TileEntity te = belt.getWorld()
			.getTileEntity(pos);
		if (te == null || !(te instanceof BeltTunnelTileEntity))
			return;
		((BeltTunnelTileEntity) te).flap(side, inward ^ side.getAxis() == Axis.Z);
	}

}
