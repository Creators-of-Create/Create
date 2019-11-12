package com.simibubi.create.modules.contraptions.relays.belt;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandler;

public class BeltInventory {

	final BeltTileEntity belt;
	final List<TransportedItemStack> items;
	boolean beltMovementPositive;
	final float SEGMENT_WINDOW = .75f;

	public BeltInventory(BeltTileEntity te) {
		this.belt = te;
		items = new LinkedList<>();
	}

	public void tick() {
		
		// Reverse item collection if belt just reversed
		if (beltMovementPositive != movingPositive()) {
			beltMovementPositive = movingPositive();
			Collections.reverse(items);
			belt.markDirty();
			belt.sendData();
		}

		// Assuming the first entry is furthest on the belt
		TransportedItemStack stackInFront = null;
		TransportedItemStack current = null;
		Iterator<TransportedItemStack> iterator = items.iterator();
		float beltSpeed = belt.getBeltMovementSpeed();
		float spacing = 1;

		while (iterator.hasNext()) {
			stackInFront = current;
			current = iterator.next();

			if (current.stack.isEmpty()) {
				iterator.remove();
				current = null;
				continue;
			}

			float movement = beltSpeed;

			// Don't move if other items are waiting in front
			float currentPos = current.beltPosition;
			if (stackInFront != null) {
				float diff = stackInFront.beltPosition - currentPos;
				if (Math.abs(diff) <= spacing)
					continue;
				movement = beltMovementPositive ? Math.min(movement, diff - spacing)
						: Math.max(movement, diff + spacing);
			}

			float diffToEnd = beltMovementPositive ? belt.beltLength - currentPos : -currentPos;
			float limitedMovement = beltMovementPositive ? Math.min(movement, diffToEnd)
					: Math.max(movement, diffToEnd);

			int segmentBefore = (int) currentPos;
			float min = segmentBefore + .5f - (SEGMENT_WINDOW / 2);
			float max = segmentBefore + .5f + (SEGMENT_WINDOW / 2);
			if (currentPos < min || currentPos > max)
				segmentBefore = -1;

			current.beltPosition += limitedMovement;

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
						world.updateComparatorOutputLevel(getPositionForOffset(segment),
								belt.getBlockState().getBlock());
				}
			}

			// End reached
			if (limitedMovement != movement) {
				if (world.isRemote)
					continue;

				BlockPos nextPosition = getPositionForOffset(beltMovementPositive ? belt.beltLength : -1);
				BlockState state = world.getBlockState(nextPosition);
				Direction movementFacing = belt.getMovementFacing();

				// next block is not a belt
				if (!AllBlocks.BELT.typeOf(state)) {
					if (!Block.hasSolidSide(state, world, nextPosition, movementFacing.getOpposite())) {
						eject(current);
						iterator.remove();
						current = null;
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
				BlockPos controller = nextBelt.getController();
				if (!world.isBlockPresent(controller))
					continue;
				te = world.getTileEntity(controller);
				if (te == null || !(te instanceof BeltTileEntity))
					continue;
				BeltTileEntity nextBeltController = (BeltTileEntity) te;
				BeltInventory nextInventory = nextBeltController.getInventory();

				if (!nextInventory.canInsertAt(nextBelt.index))
					continue;

				current.beltPosition = nextBelt.index + .5f;
				current.insertedAt = nextBelt.index;
				nextInventory.insert(current);
				iterator.remove();
				current = null;
				belt.sendData();
				nextBeltController.sendData();
			}

		}

	}

	public static class TransportedItemStack implements Comparable<TransportedItemStack> {
		public ItemStack stack;
		public float beltPosition;
		public float sideOffset;
		public int insertedAt;

		public TransportedItemStack(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public int compareTo(TransportedItemStack o) {
			return beltPosition < o.beltPosition ? 1 : beltPosition > o.beltPosition ? -1 : 0;
		}

		public CompoundNBT serializeNBT() {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Item", stack.serializeNBT());
			nbt.putFloat("Pos", beltPosition);
			nbt.putFloat("Offset", sideOffset);
			nbt.putInt("InSegment", insertedAt);
			return nbt;
		}

		public static TransportedItemStack read(CompoundNBT nbt) {
			TransportedItemStack stack = new TransportedItemStack(ItemStack.read(nbt.getCompound("Item")));
			stack.beltPosition = nbt.getFloat("Pos");
			stack.sideOffset = nbt.getFloat("Offset");
			stack.insertedAt = nbt.getInt("InSegment");
			return stack;
		}

	}

	public boolean canInsertAt(int segment) {
		float min = segment + .5f - (SEGMENT_WINDOW / 2);
		float max = segment + .5f + (SEGMENT_WINDOW / 2);

		for (TransportedItemStack stack : items) {
			float currentPos = stack.beltPosition;

			// Searched past relevant stacks
			if (beltMovementPositive ? currentPos < segment : currentPos - 1 > segment)
				break;

			// Item inside extraction window
			if (currentPos > min && currentPos < max)
				return false;

			// Items on the belt get prioritized if the previous item was inserted on the
			// same segment
			if (stack.insertedAt == segment && currentPos <= segment + 1)
				return false;

		}
		return true;
	}

	protected void insert(TransportedItemStack newStack) {
		int index = 0;
		if (items.isEmpty())
			items.add(newStack);
		for (TransportedItemStack stack : items) {
			if (stack.compareTo(newStack) > 0 == beltMovementPositive)
				break;
			index++;
		}
		items.add(index, newStack);
		belt.markDirty();
		belt.sendData();
	}

	protected TransportedItemStack getStackAtOffset(int offset) {
		float min = offset + .5f - (SEGMENT_WINDOW / 2);
		float max = offset + .5f + (SEGMENT_WINDOW / 2);
		for (TransportedItemStack stack : items) {
			if (stack.beltPosition > max)
				break;
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

	private void eject(TransportedItemStack stack) {
		ItemStack ejected = stack.stack;
		Vec3d outPos = getVectorForOffset(stack.beltPosition);
		ItemEntity entity = new ItemEntity(belt.getWorld(), outPos.x, outPos.y, outPos.z, ejected);
		entity.setMotion(new Vec3d(belt.getBeltChainDirection()).scale(Math.abs(belt.getBeltMovementSpeed())));
		entity.velocityChanged = true;
	}

	private Vec3d getVectorForOffset(float offset) {
		Vec3d vec = VecHelper.getCenterOf(belt.getPos());
		vec.add(new Vec3d(belt.getBeltChainDirection()).scale(offset));
		return vec;
	}

	private BlockPos getPositionForOffset(int offset) {
		BlockPos pos = belt.getPos();
		Vec3i vec = belt.getBeltChainDirection();
		return pos.add(offset * vec.getX(), offset * vec.getY(), offset * vec.getZ());
	}

	private boolean movingPositive() {
		return belt.getBeltMovementSpeed() > 0;
	}

	public class ItemHandlerSegment implements IItemHandler {
		int offset;

		public ItemHandlerSegment(int offset) {
			this.offset = offset;
		}

		@Override
		public int getSlots() {
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			TransportedItemStack stackAtOffset = getStackAtOffset(offset);
			if (stackAtOffset == null)
				return ItemStack.EMPTY;
			return stackAtOffset.stack;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (canInsertAt(offset)) {
				if (!simulate) {
					TransportedItemStack newStack = new TransportedItemStack(stack);
					newStack.insertedAt = offset;
					newStack.beltPosition = offset + .5f;
					insert(newStack);
				}
				return ItemStack.EMPTY;
			}
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			TransportedItemStack transported = getStackAtOffset(offset);
			if (transported == null)
				return ItemStack.EMPTY;

			amount = Math.min(amount, transported.stack.getCount());
			ItemStack extracted = simulate ? transported.stack.copy().split(amount) : transported.stack.split(amount);
			if (!simulate) {
				belt.markDirty();
				belt.sendData();
			}
			return extracted;
		}

		@Override
		public int getSlotLimit(int slot) {
			return Math.min(getStackInSlot(slot).getMaxStackSize(), 64);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return true;
		}

	}

	public IItemHandler createHandlerForSegment(int segment) {
		return new ItemHandlerSegment(segment);
	}

}
