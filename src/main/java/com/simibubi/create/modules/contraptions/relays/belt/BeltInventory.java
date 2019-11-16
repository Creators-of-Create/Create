package com.simibubi.create.modules.contraptions.relays.belt;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.BeltAttachmentState;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

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

		float beltSpeed = belt.getDirectionAwareBeltMovementSpeed();
		float spacing = 1;

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

			// Don't move if locked
			boolean onClient = belt.getWorld().isRemote;
			if (onClient && current.locked)
				continue;

			// Don't move if other items are waiting in front
			float currentPos = current.beltPosition;
			if (stackInFront != null) {
				float diff = stackInFront.beltPosition - currentPos;
				if (Math.abs(diff) <= spacing)
					continue;
				movement = beltMovementPositive ? Math.min(movement, diff - spacing)
						: Math.max(movement, diff + spacing);
			}

			// Determine current segment
			int segmentBefore = (int) currentPos;
			float min = segmentBefore + .5f - (SEGMENT_WINDOW / 2);
			float max = segmentBefore + .5f + (SEGMENT_WINDOW / 2);
			if (currentPos < min || currentPos > max)
				segmentBefore = -1;

			// Don't move beyond the edge
			float diffToEnd = beltMovementPositive ? belt.beltLength - currentPos : -currentPos;
			float limitedMovement = beltMovementPositive ? Math.min(movement, diffToEnd)
					: Math.max(movement, diffToEnd);

			if (!onClient) {
				// Don't move if belt attachments want to continue processing
				if (segmentBefore != -1 && current.locked) {
					BeltTileEntity beltSegment = getBeltSegment(segmentBefore);
					if (beltSegment != null) {

						current.locked = false;
						for (BeltAttachmentState attachmentState : beltSegment.attachmentTracker.attachments) {
							if (attachmentState.attachment.processItem(beltSegment, current, attachmentState))
								current.locked = true;
						}
						if (!current.locked || current.stack.isEmpty())
							belt.sendData();
						continue;
					}
				}

				// See if any new belt processing catches the item
				int upcomingSegment = (int) (current.beltPosition + (beltMovementPositive ? .5f : -.5f));
				for (int segment = upcomingSegment; beltMovementPositive
						? segment <= current.beltPosition + limitedMovement
						: segment >= current.beltPosition + limitedMovement; segment += beltMovementPositive ? 1 : -1) {
					BeltTileEntity beltSegment = getBeltSegment(segmentBefore);
					if (beltSegment == null)
						break;
					for (BeltAttachmentState attachmentState : beltSegment.attachmentTracker.attachments) {
						if (attachmentState.attachment.startProcessingItem(beltSegment, current, attachmentState)) {
							current.beltPosition += (segment + .5f) - current.beltPosition;
							current.locked = true;
							belt.sendData();
							continue Items;
						}
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

				// next block is a basin or a saw
				if (AllBlocks.BASIN.typeOf(state) || AllBlocks.SAW.typeOf(state)) {
					TileEntity te = world.getTileEntity(nextPosition);
					if (te != null) {
						LazyOptional<IItemHandler> optional = te
								.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
						if (optional.isPresent()) {
							IItemHandler itemHandler = optional.orElse(null);
							ItemStack remainder = ItemHandlerHelper.insertItemStacked(itemHandler, current.stack.copy(),
									false);
							if (remainder.equals(current.stack, false))
								continue;

							current.stack = remainder;
							if (remainder.isEmpty()) {
								iterator.remove();
								current = null;
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
					belt.sendData();
				}

			}

		}

	}

	public static class TransportedItemStack implements Comparable<TransportedItemStack> {
		public ItemStack stack;
		public float beltPosition;
		public float sideOffset;
		public int angle;
		public int insertedAt;
		public Direction insertedFrom;
		public boolean locked;

		public float prevBeltPosition;
		public float prevSideOffset;

		public TransportedItemStack(ItemStack stack) {
			this.stack = stack;
			angle = new Random().nextInt(360);
			sideOffset = prevSideOffset = getTargetSideOffset();
			insertedFrom = Direction.UP;
		}

		public float getTargetSideOffset() {
			return (angle - 180) / (360 * 3f);
		}

		@Override
		public int compareTo(TransportedItemStack o) {
			return beltPosition < o.beltPosition ? 1 : beltPosition > o.beltPosition ? -1 : 0;
		}

		public CompoundNBT serializeNBT() {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Item", stack.serializeNBT());
			nbt.putFloat("Pos", beltPosition);
			nbt.putFloat("PrevPos", prevBeltPosition);
			nbt.putFloat("Offset", sideOffset);
			nbt.putFloat("PrevOffset", prevSideOffset);
			nbt.putInt("InSegment", insertedAt);
			nbt.putInt("Angle", angle);
			nbt.putInt("InDirection", insertedFrom.getIndex());
			nbt.putBoolean("Locked", locked);
			return nbt;
		}

		public static TransportedItemStack read(CompoundNBT nbt) {
			TransportedItemStack stack = new TransportedItemStack(ItemStack.read(nbt.getCompound("Item")));
			stack.beltPosition = nbt.getFloat("Pos");
			stack.prevBeltPosition = nbt.getFloat("PrevPos");
			stack.sideOffset = nbt.getFloat("Offset");
			stack.prevSideOffset = nbt.getFloat("PrevOffset");
			stack.insertedAt = nbt.getInt("InSegment");
			stack.angle = nbt.getInt("Angle");
			stack.insertedFrom = Direction.byIndex(nbt.getInt("InDirection"));
			stack.locked = nbt.getBoolean("Locked");
			return stack;
		}

	}

	public boolean canInsertAt(int segment) {
		return canInsertFrom(segment, Direction.UP);
	}

	public boolean canInsertFrom(int segment, Direction side) {
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
			if (stack.insertedAt == segment && stack.insertedFrom == side
					&& (beltMovementPositive ? currentPos <= segment + 1.5 : currentPos - 1.5 >= segment))
				return false;

		}
		return true;
	}

	protected void insert(TransportedItemStack newStack) {
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

		belt.markDirty();
		belt.sendData();
	}

	public TransportedItemStack getStackAtOffset(int offset) {
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

	public void eject(TransportedItemStack stack) {
		ItemStack ejected = stack.stack;
		Vec3d outPos = getVectorForOffset(stack.beltPosition);
		float movementSpeed = Math.max(Math.abs(belt.getBeltMovementSpeed()), 1 / 8f);
		Vec3d outMotion = new Vec3d(belt.getBeltChainDirection()).scale(movementSpeed).add(0, 1 / 8f, 0);
		outPos.add(outMotion.normalize());
		ItemEntity entity = new ItemEntity(belt.getWorld(), outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
		entity.setMotion(outMotion);
		entity.velocityChanged = true;
		belt.getWorld().addEntity(entity);
	}

	private Vec3d getVectorForOffset(float offset) {
		Slope slope = belt.getBlockState().get(BeltBlock.SLOPE);
		int verticality = slope == Slope.DOWNWARD ? -1 : slope == Slope.UPWARD ? 1 : 0;
		float verticalMovement = verticality;
		if (offset < .5)
			verticalMovement = 0;
		verticalMovement = verticalMovement * (Math.min(offset, belt.beltLength - .5f) - .5f);

		Vec3d vec = VecHelper.getCenterOf(belt.getPos());
		vec = vec.add(new Vec3d(belt.getBeltFacing().getDirectionVec()).scale(offset - .5f)).add(0, verticalMovement,
				0);
		return vec;
	}

	private BeltTileEntity getBeltSegment(int segment) {
		BlockPos pos = getPositionForOffset(segment);
		TileEntity te = belt.getWorld().getTileEntity(pos);
		if (te == null || !(te instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) te;
	}

	private BlockPos getPositionForOffset(int offset) {
		BlockPos pos = belt.getPos();
		Vec3i vec = belt.getBeltFacing().getDirectionVec();
		Slope slope = belt.getBlockState().get(BeltBlock.SLOPE);
		int verticality = slope == Slope.DOWNWARD ? -1 : slope == Slope.UPWARD ? 1 : 0;

		return pos.add(offset * vec.getX(), MathHelper.clamp(offset, 0, belt.beltLength - 1) * verticality,
				offset * vec.getZ());
	}

	private boolean movingPositive() {
		return belt.getDirectionAwareBeltMovementSpeed() > 0;
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
					newStack.prevBeltPosition = newStack.beltPosition;
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
