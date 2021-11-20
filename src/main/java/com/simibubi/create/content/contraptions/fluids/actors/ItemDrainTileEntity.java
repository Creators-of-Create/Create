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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.simibubi.create.lib.transfer.TransferUtil;
import com.simibubi.create.lib.transfer.fluid.FluidStack;
import com.simibubi.create.lib.transfer.fluid.FluidTransferable;
import com.simibubi.create.lib.transfer.fluid.IFluidHandler;
import com.simibubi.create.lib.transfer.item.IItemHandler;
import com.simibubi.create.lib.transfer.item.ItemHandlerHelper;
import com.simibubi.create.lib.transfer.item.ItemTransferable;
import com.simibubi.create.lib.utility.LazyOptional;

import org.jetbrains.annotations.Nullable;

public class ItemDrainTileEntity extends SmartTileEntity implements IHaveGoggleInformation, FluidTransferable, ItemTransferable {

	public static final int FILLING_TIME = 20;

	SmartFluidTankBehaviour internalTank;
	TransportedItemStack heldItem;
	protected int processingTicks;
	Map<Direction, LazyOptional<ItemDrainItemHandler>> itemHandlers;

	public ItemDrainTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
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
					Vec3 outPos = VecHelper.getCenterOf(worldPosition)
						.add(Vec3.atLowerCornerOf(side.getNormal())
							.scale(.75));
					float movementSpeed = itemMovementPerTick();
					Vec3 outMotion = Vec3.atLowerCornerOf(side.getNormal())
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
				.fill(fluidFromItem, true) != fluidFromItem.getAmount()) {
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
			.fill(fluidFromItem, false);
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
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("ProcessingTicks", processingTicks);
		if (heldItem != null)
			compound.put("HeldItem", heldItem.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(CompoundTag compound, boolean clientPacket) {
		heldItem = null;
		processingTicks = compound.getInt("ProcessingTicks");
		if (compound.contains("HeldItem"))
			heldItem = TransportedItemStack.read(compound.getCompound("HeldItem"));
		super.fromTag(compound, clientPacket);
	}

	@Nullable
	@Override
	public IFluidHandler getFluidHandler(@Nullable Direction direction) {
		if (direction != Direction.UP) {
			return internalTank.getCapability().getValueUnsafer();
		}
		return null;
	}

	@Nullable
	@Override
	public IItemHandler getItemHandler(@Nullable Direction direction) {
		if (direction != null && direction.getAxis().isHorizontal()) {
			return itemHandlers.get(direction).orElse(null);
		}
		return null;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return containedFluidTooltip(tooltip, isPlayerSneaking, TransferUtil.getFluidHandler(this));
	}
}
