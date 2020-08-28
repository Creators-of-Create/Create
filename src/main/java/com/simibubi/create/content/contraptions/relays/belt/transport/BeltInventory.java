package com.simibubi.create.content.contraptions.relays.belt.transport;

import static com.simibubi.create.content.contraptions.relays.belt.transport.BeltTunnelInteractionHandler.flapTunnel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.minecraft.block.Block;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

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
		if (beltMovementPositive != belt.getDirectionAwareBeltMovementSpeed() > 0) {
			beltMovementPositive = !beltMovementPositive;
			Collections.reverse(items);
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
		TransportedItemStack currentItem = null;
		Iterator<TransportedItemStack> iterator = items.iterator();

		// Useful stuff
		float beltSpeed = belt.getDirectionAwareBeltMovementSpeed();
		Direction movementFacing = belt.getMovementFacing();
		boolean horizontal = belt.getBlockState()
			.get(BeltBlock.SLOPE) == BeltSlope.HORIZONTAL;
		float spacing = 1;
		World world = belt.getWorld();
		boolean onClient = world.isRemote;

		// resolve ending only when items will reach it this tick
		Ending ending = Ending.UNRESOLVED;

		// Loop over items
		while (iterator.hasNext()) {
			stackInFront = currentItem;
			currentItem = iterator.next();
			currentItem.prevBeltPosition = currentItem.beltPosition;
			currentItem.prevSideOffset = currentItem.sideOffset;

			if (currentItem.stack.isEmpty()) {
				iterator.remove();
				currentItem = null;
				continue;
			}

			float movement = beltSpeed;
			if (onClient)
				movement *= ServerSpeedProvider.get();

			// Don't move if held by processing (client)
			if (onClient && currentItem.locked)
				continue;

			// Don't move if other items are waiting in front
			float currentPos = currentItem.beltPosition;
			if (stackInFront != null) {
				float diff = stackInFront.beltPosition - currentPos;
				if (Math.abs(diff) <= spacing)
					continue;
				movement =
					beltMovementPositive ? Math.min(movement, diff - spacing) : Math.max(movement, diff + spacing);
			}

			// Don't move beyond the edge
			float diffToEnd = beltMovementPositive ? belt.beltLength - currentPos : -currentPos;
			if (Math.abs(diffToEnd) < Math.abs(movement) + 1) {
				if (ending == Ending.UNRESOLVED)
					ending = resolveEnding();
				diffToEnd += beltMovementPositive ? -ending.margin : ending.margin;
			}
			float limitedMovement =
				beltMovementPositive ? Math.min(movement, diffToEnd) : Math.max(movement, diffToEnd);
			float nextOffset = currentItem.beltPosition + limitedMovement;

			// Belt item processing
			if (!onClient && horizontal) {
				ItemStack item = currentItem.stack;
				if (handleBeltProcessingAndCheckIfRemoved(currentItem, nextOffset)) {
					iterator.remove();
					belt.sendData();
					continue;
				}
				if (item != currentItem.stack)
					belt.sendData();
				if (currentItem.locked)
					continue;
			}

			// Belt Tunnels
			if (BeltTunnelInteractionHandler.flapTunnelsAndCheckIfStuck(this, currentItem, nextOffset))
				continue;

			// Belt Funnels
			if (BeltFunnelInteractionHandler.checkForFunnels(this, currentItem, nextOffset))
				continue;

			// Apply Movement
			currentItem.beltPosition += limitedMovement;
			currentItem.sideOffset +=
				(currentItem.getTargetSideOffset() - currentItem.sideOffset) * Math.abs(limitedMovement) * 2f;
			currentPos = currentItem.beltPosition;

			// Movement successful
			if (limitedMovement == movement || onClient)
				continue;

			// End reached
			int lastOffset = beltMovementPositive ? belt.beltLength - 1 : 0;
			BlockPos nextPosition = BeltHelper.getPositionForOffset(belt, beltMovementPositive ? belt.beltLength : -1);

			if (ending == Ending.FUNNEL)
				continue;

			if (ending == Ending.INSERT) {
				DirectBeltInputBehaviour inputBehaviour =
					TileEntityBehaviour.get(world, nextPosition, DirectBeltInputBehaviour.TYPE);
				if (inputBehaviour == null)
					continue;
				if (!inputBehaviour.canInsertFromSide(movementFacing))
					continue;

				ItemStack remainder = inputBehaviour.handleInsertion(currentItem, movementFacing, false);
				if (remainder.equals(currentItem.stack, false))
					continue;

				currentItem.stack = remainder;
				if (remainder.isEmpty())
					iterator.remove();

				flapTunnel(this, lastOffset, movementFacing, false);
				belt.sendData();
				continue;
			}

			if (ending == Ending.BLOCKED)
				continue;

			if (ending == Ending.EJECT) {
				eject(currentItem);
				iterator.remove();
				flapTunnel(this, lastOffset, movementFacing, false);
				belt.sendData();
				continue;
			}
		}
	}

	protected boolean handleBeltProcessingAndCheckIfRemoved(TransportedItemStack currentItem, float nextOffset) {
		int currentSegment = (int) currentItem.beltPosition;

		// Continue processing if held
		if (currentItem.locked) {
			BeltProcessingBehaviour processingBehaviour = getBeltProcessingAtSegment(currentSegment);
			TransportedItemStackHandlerBehaviour stackHandlerBehaviour =
				getTransportedItemStackHandlerAtSegment(currentSegment);

			if (stackHandlerBehaviour == null)
				return false;
			if (processingBehaviour == null) {
				currentItem.locked = false;
				belt.sendData();
				return false;
			}

			ProcessingResult result = processingBehaviour.handleHeldItem(currentItem, stackHandlerBehaviour);
			if (result == ProcessingResult.REMOVE)
				return true;
			if (result == ProcessingResult.HOLD)
				return false;

			currentItem.locked = false;
			belt.sendData();
			return false;
		}

		// See if any new belt processing catches the item
		if (currentItem.beltPosition > .5f || beltMovementPositive) {
			int firstUpcomingSegment = (int) (currentItem.beltPosition + (beltMovementPositive ? .5f : -.5f));
			int step = beltMovementPositive ? 1 : -1;

			for (int segment = firstUpcomingSegment; beltMovementPositive ? segment + .5f <= nextOffset
				: segment + .5f >= nextOffset; segment += step) {

				BeltProcessingBehaviour processingBehaviour = getBeltProcessingAtSegment(segment);
				TransportedItemStackHandlerBehaviour stackHandlerBehaviour =
					getTransportedItemStackHandlerAtSegment(segment);

				if (processingBehaviour == null)
					continue;
				if (stackHandlerBehaviour == null)
					continue;
				if (BeltProcessingBehaviour.isBlocked(belt.getWorld(), BeltHelper.getPositionForOffset(belt, segment)))
					continue;

				ProcessingResult result = processingBehaviour.handleReceivedItem(currentItem, stackHandlerBehaviour);
				if (result == ProcessingResult.REMOVE)
					return true;

				if (result == ProcessingResult.HOLD) {
					currentItem.beltPosition = segment + .5f + (beltMovementPositive ? 1 / 64f : -1 / 64f);
					currentItem.locked = true;
					belt.sendData();
					return false;
				}
			}
		}

		return false;
	}

	protected BeltProcessingBehaviour getBeltProcessingAtSegment(int segment) {
		return TileEntityBehaviour.get(belt.getWorld(), BeltHelper.getPositionForOffset(belt, segment)
			.up(2), BeltProcessingBehaviour.TYPE);
	}

	protected TransportedItemStackHandlerBehaviour getTransportedItemStackHandlerAtSegment(int segment) {
		return TileEntityBehaviour.get(belt.getWorld(), BeltHelper.getPositionForOffset(belt, segment),
			TransportedItemStackHandlerBehaviour.TYPE);
	}

	private enum Ending {
		UNRESOLVED(0), EJECT(0), INSERT(.25f), FUNNEL(.5f), BLOCKED(.45f);

		private float margin;

		Ending(float f) {
			this.margin = f;
		}
	}

	private Ending resolveEnding() {
		int lastOffset = beltMovementPositive ? belt.beltLength - 1 : 0;
		World world = belt.getWorld();
		BlockPos lastPosition = BeltHelper.getPositionForOffset(belt, lastOffset);
		BlockPos nextPosition = BeltHelper.getPositionForOffset(belt, beltMovementPositive ? belt.beltLength : -1);

		if (AllBlocks.BRASS_BELT_FUNNEL.has(world.getBlockState(lastPosition.up())))
			return Ending.FUNNEL;

		DirectBeltInputBehaviour inputBehaviour =
			TileEntityBehaviour.get(world, nextPosition, DirectBeltInputBehaviour.TYPE);
		if (inputBehaviour != null)
			return Ending.INSERT;

		if (Block.hasSolidSide(world.getBlockState(nextPosition), world, nextPosition, belt.getMovementFacing()
			.getOpposite()))
			return Ending.BLOCKED;

		return Ending.EJECT;
	}

	//

	public boolean canInsertAt(int segment) {
		return canInsertAtFromSide(segment, Direction.UP);
	}

	public boolean canInsertAtFromSide(int segment, Direction side) {
		float segmentPos = segment;
		if (belt.getMovementFacing() == side.getOpposite())
			return false;
		if (belt.getMovementFacing() != side)
			segmentPos += .5f;
		else if (!beltMovementPositive)
			segmentPos += 1f;

		for (TransportedItemStack stack : items)
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
		if (items.isEmpty())
			items.add(newStack);
		else {
			int index = 0;
			for (TransportedItemStack stack : items) {
				if (stack.compareTo(newStack) > 0 == beltMovementPositive)
					break;
				index++;
			}
			items.add(index, newStack);
		}
	}

	public TransportedItemStack getStackAtOffset(int offset) {
		float min = offset;
		float max = offset + 1;
		for (TransportedItemStack stack : items) {
			if (stack.beltPosition > max)
				continue;
			if (stack.beltPosition > min)
				return stack;
		}
		return null;
	}

	public void read(CompoundNBT nbt) {
		items.clear();
		nbt.getList("Items", NBT.TAG_COMPOUND)
			.forEach(inbt -> items.add(TransportedItemStack.read((CompoundNBT) inbt)));
		beltMovementPositive = nbt.getBoolean("PositiveOrder");
	}

	public CompoundNBT write() {
		CompoundNBT nbt = new CompoundNBT();
		ListNBT itemsNBT = new ListNBT();
		items.forEach(stack -> itemsNBT.add(stack.serializeNBT()));
		nbt.put("Items", itemsNBT);
		nbt.putBoolean("PositiveOrder", beltMovementPositive);
		return nbt;
	}

	public void eject(TransportedItemStack stack) {
		ItemStack ejected = stack.stack;
		Vec3d outPos = BeltHelper.getVectorForOffset(belt, stack.beltPosition);
		float movementSpeed = Math.max(Math.abs(belt.getBeltMovementSpeed()), 1 / 8f);
		Vec3d outMotion = new Vec3d(belt.getBeltChainDirection()).scale(movementSpeed)
			.add(0, 1 / 8f, 0);
		outPos.add(outMotion.normalize());
		ItemEntity entity = new ItemEntity(belt.getWorld(), outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
		entity.setMotion(outMotion);
		entity.setDefaultPickupDelay();
		entity.velocityChanged = true;
		belt.getWorld()
			.addEntity(entity);
	}

	public void ejectAll() {
		items.forEach(this::eject);
		items.clear();
	}

	public void applyToEachWithin(float position, float maxDistanceToPosition,
		Function<TransportedItemStack, List<TransportedItemStack>> processFunction) {
		List<TransportedItemStack> toBeAdded = new ArrayList<>();
		boolean dirty = false;
		for (Iterator<TransportedItemStack> iterator = items.iterator(); iterator.hasNext();) {
			TransportedItemStack transportedItemStack = iterator.next();
			ItemStack stackBefore = transportedItemStack.stack.copy();
			if (Math.abs(position - transportedItemStack.beltPosition) < maxDistanceToPosition) {
				List<TransportedItemStack> apply = processFunction.apply(transportedItemStack);
				if (apply == null)
					continue;
				if (apply.size() == 1 && apply.get(0).stack.equals(stackBefore, false))
					continue;
				dirty = true;
				toBeAdded.addAll(apply);
				iterator.remove();
			}
		}
		toBeAdded.forEach(toInsert::add);
		if (dirty) {
			belt.markDirty();
			belt.sendData();
		}
	}

	public List<TransportedItemStack> getTransportedItems() {
		return items;
	}

}
