package com.simibubi.create.content.logistics.block.depot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class DepotTileEntity extends SmartTileEntity {

	TransportedItemStack heldItem;
	ItemStackHandler processingOutputBuffer;

	DepotItemHandler itemHandler;
	LazyOptional<DepotItemHandler> lazyItemHandler;

	public DepotTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		itemHandler = new DepotItemHandler(this);
		lazyItemHandler = LazyOptional.of(() -> itemHandler);
		processingOutputBuffer = new ItemStackHandler(8) {
			protected void onContentsChanged(int slot) {
				markDirty();
				sendData();
			};
		};
	}

	@Override
	public void tick() {
		super.tick();
		if (heldItem == null)
			return;

		heldItem.prevBeltPosition = heldItem.beltPosition;
		heldItem.prevSideOffset = heldItem.sideOffset;
		float diff = .5f - heldItem.beltPosition;
		if (diff > 1 / 512f) {
			if (diff > 1 / 32f && !BeltHelper.isItemUpright(heldItem.stack))
				heldItem.angle += 1;
			heldItem.beltPosition += diff / 4f;
		}

		if (diff > 1 / 16f)
			return;
		if (world.isRemote)
			return;

		BeltProcessingBehaviour processingBehaviour =
			TileEntityBehaviour.get(world, pos.up(2), BeltProcessingBehaviour.TYPE);
		if (processingBehaviour == null)
			return;
		if (!heldItem.locked && BeltProcessingBehaviour.isBlocked(world, pos))
			return;

		boolean wasLocked = heldItem.locked;
		ItemStack previousItem = heldItem.stack;
		TransportedItemStackHandlerBehaviour handler = getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
		ProcessingResult result = wasLocked ? processingBehaviour.handleHeldItem(heldItem, handler)
			: processingBehaviour.handleReceivedItem(heldItem, handler);
		if (result == ProcessingResult.REMOVE) {
			heldItem = null;
			sendData();
			return;
		}

		heldItem.locked = result == ProcessingResult.HOLD;
		if (heldItem.locked != wasLocked || !previousItem.equals(heldItem.stack, false))
			sendData();
	}
	
	@Override
	public void remove() {
		super.remove();
		if (lazyItemHandler != null)
			lazyItemHandler.invalidate();
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		if (heldItem != null)
			compound.put("HeldItem", heldItem.serializeNBT());
		compound.put("OutputBuffer", processingOutputBuffer.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		heldItem = null;
		if (compound.contains("HeldItem"))
			heldItem = TransportedItemStack.read(compound.getCompound("HeldItem"));
		processingOutputBuffer.deserializeNBT(compound.getCompound("OutputBuffer"));
		super.read(compound, clientPacket);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this).setInsertionHandler(this::tryInsertingFromSide));
		behaviours.add(new TransportedItemStackHandlerBehaviour(this, this::applyToAllItems)
			.withStackPlacement(this::getWorldPositionOf));
	}

	public ItemStack getHeldItemStack() {
		return heldItem == null ? ItemStack.EMPTY : heldItem.stack;
	}

	public void setHeldItem(TransportedItemStack heldItem) {
		this.heldItem = heldItem;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return lazyItemHandler.cast();
		return super.getCapability(cap, side);
	}

	private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
		ItemStack inserted = transportedStack.stack;
		ItemStack empty = ItemStack.EMPTY;

		if (!getHeldItemStack().isEmpty())
			return inserted;
		if (!isOutputEmpty())
			return inserted;
		if (simulate)
			return empty;

		transportedStack = transportedStack.copy();
		transportedStack.beltPosition = side.getAxis()
			.isVertical() ? .5f : 0;
		transportedStack.insertedFrom = side;
		transportedStack.prevSideOffset = transportedStack.sideOffset;
		transportedStack.prevBeltPosition = transportedStack.beltPosition;
		setHeldItem(transportedStack);
		markDirty();
		sendData();

		return empty;
	}

	private void applyToAllItems(float maxDistanceFromCentre,
		Function<TransportedItemStack, List<TransportedItemStack>> processFunction) {
		if (heldItem == null)
			return;
		if (.5f - heldItem.beltPosition > maxDistanceFromCentre)
			return;

		boolean dirty = false;
		List<TransportedItemStack> toBeAdded = new ArrayList<>();
		TransportedItemStack transportedItemStack = heldItem;
		ItemStack stackBefore = transportedItemStack.stack.copy();
		List<TransportedItemStack> apply = processFunction.apply(transportedItemStack);

		if (apply == null)
			return;
		if (apply.size() == 1 && apply.get(0).stack.equals(stackBefore, false))
			return;

		dirty = true;
		heldItem = null;
		toBeAdded.addAll(apply);
		for (TransportedItemStack added : toBeAdded) {
			if (heldItem == null) {
				heldItem = added;
				heldItem.beltPosition = 0.5f;
				heldItem.prevBeltPosition = 0.5f;
				continue;
			}
			for (int i = 0; i < processingOutputBuffer.getSlots(); i++) {
				ItemStack stackInSlot = processingOutputBuffer.getStackInSlot(i);
				if (!stackInSlot.isEmpty())
					continue;
				processingOutputBuffer.setStackInSlot(i, added.stack);
				break;
			}
		}

		if (dirty) {
			markDirty();
			sendData();
		}
	}

	public boolean isOutputEmpty() {
		for (int i = 0; i < processingOutputBuffer.getSlots(); i++)
			if (!processingOutputBuffer.getStackInSlot(i)
				.isEmpty())
				return false;
		return true;
	}

	private Vec3d getWorldPositionOf(TransportedItemStack transported) {
		Vec3d offsetVec = new Vec3d(.5f, 14 / 16f, .5f);
		return offsetVec.add(new Vec3d(pos));
	}

}
