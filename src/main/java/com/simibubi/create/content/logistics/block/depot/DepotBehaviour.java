package com.simibubi.create.content.logistics.block.depot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.funnel.AbstractFunnelBlock;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class DepotBehaviour extends TileEntityBehaviour {

	public static BehaviourType<DepotBehaviour> TYPE = new BehaviourType<>();

	TransportedItemStack heldItem;
	List<TransportedItemStack> incoming;
	ItemStackHandler processingOutputBuffer;
	DepotItemHandler itemHandler;
	LazyOptional<DepotItemHandler> lazyItemHandler;
	TransportedItemStackHandlerBehaviour transportedHandler;
	Supplier<Integer> maxStackSize;
	Supplier<Boolean> canAcceptItems;
	Predicate<Direction> canFunnelsPullFrom;
	boolean allowMerge;

	public DepotBehaviour(SmartTileEntity te) {
		super(te);
		maxStackSize = () -> 64;
		canAcceptItems = () -> true;
		canFunnelsPullFrom = $ -> true;
		incoming = new ArrayList<>();
		itemHandler = new DepotItemHandler(this);
		lazyItemHandler = LazyOptional.of(() -> itemHandler);
		processingOutputBuffer = new ItemStackHandler(8) {
			protected void onContentsChanged(int slot) {
				te.notifyUpdate();
			};
		};
	}

	public void enableMerging() {
		allowMerge = true;
	}

	@Override
	public void tick() {
		super.tick();

		World world = tileEntity.getLevel();

		for (Iterator<TransportedItemStack> iterator = incoming.iterator(); iterator.hasNext();) {
			TransportedItemStack ts = iterator.next();
			if (!tick(ts))
				continue;
			if (world.isClientSide && !tileEntity.isVirtual())
				continue;
			if (heldItem == null) {
				heldItem = ts;
			} else {
				if (!ItemHelper.canItemStackAmountsStack(heldItem.stack, ts.stack)) {
					Vector3d vec = VecHelper.getCenterOf(tileEntity.getBlockPos());
					InventoryHelper.dropItemStack(tileEntity.getLevel(), vec.x, vec.y + .5f, vec.z, ts.stack);
				} else {
					heldItem.stack.grow(ts.stack.getCount());
				}
			}
			iterator.remove();
			tileEntity.notifyUpdate();
		}

		if (heldItem == null)
			return;
		if (!tick(heldItem))
			return;

		BlockPos pos = tileEntity.getBlockPos();

		if (world.isClientSide)
			return;
		if (handleBeltFunnelOutput())
			return;

		BeltProcessingBehaviour processingBehaviour =
			TileEntityBehaviour.get(world, pos.above(2), BeltProcessingBehaviour.TYPE);
		if (processingBehaviour == null)
			return;
		if (!heldItem.locked && BeltProcessingBehaviour.isBlocked(world, pos))
			return;

		ItemStack previousItem = heldItem.stack;
		boolean wasLocked = heldItem.locked;
		ProcessingResult result = wasLocked ? processingBehaviour.handleHeldItem(heldItem, transportedHandler)
			: processingBehaviour.handleReceivedItem(heldItem, transportedHandler);
		if (result == ProcessingResult.REMOVE) {
			heldItem = null;
			tileEntity.sendData();
			return;
		}

		heldItem.locked = result == ProcessingResult.HOLD;
		if (heldItem.locked != wasLocked || !previousItem.equals(heldItem.stack, false))
			tileEntity.sendData();
	}

	protected boolean tick(TransportedItemStack heldItem) {
		heldItem.prevBeltPosition = heldItem.beltPosition;
		heldItem.prevSideOffset = heldItem.sideOffset;
		float diff = .5f - heldItem.beltPosition;
		if (diff > 1 / 512f) {
			if (diff > 1 / 32f && !BeltHelper.isItemUpright(heldItem.stack))
				heldItem.angle += 1;
			heldItem.beltPosition += diff / 4f;
		}
		return diff < 1 / 16f;
	}

	private boolean handleBeltFunnelOutput() {
		BlockState funnel = getWorld().getBlockState(getPos().above());
		Direction funnelFacing = AbstractFunnelBlock.getFunnelFacing(funnel);
		if (funnelFacing == null || !canFunnelsPullFrom.test(funnelFacing.getOpposite()))
			return false;

		for (int slot = 0; slot < processingOutputBuffer.getSlots(); slot++) {
			ItemStack previousItem = processingOutputBuffer.getStackInSlot(slot);
			if (previousItem.isEmpty())
				continue;
			ItemStack afterInsert = tileEntity.getBehaviour(DirectBeltInputBehaviour.TYPE)
				.tryExportingToBeltFunnel(previousItem, null, false);
			if (afterInsert == null)
				return false;
			if (previousItem.getCount() != afterInsert.getCount()) {
				processingOutputBuffer.setStackInSlot(slot, afterInsert);
				tileEntity.notifyUpdate();
				return true;
			}
		}

		ItemStack previousItem = heldItem.stack;
		ItemStack afterInsert = tileEntity.getBehaviour(DirectBeltInputBehaviour.TYPE)
			.tryExportingToBeltFunnel(previousItem, null, false);
		if (afterInsert == null)
			return false;
		if (previousItem.getCount() != afterInsert.getCount()) {
			if (afterInsert.isEmpty())
				heldItem = null;
			else
				heldItem.stack = afterInsert;
			tileEntity.notifyUpdate();
			return true;
		}

		return false;
	}

	@Override
	public void remove() {
		if (lazyItemHandler != null)
			lazyItemHandler.invalidate();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		if (heldItem != null)
			compound.put("HeldItem", heldItem.serializeNBT());
		compound.put("OutputBuffer", processingOutputBuffer.serializeNBT());
		if (canMergeItems() && !incoming.isEmpty())
			compound.put("Incoming", NBTHelper.writeCompoundList(incoming, TransportedItemStack::serializeNBT));
	}

	@Override
	public void read(CompoundNBT compound, boolean clientPacket) {
		heldItem = null;
		if (compound.contains("HeldItem"))
			heldItem = TransportedItemStack.read(compound.getCompound("HeldItem"));
		processingOutputBuffer.deserializeNBT(compound.getCompound("OutputBuffer"));
		if (canMergeItems()) {
			ListNBT list = compound.getList("Incoming", NBT.TAG_COMPOUND);
			incoming = NBTHelper.readCompoundList(list, TransportedItemStack::read);
		}
	}

	public void addSubBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(tileEntity).allowingBeltFunnels()
			.setInsertionHandler(this::tryInsertingFromSide));
		transportedHandler = new TransportedItemStackHandlerBehaviour(tileEntity, this::applyToAllItems)
			.withStackPlacement(this::getWorldPositionOf);
		behaviours.add(transportedHandler);
	}

	public ItemStack getHeldItemStack() {
		return heldItem == null ? ItemStack.EMPTY : heldItem.stack;
	}

	public boolean canMergeItems() {
		return allowMerge;
	}

	public int getPresentStackSize() {
		int cumulativeStackSize = 0;
		cumulativeStackSize += getHeldItemStack().getCount();
		for (int slot = 0; slot < processingOutputBuffer.getSlots(); slot++)
			cumulativeStackSize += processingOutputBuffer.getStackInSlot(slot)
				.getCount();
		return cumulativeStackSize;
	}

	public int getRemainingSpace() {
		int cumulativeStackSize = getPresentStackSize();
		for (TransportedItemStack transportedItemStack : incoming)
			cumulativeStackSize += transportedItemStack.stack.getCount();
		int fromGetter = maxStackSize.get();
		return (fromGetter == 0 ? 64 : fromGetter) - cumulativeStackSize;
	}

	public ItemStack insert(TransportedItemStack heldItem, boolean simulate) {
		if (!canAcceptItems.get())
			return heldItem.stack;

		if (canMergeItems()) {
			int remainingSpace = getRemainingSpace();
			ItemStack inserted = heldItem.stack;
			if (remainingSpace <= 0)
				return inserted;
			if (this.heldItem != null && !ItemHelper.canItemStackAmountsStack(this.heldItem.stack, inserted))
				return inserted;

			ItemStack returned = ItemStack.EMPTY;
			if (remainingSpace < inserted.getCount()) {
				returned = ItemHandlerHelper.copyStackWithSize(heldItem.stack, inserted.getCount() - remainingSpace);
				if (!simulate) {
					TransportedItemStack copy = heldItem.copy();
					copy.stack.setCount(remainingSpace);
					if (this.heldItem != null)
						incoming.add(copy);
					else
						this.heldItem = copy;
				}
			} else {
				if (!simulate) {
					if (this.heldItem != null)
						incoming.add(heldItem);
					else
						this.heldItem = heldItem;
				}
			}
			return returned;
		}

		if (!simulate) {
			if (this.isEmpty()) {
				if (heldItem.insertedFrom.getAxis()
					.isHorizontal())
					AllSoundEvents.DEPOT_SLIDE.playOnServer(getWorld(), getPos());
				else
					AllSoundEvents.DEPOT_PLOP.playOnServer(getWorld(), getPos());
			}
			this.heldItem = heldItem;
		}
		return ItemStack.EMPTY;
	}

	public void setHeldItem(TransportedItemStack heldItem) {
		this.heldItem = heldItem;
	}

	public void removeHeldItem() {
		this.heldItem = null;
	}

	public void setCenteredHeldItem(TransportedItemStack heldItem) {
		this.heldItem = heldItem;
		this.heldItem.beltPosition = 0.5f;
		this.heldItem.prevBeltPosition = 0.5f;
	}

	public <T> LazyOptional<T> getItemCapability(Capability<T> cap, Direction side) {
		return lazyItemHandler.cast();
	}

	private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
		ItemStack inserted = transportedStack.stack;

		if (!getHeldItemStack().isEmpty() && !canMergeItems())
			return inserted;
		if (!isOutputEmpty() && !canMergeItems())
			return inserted;
		if (!canAcceptItems.get())
			return inserted;

		int size = transportedStack.stack.getCount();
		transportedStack = transportedStack.copy();
		transportedStack.beltPosition = side.getAxis()
			.isVertical() ? .5f : 0;
		transportedStack.insertedFrom = side;
		transportedStack.prevSideOffset = transportedStack.sideOffset;
		transportedStack.prevBeltPosition = transportedStack.beltPosition;
		ItemStack remainder = insert(transportedStack, simulate);
		if (remainder.getCount() != size)
			tileEntity.notifyUpdate();

		return remainder;
	}

	private void applyToAllItems(float maxDistanceFromCentre,
		Function<TransportedItemStack, TransportedResult> processFunction) {
		if (heldItem == null)
			return;
		if (.5f - heldItem.beltPosition > maxDistanceFromCentre)
			return;

		boolean dirty = false;
		TransportedItemStack transportedItemStack = heldItem;
		ItemStack stackBefore = transportedItemStack.stack.copy();
		TransportedResult result = processFunction.apply(transportedItemStack);
		if (result == null || result.didntChangeFrom(stackBefore))
			return;

		dirty = true;
		heldItem = null;
		if (result.hasHeldOutput())
			setCenteredHeldItem(result.getHeldOutput());

		for (TransportedItemStack added : result.getOutputs()) {
			if (getHeldItemStack().isEmpty()) {
				setCenteredHeldItem(added);
				continue;
			}
			ItemStack remainder = ItemHandlerHelper.insertItemStacked(processingOutputBuffer, added.stack, false);
			Vector3d vec = VecHelper.getCenterOf(tileEntity.getBlockPos());
			InventoryHelper.dropItemStack(tileEntity.getLevel(), vec.x, vec.y + .5f, vec.z, remainder);
		}

		if (dirty)
			tileEntity.notifyUpdate();
	}

	public boolean isEmpty() {
		return heldItem == null && isOutputEmpty();
	}

	public boolean isOutputEmpty() {
		for (int i = 0; i < processingOutputBuffer.getSlots(); i++)
			if (!processingOutputBuffer.getStackInSlot(i)
				.isEmpty())
				return false;
		return true;
	}

	private Vector3d getWorldPositionOf(TransportedItemStack transported) {
		Vector3d offsetVec = new Vector3d(.5f, 14 / 16f, .5f);
		return offsetVec.add(Vector3d.atLowerCornerOf(tileEntity.getBlockPos()));
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
