package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemDrainTileEntity extends SmartTileEntity implements IHaveGoggleInformation {

	public static final int FILLING_TIME = 20;

	SmartFluidTankBehaviour internalTank;
	TransportedItemStack heldItem;
	protected int processingTicks;
	Map<Direction, LazyOptional<ItemDrainItemHandler>> itemHandlers;

	public ItemDrainTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		itemHandlers = new IdentityHashMap<>();
		for (Direction d : Iterate.horizontalDirections) {
			ItemDrainItemHandler itemDrainItemHandler = new ItemDrainItemHandler(this, d);
			itemHandlers.put(d, LazyOptional.of(() -> itemDrainItemHandler));
		}
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnels()
			.setInsertionHandler(this::tryInsertingFromSide));
		behaviours.add(internalTank = SmartFluidTankBehaviour.single(this, 1500)
			.allowExtraction()
			.forbidInsertion());
	}

	private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
		ItemStack inserted = transportedStack.stack;
		ItemStack returned = ItemStack.EMPTY;

		if (!getHeldItemStack().isEmpty())
			return inserted;

		if (inserted.getCount() > 1 && EmptyingByBasin.canItemBeEmptied(level, inserted)) {
			returned = ItemHandlerHelper.copyStackWithSize(inserted, inserted.getCount() - 1);
			inserted = ItemHandlerHelper.copyStackWithSize(inserted, 1);
		}

		if (simulate)
			return returned;

		transportedStack = transportedStack.copy();
		transportedStack.beltPosition = side.getAxis()
			.isVertical() ? .5f : 0;
		transportedStack.prevSideOffset = transportedStack.sideOffset;
		transportedStack.prevBeltPosition = transportedStack.beltPosition;
		setHeldItem(transportedStack, side);
		setChanged();
		sendData();

		return returned;
	}

	public ItemStack getHeldItemStack() {
		return heldItem == null ? ItemStack.EMPTY : heldItem.stack;
	}

	@Override
	public void tick() {
		super.tick();

		if (heldItem == null) {
			processingTicks = 0;
			return;
		}

		boolean onClient = level.isClientSide && !isVirtual();

		if (processingTicks > 0) {
			heldItem.prevBeltPosition = .5f;
			boolean wasAtBeginning = processingTicks == FILLING_TIME;
			if (!onClient || processingTicks < FILLING_TIME)
				processingTicks--;
			if (!continueProcessing()) {
				processingTicks = 0;
				notifyUpdate();
				return;
			}
			if (wasAtBeginning != (processingTicks == FILLING_TIME))
				sendData();
			return;
		}

		heldItem.prevBeltPosition = heldItem.beltPosition;
		heldItem.prevSideOffset = heldItem.sideOffset;

		heldItem.beltPosition += itemMovementPerTick();
		if (heldItem.beltPosition > 1) {
			heldItem.beltPosition = 1;

			if (onClient)
				return;

			Direction side = heldItem.insertedFrom;

			ItemStack tryExportingToBeltFunnel = getBehaviour(DirectBeltInputBehaviour.TYPE)
				.tryExportingToBeltFunnel(heldItem.stack, side.getOpposite(), false);
			if (tryExportingToBeltFunnel != null) {
				if (tryExportingToBeltFunnel.getCount() != heldItem.stack.getCount()) {
					if (tryExportingToBeltFunnel.isEmpty())
						heldItem = null;
					else
						heldItem.stack = tryExportingToBeltFunnel;
					notifyUpdate();
					return;
				}
				if (!tryExportingToBeltFunnel.isEmpty())
					return;
			}

			BlockPos nextPosition = worldPosition.relative(side);
			DirectBeltInputBehaviour directBeltInputBehaviour =
				TileEntityBehaviour.get(level, nextPosition, DirectBeltInputBehaviour.TYPE);
			if (directBeltInputBehaviour == null) {
				if (!BlockHelper.hasBlockSolidSide(level.getBlockState(nextPosition), level, nextPosition,
					side.getOpposite())) {
					ItemStack ejected = heldItem.stack;
					Vector3d outPos = VecHelper.getCenterOf(worldPosition)
						.add(Vector3d.atLowerCornerOf(side.getNormal())
							.scale(.75));
					float movementSpeed = itemMovementPerTick();
					Vector3d outMotion = Vector3d.atLowerCornerOf(side.getNormal())
						.scale(movementSpeed)
						.add(0, 1 / 8f, 0);
					outPos.add(outMotion.normalize());
					ItemEntity entity = new ItemEntity(level, outPos.x, outPos.y + 6 / 16f, outPos.z, ejected);
					entity.setDeltaMovement(outMotion);
					entity.setDefaultPickUpDelay();
					entity.hurtMarked = true;
					level.addFreshEntity(entity);

					heldItem = null;
					notifyUpdate();
				}
				return;
			}

			if (!directBeltInputBehaviour.canInsertFromSide(side))
				return;

			ItemStack returned = directBeltInputBehaviour.handleInsertion(heldItem.copy(), side, false);

			if (returned.isEmpty()) {
				if (level.getBlockEntity(nextPosition) instanceof ItemDrainTileEntity)
					AllTriggers.triggerForNearbyPlayers(AllTriggers.CHAINED_ITEM_DRAIN, level, worldPosition, 5);
				heldItem = null;
				notifyUpdate();
				return;
			}

			if (returned.getCount() != heldItem.stack.getCount()) {
				heldItem.stack = returned;
				notifyUpdate();
				return;
			}

			return;
		}

		if (heldItem.prevBeltPosition < .5f && heldItem.beltPosition >= .5f) {
			if (!EmptyingByBasin.canItemBeEmptied(level, heldItem.stack))
				return;
			heldItem.beltPosition = .5f;
			if (onClient)
				return;
			processingTicks = FILLING_TIME;
			sendData();
		}

	}

	protected boolean continueProcessing() {
		if (level.isClientSide && !isVirtual())
			return true;
		if (processingTicks < 5)
			return true;
		if (!EmptyingByBasin.canItemBeEmptied(level, heldItem.stack))
			return false;

		Pair<FluidStack, ItemStack> emptyItem = EmptyingByBasin.emptyItem(level, heldItem.stack, true);
		FluidStack fluidFromItem = emptyItem.getFirst();

		if (processingTicks > 5) {
			internalTank.allowInsertion();
			if (internalTank.getPrimaryHandler()
				.fill(fluidFromItem, FluidAction.SIMULATE) != fluidFromItem.getAmount()) {
				internalTank.forbidInsertion();
				processingTicks = FILLING_TIME;
				return true;
			}
			internalTank.forbidInsertion();
			return true;
		}

		emptyItem = EmptyingByBasin.emptyItem(level, heldItem.stack.copy(), false);
		AllTriggers.triggerForNearbyPlayers(AllTriggers.ITEM_DRAIN, level, worldPosition, 5);

		// Process finished
		ItemStack out = emptyItem.getSecond();
		if (!out.isEmpty())
			heldItem.stack = out;
		else
			heldItem = null;
		internalTank.allowInsertion();
		internalTank.getPrimaryHandler()
			.fill(fluidFromItem, FluidAction.EXECUTE);
		internalTank.forbidInsertion();
		notifyUpdate();
		return true;
	}

	private float itemMovementPerTick() {
		return 1 / 8f;
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		for (LazyOptional<ItemDrainItemHandler> lazyOptional : itemHandlers.values())
			lazyOptional.invalidate();
	}

	public void setHeldItem(TransportedItemStack heldItem, Direction insertedFrom) {
		this.heldItem = heldItem;
		this.heldItem.insertedFrom = insertedFrom;
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putInt("ProcessingTicks", processingTicks);
		if (heldItem != null)
			compound.put("HeldItem", heldItem.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		heldItem = null;
		processingTicks = compound.getInt("ProcessingTicks");
		if (compound.contains("HeldItem"))
			heldItem = TransportedItemStack.read(compound.getCompound("HeldItem"));
		super.fromTag(state, compound, clientPacket);
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (side != null && side.getAxis()
			.isHorizontal() && isItemHandlerCap(cap))
			return itemHandlers.get(side)
				.cast();

		if (side != Direction.UP && isFluidHandlerCap(cap))
			return internalTank.getCapability()
				.cast();

		return super.getCapability(cap, side);
	}

	@Override
	public boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
		return containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY));
	}

}
