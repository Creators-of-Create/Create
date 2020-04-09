package com.simibubi.create.modules.contraptions.relays.belt.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.modules.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.modules.logistics.block.belts.tunnel.BeltTunnelTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class BeltInventory {

	final BeltTileEntity belt;
	private final List<TransportedItemStack> items;
	final List<TransportedItemStack> toInsert;
	boolean beltMovementPositive;
	final float SEGMENT_WINDOW = .75f;

	public BeltInventory(BeltTileEntity te) {
		this.belt = te;
		items = new LinkedList<>();
		toInsert = new LinkedList<>();
	}

	public void tick() {

		// Reverse item collection if belt just reversed
		if (beltMovementPositive != movingPositive()) {
			beltMovementPositive = movingPositive();
			Collections.reverse(getItems());
			belt.markDirty();
			belt.sendData();
		}

		// Add items from previous cycle
		if (!toInsert.isEmpty()) {
			toInsert.forEach(this::insert);
			toInsert.clear();
			belt.markDirty();
			belt.sendData();
		}

		// Assuming the first entry is furthest on the belt
		TransportedItemStack stackInFront = null;
		TransportedItemStack current = null;
		Iterator<TransportedItemStack> iterator = getItems().iterator();

		float beltSpeed = belt.getDirectionAwareBeltMovementSpeed();
		Direction movementFacing = belt.getMovementFacing();
		float spacing = 1;
		boolean onClient = belt.getWorld().isRemote;

		Items: while (iterator.hasNext()) {
			stackInFront = current;
			current = iterator.next();
			current.prevBeltPosition = current.beltPosition;
			current.prevSideOffset = current.sideOffset;

			if (current.stack.isEmpty()) {
				iterator.remove();
				current = null;
				continue;
			}

			float movement = beltSpeed;
			if (onClient)
				movement *= ServerSpeedProvider.get();

			// Don't move if locked
			if (onClient && current.locked)
				continue;

			// Don't move if other items are waiting in front
			float currentPos = current.beltPosition;
			if (stackInFront != null) {
				float diff = stackInFront.beltPosition - currentPos;
				if (Math.abs(diff) <= spacing)
					continue;
				movement =
					beltMovementPositive ? Math.min(movement, diff - spacing) : Math.max(movement, diff + spacing);
			}

			// Determine current segment
			int segmentBefore = (int) currentPos;
			float min = segmentBefore + .5f - (SEGMENT_WINDOW / 2);
			float max = segmentBefore + .5f + (SEGMENT_WINDOW / 2);
			if (currentPos < min || currentPos > max)
				segmentBefore = -1;

			// Don't move beyond the edge
			float diffToEnd = beltMovementPositive ? belt.beltLength - currentPos : -currentPos;
			float limitedMovement =
				beltMovementPositive ? Math.min(movement, diffToEnd) : Math.max(movement, diffToEnd);

			float nextOffset = current.beltPosition + limitedMovement;
			if (!onClient) {
				// Don't move if belt attachments want to continue processing
				if (segmentBefore != -1 && current.locked) {
					BeltTileEntity beltSegment = BeltHelper.getBeltAtSegment(belt, segmentBefore);
					if (beltSegment != null) {

						current.locked = false;
						List<BeltAttachmentState> attachments = beltSegment.attachmentTracker.attachments;
						for (BeltAttachmentState attachmentState : attachments) {
							if (attachmentState.attachment.processItem(beltSegment, current, attachmentState))
								current.locked = true;
						}
						if (!current.locked || current.stack.isEmpty()) {
							if (!attachments.isEmpty())
								attachments.add(attachments.remove(0));
							belt.sendData();
						}
						continue;
					}
				}

				// See if any new belt processing catches the item
				int upcomingSegment = (int) (current.beltPosition + (beltMovementPositive ? .5f : -.5f));
				for (int segment = upcomingSegment; beltMovementPositive ? segment + .5f <= nextOffset
						: segment + .5f >= nextOffset; segment += beltMovementPositive ? 1 : -1) {
					BeltTileEntity beltSegment = BeltHelper.getBeltAtSegment(belt, segmentBefore);
					if (beltSegment == null)
						break;
					for (BeltAttachmentState attachmentState : beltSegment.attachmentTracker.attachments) {
						ItemStack stackBefore = current.stack.copy();
						if (attachmentState.attachment.startProcessingItem(beltSegment, current, attachmentState)) {
							current.beltPosition = segment + .5f + (beltMovementPositive ? 1 / 64f : -1 / 64f);
							current.locked = true;
							belt.sendData();
							continue Items;
						}
						if (!stackBefore.equals(current.stack, true))
							belt.sendData();
					}
				}
			}

			// Belt tunnels
			{
				int seg1 = (int) current.beltPosition;
				int seg2 = (int) nextOffset;
				if (!beltMovementPositive && nextOffset == 0)
					seg2 = -1;
				if (seg1 != seg2) {
					if (stuckAtTunnel(seg2, current.stack, movementFacing)) {
						continue;
					}
					if (!onClient) {
						flapTunnel(seg1, movementFacing, false);
						flapTunnel(seg2, movementFacing.getOpposite(), true);
					}
				}
			}

			// Apply Movement
			current.beltPosition += limitedMovement;
			current.sideOffset += (current.getTargetSideOffset() - current.sideOffset) * Math.abs(limitedMovement) * 2f;
			currentPos = current.beltPosition;

			// Determine segment after movement
			int segmentAfter = (int) currentPos;
			min = segmentAfter + .5f - (SEGMENT_WINDOW / 2);
			max = segmentAfter + .5f + (SEGMENT_WINDOW / 2);
			if (currentPos < min || currentPos > max)
				segmentAfter = -1;

			// Item changed segments
			World world = belt.getWorld();
			if (segmentBefore != segmentAfter) {
				for (int segment : new int[] { segmentBefore, segmentAfter }) {
					if (segment == -1)
						continue;
					if (!world.isRemote)
						world.updateComparatorOutputLevel(BeltHelper.getPositionForOffset(belt, segment),
								belt.getBlockState().getBlock());
				}
			}

			// End reached
			if (limitedMovement != movement) {
				if (world.isRemote)
					continue;

				int lastOffset = beltMovementPositive ? belt.beltLength - 1 : 0;
				BlockPos nextPosition =
					BeltHelper.getPositionForOffset(belt, beltMovementPositive ? belt.beltLength : -1);
				BlockState state = world.getBlockState(nextPosition);

				// next block is a basin or a saw
				if (AllBlocks.BASIN.typeOf(state) || AllBlocks.SAW.typeOf(state)
						|| AllBlocks.CRUSHING_WHEEL_CONTROLLER.typeOf(state)) {
					TileEntity te = world.getTileEntity(nextPosition);
					if (te != null) {
						LazyOptional<IItemHandler> optional =
							te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
						if (optional.isPresent()) {
							IItemHandler itemHandler = optional.orElse(null);
							ItemStack remainder =
								ItemHandlerHelper.insertItemStacked(itemHandler, current.stack.copy(), false);
							if (remainder.equals(current.stack, false))
								continue;

							current.stack = remainder;
							if (remainder.isEmpty()) {
								iterator.remove();
								current = null;
								flapTunnel(lastOffset, movementFacing, false);
							}

							belt.sendData();
						}
					}
					continue;
				}

				// next block is not a belt
				if (!AllBlocks.BELT.typeOf(state)) {
					if (!Block.hasSolidSide(state, world, nextPosition, movementFacing.getOpposite())) {
						eject(current);
						iterator.remove();
						current = null;
						flapTunnel(lastOffset, movementFacing, false);
						belt.sendData();
					}
					continue;
				}

				// Next block is a belt
				TileEntity te = world.getTileEntity(nextPosition);
				if (te == null || !(te instanceof BeltTileEntity))
					continue;
				BeltTileEntity nextBelt = (BeltTileEntity) te;
				Direction nextMovementFacing = nextBelt.getMovementFacing();

				// next belt goes the opposite way
				if (nextMovementFacing == movementFacing.getOpposite())
					continue;

				// Inserting into other belt
				if (nextBelt.tryInsertingFromSide(movementFacing, current, false)) {
					iterator.remove();
					current = null;
					flapTunnel(lastOffset, movementFacing, false);
					belt.sendData();
				}

			}

		}

	}

	private boolean stuckAtTunnel(int offset, ItemStack stack, Direction movementDirection) {
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset).up();
		if (!AllBlocks.BELT_TUNNEL.typeOf(belt.getWorld().getBlockState(pos)))
			return false;
		TileEntity te = belt.getWorld().getTileEntity(pos);
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

	private void flapTunnel(int offset, Direction side, boolean inward) {
		if (belt.getBlockState().get(BeltBlock.SLOPE) != Slope.HORIZONTAL)
			return;
		BlockPos pos = BeltHelper.getPositionForOffset(belt, offset).up();
		if (!AllBlocks.BELT_TUNNEL.typeOf(belt.getWorld().getBlockState(pos)))
			return;
		TileEntity te = belt.getWorld().getTileEntity(pos);
		if (te == null || !(te instanceof BeltTunnelTileEntity))
			return;
		((BeltTunnelTileEntity) te).flap(side, inward ^ side.getAxis() == Axis.Z);
	}

	public boolean canInsertAt(int segment) {
		return canInsertFrom(segment, Direction.UP);
	}

	public boolean canInsertFrom(int segment, Direction side) {
		float segmentPos = segment;
		if (belt.getMovementFacing() == side.getOpposite())
			return false;
		if (belt.getMovementFacing() != side)
			segmentPos += .5f;
		else if (!beltMovementPositive)
			segmentPos += 1f;

		for (TransportedItemStack stack : getItems())
			if (isBlocking(segment, side, segmentPos, stack))
				return false;
		for (TransportedItemStack stack : toInsert)
			if (isBlocking(segment, side, segmentPos, stack))
				return false;

		return true;
	}

	private boolean isBlocking(int segment, Direction side, float segmentPos, TransportedItemStack stack) {
		float currentPos = stack.beltPosition;
		if (stack.insertedAt == segment && stack.insertedFrom == side
				&& (beltMovementPositive ? currentPos <= segmentPos + 1 : currentPos >= segmentPos - 1))
			return true;
		return false;
	}

	public void addItem(TransportedItemStack newStack) {
		toInsert.add(newStack);
	}

	private void insert(TransportedItemStack newStack) {
		if (getItems().isEmpty())
			getItems().add(newStack);
		else {
			int index = 0;
			for (TransportedItemStack stack : getItems()) {
				if (stack.compareTo(newStack) > 0 == beltMovementPositive)
					break;
				index++;
			}
			getItems().add(index, newStack);
		}
	}

	public TransportedItemStack getStackAtOffset(int offset) {
		float min = offset + .5f - (SEGMENT_WINDOW / 2);
		float max = offset + .5f + (SEGMENT_WINDOW / 2);
		for (TransportedItemStack stack : getItems()) {
			if (stack.beltPosition > max)
				continue;
			if (stack.beltPosition > min)
				return stack;
		}
		return null;
	}

	public void read(CompoundNBT nbt) {
		getItems().clear();
		nbt.getList("Items", NBT.TAG_COMPOUND)
				.forEach(inbt -> getItems().add(TransportedItemStack.read((CompoundNBT) inbt)));
		beltMovementPositive = nbt.getBoolean("PositiveOrder");
	}

	public CompoundNBT write() {
		CompoundNBT nbt = new CompoundNBT();
		ListNBT itemsNBT = new ListNBT();
		getItems().forEach(stack -> itemsNBT.add(stack.serializeNBT()));
		nbt.put("Items", itemsNBT);
		nbt.putBoolean("PositiveOrder", beltMovementPositive);
		return nbt;
	}

	public void eject(TransportedItemStack stack) {
		ItemStack ejected = stack.stack;
		Vec3d outPos = BeltHelper.getVectorForOffset(belt, stack.beltPosition);
		float movementSpeed = Math.max(Math.abs(belt.getBeltMovementSpeed()), 1 / 8f);
		Vec3d outMotion = new Vec3d(belt.getBeltChainDirection()).scale(movementSpeed).add(0, 1 / 8f, 0);
		outPos.add(outMotion.normalize());
		ItemEntity entity = new ItemEntity(belt.getWorld(), outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
		entity.setMotion(outMotion);
		entity.setDefaultPickupDelay();
		entity.velocityChanged = true;
		belt.getWorld().addEntity(entity);
	}

	public void ejectAll() {
		getItems().forEach(this::eject);
		getItems().clear();
	}

	private boolean movingPositive() {
		return belt.getDirectionAwareBeltMovementSpeed() > 0;
	}

	public IItemHandler createHandlerForSegment(int segment) {
		return new ItemHandlerBeltSegment(this, segment);
	}

	public void forEachWithin(float position, float distance,
			Function<TransportedItemStack, List<TransportedItemStack>> callback) {
		List<TransportedItemStack> toBeAdded = new ArrayList<>();
		boolean dirty = false;
		for (Iterator<TransportedItemStack> iterator = getItems().iterator(); iterator.hasNext();) {
			TransportedItemStack transportedItemStack = iterator.next();
			if (Math.abs(position - transportedItemStack.beltPosition) < distance) {
				List<TransportedItemStack> apply = callback.apply(transportedItemStack);
				if (apply == null)
					continue;
				dirty = true;
				toBeAdded.addAll(apply);
				iterator.remove();
			}
		}
		toBeAdded.forEach(this::insert);
		if (dirty) {
			belt.markDirty();
			belt.sendData();
		}
	}

	public List<TransportedItemStack> getItems() {
		return items;
	}

}
