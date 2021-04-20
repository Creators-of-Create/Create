package com.simibubi.create.content.contraptions.relays.belt;

import static com.simibubi.create.content.contraptions.relays.belt.BeltPart.MIDDLE;
import static com.simibubi.create.content.contraptions.relays.belt.BeltSlope.HORIZONTAL;
import static net.minecraft.util.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.Direction.AxisDirection.POSITIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltInventory;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltMovementHandler;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.simibubi.create.content.contraptions.relays.belt.transport.BeltTunnelInteractionHandler;
import com.simibubi.create.content.contraptions.relays.belt.transport.ItemHandlerBeltSegment;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.block.belts.tunnel.BrassTunnelTileEntity;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.light.GridAlignedBB;
import com.simibubi.create.foundation.render.backend.light.LightUpdateListener;
import com.simibubi.create.foundation.render.backend.light.LightUpdater;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltTileEntity extends KineticTileEntity implements LightUpdateListener {

	public Map<Entity, TransportedEntityInfo> passengers;
	public Optional<DyeColor> color;
	public int beltLength;
	public int index;
	public Direction lastInsert;
	public CasingType casing;

	protected BlockPos controller;
	protected BeltInventory inventory;
	protected LazyOptional<IItemHandler> itemHandler;

	public CompoundNBT trackerUpdateTag;

	// client
	public byte[] light;

	public static enum CasingType {
		NONE, ANDESITE, BRASS;
	}

	public BeltTileEntity(TileEntityType<? extends BeltTileEntity> type) {
		super(type);
		controller = BlockPos.ZERO;
		itemHandler = LazyOptional.empty();
		casing = CasingType.NONE;
		color = Optional.empty();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::canInsertFrom)
			.setInsertionHandler(this::tryInsertingFromSide));
		behaviours.add(new TransportedItemStackHandlerBehaviour(this, this::applyToAllItems)
			.withStackPlacement(this::getWorldPositionOf));
	}

	@Override
	public void tick() {
		super.tick();

		// Init belt
		if (beltLength == 0)
			BeltBlock.initBelt(world, pos);
		if (!AllBlocks.BELT.has(world.getBlockState(pos)))
			return;

		initializeItemHandler();

		// Move Items
		if (!isController())
			return;

		if (light == null && world.isRemote) {
			initializeLight();
			LightUpdater.getInstance()
				.startListening(getBeltVolume(), this);
		}

		getInventory().tick();

		if (getSpeed() == 0)
			return;

		// Move Entities
		if (passengers == null)
			passengers = new HashMap<>();

		List<Entity> toRemove = new ArrayList<>();
		passengers.forEach((entity, info) -> {
			boolean canBeTransported = BeltMovementHandler.canBeTransported(entity);
			boolean leftTheBelt =
				info.getTicksSinceLastCollision() > ((getBlockState().get(BeltBlock.SLOPE) != HORIZONTAL) ? 3 : 1);
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
	public float calculateStressApplied() {
		if (!isController())
			return 0;
		return super.calculateStressApplied();
	}

	@Override
	public AxisAlignedBB makeRenderBoundingBox() {
		if (!isController())
			return super.makeRenderBoundingBox();
		else
			return super.makeRenderBoundingBox().grow(beltLength + 1);
	}

	protected void initializeItemHandler() {
		if (world.isRemote || itemHandler.isPresent())
			return;
		if (!world.isBlockPresent(controller))
			return;
		TileEntity te = world.getTileEntity(controller);
		if (te == null || !(te instanceof BeltTileEntity))
			return;
		BeltInventory inventory = ((BeltTileEntity) te).getInventory();
		if (inventory == null)
			return;
		IItemHandler handler = new ItemHandlerBeltSegment(inventory, index);
		itemHandler = LazyOptional.of(() -> handler);
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
	public void write(CompoundNBT compound, boolean clientPacket) {
		if (controller != null)
			compound.put("Controller", NBTUtil.writeBlockPos(controller));
		compound.putBoolean("IsController", isController());
		compound.putInt("Length", beltLength);
		compound.putInt("Index", index);
		NBTHelper.writeEnum(compound, "Casing", casing);

		if (color.isPresent())
			NBTHelper.writeEnum(compound, "Dye", color.get());

		if (isController())
			compound.put("Inventory", getInventory().write());
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);

		if (compound.getBoolean("IsController"))
			controller = pos;

		color = compound.contains("Dye") ? Optional.of(NBTHelper.readEnum(compound, "Dye", DyeColor.class))
			: Optional.empty();

		if (!wasMoved) {
			if (!isController())
				controller = NBTUtil.readBlockPos(compound.getCompound("Controller"));
			trackerUpdateTag = compound;
			beltLength = compound.getInt("Length");
			index = compound.getInt("Index");
		}

		if (isController())
			getInventory().read(compound.getCompound("Inventory"));

		CasingType casingBefore = casing;
		casing = NBTHelper.readEnum(compound, "Casing", CasingType.class);

		if (!clientPacket)
			return;

		if (casingBefore == casing)
			return;
		if (!isVirtual())
			requestModelDataUpdate();
		if (hasWorld())
			world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 16);
	}

	@Override
	public void clearKineticInformation() {
		super.clearKineticInformation();
		beltLength = 0;
		index = 0;
		controller = null;
		trackerUpdateTag = new CompoundNBT();
	}

	public void applyColor(DyeColor colorIn) {
		if (colorIn == null) {
			if (!color.isPresent())
				return;
		} else if (color.isPresent() && color.get() == colorIn)
			return;

		for (BlockPos blockPos : BeltBlock.getBeltChain(world, getController())) {
			BeltTileEntity belt = BeltHelper.getSegmentTE(world, blockPos);
			if (belt == null)
				continue;
			belt.color = Optional.ofNullable(colorIn);
			belt.markDirty();
			belt.sendData();
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FastRenderDispatcher.enqueueUpdate(belt));
		}
	}

	public BeltTileEntity getControllerTE() {
		if (controller == null)
			return null;
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
		return controller == null ? pos : controller;
	}

	public boolean isController() {
		return controller != null && pos.getX() == controller.getX() && pos.getY() == controller.getY()
			&& pos.getZ() == controller.getZ();
	}

	public float getBeltMovementSpeed() {
		return getSpeed() / 480f;
	}

	public float getDirectionAwareBeltMovementSpeed() {
		int offset = getBeltFacing().getAxisDirection()
			.getOffset();
		if (getBeltFacing().getAxis() == Axis.X)
			offset *= -1;
		return getBeltMovementSpeed() * offset;
	}

	public boolean hasPulley() {
		if (!AllBlocks.BELT.has(getBlockState()))
			return false;
		return getBlockState().get(BeltBlock.PART) != MIDDLE;
	}

	protected boolean isLastBelt() {
		if (getSpeed() == 0)
			return false;

		Direction direction = getBeltFacing();
		if (getBlockState().get(BeltBlock.SLOPE) == BeltSlope.VERTICAL)
			return false;

		BeltPart part = getBlockState().get(BeltBlock.PART);
		if (part == MIDDLE)
			return false;

		boolean movingPositively = (getSpeed() > 0 == (direction.getAxisDirection()
			.getOffset() == 1)) ^ direction.getAxis() == Axis.X;
		return part == BeltPart.START ^ movingPositively;
	}

	public Vector3i getMovementDirection(boolean firstHalf) {
		return this.getMovementDirection(firstHalf, false);
	}

	public Vector3i getBeltChainDirection() {
		return this.getMovementDirection(true, true);
	}

	protected Vector3i getMovementDirection(boolean firstHalf, boolean ignoreHalves) {
		if (getSpeed() == 0)
			return BlockPos.ZERO;

		final BlockState blockState = getBlockState();
		final Direction beltFacing = blockState.get(BlockStateProperties.HORIZONTAL_FACING);
		final BeltSlope slope = blockState.get(BeltBlock.SLOPE);
		final BeltPart part = blockState.get(BeltBlock.PART);
		final Axis axis = beltFacing.getAxis();

		Direction movementFacing = Direction.getFacingFromAxis(axis == Axis.X ? NEGATIVE : POSITIVE, axis);
		boolean notHorizontal = blockState.get(BeltBlock.SLOPE) != HORIZONTAL;
		if (getSpeed() < 0)
			movementFacing = movementFacing.getOpposite();
		Vector3i movement = movementFacing.getDirectionVec();

		boolean slopeBeforeHalf = (part == BeltPart.END) == (beltFacing.getAxisDirection() == POSITIVE);
		boolean onSlope = notHorizontal && (part == MIDDLE || slopeBeforeHalf == firstHalf || ignoreHalves);
		boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

		if (!onSlope)
			return movement;

		return new Vector3i(movement.getX(), movingUp ? 1 : -1, movement.getZ());
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
		if (!isController()) {
			BeltTileEntity controllerTE = getControllerTE();
			if (controllerTE != null)
				return controllerTE.getInventory();
			return null;
		}
		if (inventory == null) {
			inventory = new BeltInventory(this);
		}
		return inventory;
	}

	private void applyToAllItems(float maxDistanceFromCenter,
		Function<TransportedItemStack, TransportedResult> processFunction) {
		BeltTileEntity controller = getControllerTE();
		if (controller == null)
			return;
		BeltInventory inventory = controller.getInventory();
		if (inventory != null)
			inventory.applyToEachWithin(index + .5f, maxDistanceFromCenter, processFunction);
	}

	private Vector3d getWorldPositionOf(TransportedItemStack transported) {
		BeltTileEntity controllerTE = getControllerTE();
		if (controllerTE == null)
			return Vector3d.ZERO;
		return BeltHelper.getVectorForOffset(controllerTE, transported.beltPosition);
	}

	public void setCasingType(CasingType type) {
		if (casing == type)
			return;
		if (casing != CasingType.NONE)
			world.playEvent(2001, pos,
				Block.getStateId(casing == CasingType.ANDESITE ? AllBlocks.ANDESITE_CASING.getDefaultState()
					: AllBlocks.BRASS_CASING.getDefaultState()));
		casing = type;
		boolean shouldBlockHaveCasing = type != CasingType.NONE;
		BlockState blockState = getBlockState();
		if (blockState.get(BeltBlock.CASING) != shouldBlockHaveCasing)
			KineticTileEntity.switchToBlockState(world, pos, blockState.with(BeltBlock.CASING, shouldBlockHaveCasing));
		markDirty();
		sendData();
	}

	private boolean canInsertFrom(Direction side) {
		if (getSpeed() == 0)
			return false;
		BlockState state = getBlockState();
		if (state.contains(BeltBlock.SLOPE)
			&& (state.get(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS || state.get(BeltBlock.SLOPE) == BeltSlope.VERTICAL))
			return false;
		return getMovementFacing() != side.getOpposite();
	}

	private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
		BeltTileEntity nextBeltController = getControllerTE();
		ItemStack inserted = transportedStack.stack;
		ItemStack empty = ItemStack.EMPTY;

		if (nextBeltController == null)
			return inserted;
		BeltInventory nextInventory = nextBeltController.getInventory();
		if (nextInventory == null)
			return inserted;

		TileEntity teAbove = world.getTileEntity(pos.up());
		if (teAbove instanceof BrassTunnelTileEntity) {
			BrassTunnelTileEntity tunnelTE = (BrassTunnelTileEntity) teAbove;
			if (tunnelTE.hasDistributionBehaviour()) {
				if (!tunnelTE.getStackToDistribute()
					.isEmpty())
					return inserted;
				if (!tunnelTE.testFlapFilter(side.getOpposite(), inserted))
					return inserted;
				if (!simulate) {
					BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);
					tunnelTE.setStackToDistribute(inserted);
				}
				return empty;
			}
		}

		if (getSpeed() == 0)
			return inserted;
		if (getMovementFacing() == side.getOpposite())
			return inserted;
		if (!nextInventory.canInsertAtFromSide(index, side))
			return inserted;
		if (simulate)
			return empty;

		transportedStack = transportedStack.copy();
		transportedStack.beltPosition = index + .5f - Math.signum(getDirectionAwareBeltMovementSpeed()) / 16f;

		Direction movementFacing = getMovementFacing();
		if (!side.getAxis()
			.isVertical()) {
			if (movementFacing != side) {
				transportedStack.sideOffset = side.getAxisDirection()
					.getOffset() * .35f;
				if (side.getAxis() == Axis.X)
					transportedStack.sideOffset *= -1;
			} else
				transportedStack.beltPosition = getDirectionAwareBeltMovementSpeed() > 0 ? index : index + 1;
		}

		transportedStack.prevSideOffset = transportedStack.sideOffset;
		transportedStack.insertedAt = index;
		transportedStack.insertedFrom = side;
		transportedStack.prevBeltPosition = transportedStack.beltPosition;

		BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);

		nextInventory.addItem(transportedStack);
		nextBeltController.markDirty();
		nextBeltController.sendData();
		return empty;
	}

	public static ModelProperty<CasingType> CASING_PROPERTY = new ModelProperty<>();

	@Override
	public IModelData getModelData() {
		return new ModelDataMap.Builder().withInitial(CASING_PROPERTY, casing)
			.build();
	}

	@Override
	protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
		return state.contains(BeltBlock.SLOPE)
			&& (state.get(BeltBlock.SLOPE) == BeltSlope.UPWARD || state.get(BeltBlock.SLOPE) == BeltSlope.DOWNWARD);
	}

	@Override
	public float propagateRotationTo(KineticTileEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		if (target instanceof BeltTileEntity && !connectedViaAxes)
			return getController().equals(((BeltTileEntity) target).getController()) ? 1 : 0;
		return 0;
	}

	@Override
	public boolean shouldRenderAsTE() {
		if (world == null)
			return isController();
		BlockState state = getBlockState();
		return state != null && state.contains(BeltBlock.PART) && state.get(BeltBlock.PART) == BeltPart.START;
	}

	@Override
	public boolean onLightUpdate(IBlockDisplayReader world, LightType type, GridAlignedBB changed) {
		if (this.removed) {
			return true;
		}

		GridAlignedBB beltVolume = getBeltVolume();

		if (beltVolume.intersects(changed)) {
			if (type == LightType.BLOCK)
				updateBlockLight();

			if (type == LightType.SKY)
				updateSkyLight();
		}

		return false;
	}

	private GridAlignedBB getBeltVolume() {
		BlockPos endPos = BeltHelper.getPositionForOffset(this, beltLength - 1);

		GridAlignedBB bb = GridAlignedBB.from(pos, endPos);
		bb.fixMinMax();
		return bb;
	}

	private void initializeLight() {
		if (beltLength > 0) {
			light = new byte[beltLength * 2];

			Vector3i vec = getBeltFacing().getDirectionVec();
			BeltSlope slope = getBlockState().get(BeltBlock.SLOPE);
			int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

			BlockPos.Mutable pos = new BlockPos.Mutable(controller.getX(), controller.getY(), controller.getZ());
			for (int i = 0; i < beltLength * 2; i += 2) {
				light[i] = (byte) world.getLightLevel(LightType.BLOCK, pos);
				light[i + 1] = (byte) world.getLightLevel(LightType.SKY, pos);
				pos.move(vec.getX(), verticality, vec.getZ());
			}
		}
	}

	private void updateBlockLight() {
		Vector3i vec = getBeltFacing().getDirectionVec();
		BeltSlope slope = getBlockState().get(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

		BlockPos.Mutable pos = new BlockPos.Mutable(controller.getX(), controller.getY(), controller.getZ());
		for (int i = 0; i < beltLength * 2; i += 2) {
			light[i] = (byte) world.getLightLevel(LightType.BLOCK, pos);

			pos.move(vec.getX(), verticality, vec.getZ());
		}
	}

	private void updateSkyLight() {
		Vector3i vec = getBeltFacing().getDirectionVec();
		BeltSlope slope = getBlockState().get(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

		BlockPos.Mutable pos = new BlockPos.Mutable(controller.getX(), controller.getY(), controller.getZ());
		for (int i = 1; i < beltLength * 2; i += 2) {
			light[i] = (byte) world.getLightLevel(LightType.SKY, pos);

			pos.move(vec.getX(), verticality, vec.getZ());
		}
	}
}
