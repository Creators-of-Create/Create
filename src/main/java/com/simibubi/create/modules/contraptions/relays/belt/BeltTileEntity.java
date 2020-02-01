package com.simibubi.create.modules.contraptions.relays.belt;

import static com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part.END;
import static com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part.MIDDLE;
import static com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope.DOWNWARD;
import static com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope.HORIZONTAL;
import static com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope.UPWARD;
import static net.minecraft.util.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.Direction.AxisDirection.POSITIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.AllBeltAttachments.Tracker;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Part;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;
import com.simibubi.create.modules.contraptions.relays.belt.BeltMovementHandler.TransportedEntityInfo;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltTileEntity extends KineticTileEntity {

	public Map<Entity, TransportedEntityInfo> passengers;
	public AllBeltAttachments.Tracker attachmentTracker;
	public int color;
	public int beltLength;
	public int index;
	public Direction lastInsert;

	protected BlockPos controller;
	protected BeltInventory inventory;
	protected LazyOptional<IItemHandler> itemHandler;

	private CompoundNBT trackerUpdateTag;

	public BeltTileEntity() {
		super(AllTileEntities.BELT.type);
		controller = BlockPos.ZERO;
		attachmentTracker = new Tracker(this);
		itemHandler = LazyOptional.empty();
		color = -1;
		beltLength = -1;
		index = -1;
	}

	@Override
	public void tick() {
		super.tick();

		// Initialize Belt Attachments
		if (world != null && trackerUpdateTag != null) {
			attachmentTracker.readAndSearch(trackerUpdateTag, this);
			trackerUpdateTag = null;
		}
		if (getSpeed() == 0)
			return;

		initializeItemHandler();

		// Move Items
		if (!isController())
			return;
		getInventory().tick();

		// Move Entities
		if (passengers == null)
			passengers = new HashMap<>();

		List<Entity> toRemove = new ArrayList<>();
		passengers.forEach((entity, info) -> {
			boolean canBeTransported = BeltMovementHandler.canBeTransported(entity);
			boolean leftTheBelt = info.ticksSinceLastCollision > ((getBlockState().get(BeltBlock.SLOPE) != HORIZONTAL)
					? 3
					: 1);
			if (!canBeTransported || leftTheBelt) {
				toRemove.add(entity);
				return;
			}

			info.tick();
			BeltMovementHandler.transportEntity(this, entity, info);
		});
		toRemove.forEach(passengers::remove);
	}

	@Override
	public float getStressApplied() {
		if (!isController())
			return 0;
		return super.getStressApplied();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (!isController())
			return super.getRenderBoundingBox();
		return super.getRenderBoundingBox().grow(beltLength);
	}

	protected void initializeItemHandler() {
		if (world.isRemote || itemHandler.isPresent())
			return;
		if (!world.isBlockPresent(controller))
			return;
		TileEntity te = world.getTileEntity(controller);
		if (te == null || !(te instanceof BeltTileEntity))
			return;
		IItemHandler handler = ((BeltTileEntity) te).getInventory().createHandlerForSegment(index);
		itemHandler = LazyOptional.of(() -> handler);
	}

	@Override
	public boolean hasFastRenderer() {
		return !isController();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (side == Direction.UP || BeltBlock.canAccessFromSide(side, getBlockState())) {
				return itemHandler.cast();
			}
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void remove() {
		super.remove();
		itemHandler.invalidate();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		attachmentTracker.write(compound);
		compound.put("Controller", NBTUtil.writeBlockPos(controller));
		compound.putInt("Color", color);
		compound.putInt("Length", beltLength);
		compound.putInt("Index", index);

		if (isController())
			compound.put("Inventory", getInventory().write());
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		trackerUpdateTag = compound;
		controller = NBTUtil.readBlockPos(compound.getCompound("Controller"));
		color = compound.getInt("Color");
		beltLength = compound.getInt("Length");
		index = compound.getInt("Index");

		if (isController())
			getInventory().read(compound.getCompound("Inventory"));
	}

	public void applyColor(DyeColor colorIn) {
		int colorValue = colorIn.getMapColor().colorValue;
		for (BlockPos blockPos : BeltBlock.getBeltChain(world, getController())) {
			BeltTileEntity belt = (BeltTileEntity) world.getTileEntity(blockPos);
			if (belt == null)
				continue;
			belt.color = belt.color == -1 ? colorValue : ColorHelper.mixColors(belt.color, colorValue, .5f);
			belt.markDirty();
			belt.sendData();
		}
	}

	public BeltTileEntity getControllerTE() {
		if (!world.isBlockPresent(controller))
			return null;
		TileEntity te = world.getTileEntity(controller);
		if (te == null || !(te instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) te;
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

	public BlockPos getController() {
		return controller;
	}

	public boolean isController() {
		return controller.equals(pos);
	}

	public float getBeltMovementSpeed() {
		return getSpeed() / 480f;
	}

	public float getDirectionAwareBeltMovementSpeed() {
		int offset = getBeltFacing().getAxisDirection().getOffset();
		if (getBeltFacing().getAxis() == Axis.X)
			offset *= -1;
		return getBeltMovementSpeed() * offset;
	}

	public boolean hasPulley() {
		if (!AllBlocks.BELT.typeOf(getBlockState()))
			return false;
		return getBlockState().get(BeltBlock.PART) != Part.MIDDLE;
	}

	protected boolean isLastBelt() {
		if (getSpeed() == 0)
			return false;

		Direction direction = getBeltFacing();
		if (getBlockState().get(BeltBlock.SLOPE) == Slope.VERTICAL)
			return false;

		Part part = getBlockState().get(BeltBlock.PART);
		if (part == MIDDLE)
			return false;

		boolean movingPositively = (getSpeed() > 0 == (direction.getAxisDirection().getOffset() == 1))
				^ direction.getAxis() == Axis.X;
		return part == Part.START ^ movingPositively;
	}

	public Vec3i getMovementDirection(boolean firstHalf) {
		return this.getMovementDirection(firstHalf, false);
	}

	public Vec3i getBeltChainDirection() {
		return this.getMovementDirection(true, true);
	}

	protected Vec3i getMovementDirection(boolean firstHalf, boolean ignoreHalves) {
		if (getSpeed() == 0)
			return BlockPos.ZERO;

		final BlockState blockState = getBlockState();
		final Direction beltFacing = blockState.get(BlockStateProperties.HORIZONTAL_FACING);
		final Slope slope = blockState.get(BeltBlock.SLOPE);
		final Part part = blockState.get(BeltBlock.PART);
		final Axis axis = beltFacing.getAxis();

		Direction movementFacing = Direction.getFacingFromAxis(axis == Axis.X ? NEGATIVE : POSITIVE, axis);
		boolean notHorizontal = blockState.get(BeltBlock.SLOPE) != HORIZONTAL;
		if (getSpeed() < 0)
			movementFacing = movementFacing.getOpposite();
		Vec3i movement = movementFacing.getDirectionVec();

		boolean slopeBeforeHalf = (part == END) == (beltFacing.getAxisDirection() == POSITIVE);
		boolean onSlope = notHorizontal && (part == MIDDLE || slopeBeforeHalf == firstHalf || ignoreHalves);
		boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? UPWARD : DOWNWARD);

		if (!onSlope)
			return movement;

		return new Vec3i(movement.getX(), movingUp ? 1 : -1, movement.getZ());
	}

	public Direction getMovementFacing() {
		Axis axis = getBeltFacing().getAxis();
		return Direction.getFacingFromAxisDirection(axis,
				getBeltMovementSpeed() < 0 ^ axis == Axis.X ? NEGATIVE : POSITIVE);
	}

	protected Direction getBeltFacing() {
		return getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
	}

	public BeltInventory getInventory() {
		if (inventory == null)
			inventory = new BeltInventory(this);
		return inventory;
	}

	public boolean tryInsertingFromSide(Direction side, ItemStack stack, boolean simulate) {
		return tryInsertingFromSide(side, new TransportedItemStack(stack), simulate);
	}

	public boolean tryInsertingFromSide(Direction side, TransportedItemStack transportedStack, boolean simulate) {
		BeltTileEntity nextBeltController = getControllerTE();
		if (nextBeltController == null)
			return false;
		BeltInventory nextInventory = nextBeltController.getInventory();

		if (getSpeed() == 0)
			return false;
		if (!nextInventory.canInsertFrom(index, side))
			return false;
		if (simulate)
			return true;

		transportedStack.beltPosition = index + .5f - Math.signum(getSpeed()) / 16f;

		Direction movementFacing = getMovementFacing();
		if (!side.getAxis().isVertical()) {
			if (movementFacing != side) {
				transportedStack.sideOffset = side.getAxisDirection().getOffset() * .35f;
				if (side.getAxis() == Axis.X)
					transportedStack.sideOffset *= -1;
			} else
				transportedStack.beltPosition = getDirectionAwareBeltMovementSpeed() > 0 ? index : index + 1;
		}

		transportedStack.prevSideOffset = transportedStack.sideOffset;
		transportedStack.insertedAt = index;
		transportedStack.insertedFrom = side;
		transportedStack.prevBeltPosition = transportedStack.beltPosition;
		nextInventory.insert(transportedStack);
		nextBeltController.markDirty();
		nextBeltController.sendData();

		return true;
	}

}
