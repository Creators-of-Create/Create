package com.simibubi.create.content.contraptions.relays.belt;

import static com.simibubi.create.content.contraptions.relays.belt.BeltPart.MIDDLE;
import static com.simibubi.create.content.contraptions.relays.belt.BeltSlope.HORIZONTAL;
import static net.minecraft.core.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.core.Direction.AxisDirection.POSITIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.light.GridAlignedBB;
import com.jozufozu.flywheel.light.ILightUpdateListener;
import com.jozufozu.flywheel.light.ImmutableBox;
import com.jozufozu.flywheel.light.LightProvider;
import com.jozufozu.flywheel.light.ListenerStatus;
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
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.NBTHelper;

import com.simibubi.create.foundation.utility.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BeltTileEntity extends KineticTileEntity implements ILightUpdateListener {

	public Map<Entity, TransportedEntityInfo> passengers;
	public Optional<DyeColor> color;
	public int beltLength;
	public int index;
	public Direction lastInsert;
	public CasingType casing;

	protected BlockPos controller;
	protected BeltInventory inventory;
	protected LazyOptional<IItemHandler> itemHandler;

	public CompoundTag trackerUpdateTag;

	// client
	public byte[] light;

	public static enum CasingType {
		NONE, ANDESITE, BRASS;
	}

	public BeltTileEntity(BlockEntityType<? extends BeltTileEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		controller = BlockPos.ZERO;
		itemHandler = LazyOptional.empty();
		casing = CasingType.NONE;
		color = Optional.empty();
	}

	public BeltTileEntity(BlockPos pos, BlockState state, BlockEntityType<BeltTileEntity> type) {
		this(type, pos, state);
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
	public void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		super.tick(level, pos, state, blockEntity);

		// Init belt
		if (beltLength == 0)
			BeltBlock.initBelt(level, worldPosition);
		if (!AllBlocks.BELT.has(level.getBlockState(worldPosition)))
			return;

		initializeItemHandler();

		// Move Items
		if (!isController())
			return;

		if (light == null && level.isClientSide) {
			initializeLight();
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
				info.getTicksSinceLastCollision() > ((getBlockState().getValue(BeltBlock.SLOPE) != HORIZONTAL) ? 3 : 1);
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
	public AABB makeRenderBoundingBox() {
		if (!isController())
			return super.makeRenderBoundingBox();
		else
			return super.makeRenderBoundingBox().inflate(beltLength + 1);
	}

	protected void initializeItemHandler() {
		if (level.isClientSide || itemHandler.isPresent())
			return;
		if (!level.isLoaded(controller))
			return;
		BlockEntity te = level.getBlockEntity(controller);
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
	public void setRemoved() {
		super.setRemoved();
		itemHandler.invalidate();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		if (controller != null)
			compound.put("Controller", NbtUtils.writeBlockPos(controller));
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
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);

		if (compound.getBoolean("IsController"))
			controller = worldPosition;

		color = compound.contains("Dye") ? Optional.of(NBTHelper.readEnum(compound, "Dye", DyeColor.class))
			: Optional.empty();

		if (!wasMoved) {
			if (!isController())
				controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));
			trackerUpdateTag = compound;
			index = compound.getInt("Index");
			int length = compound.getInt("Length");
			if (beltLength != length) {
				beltLength = length;
				if (level != null)
					initializeLight();
				else
					light = null;
			}
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
		if (hasLevel())
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
	}

	@Override
	public void clearKineticInformation() {
		super.clearKineticInformation();
		beltLength = 0;
		index = 0;
		controller = null;
		trackerUpdateTag = new CompoundTag();
	}

	public void applyColor(DyeColor colorIn) {
		if (colorIn == null) {
			if (!color.isPresent())
				return;
		} else if (color.isPresent() && color.get() == colorIn)
			return;

		for (BlockPos blockPos : BeltBlock.getBeltChain(level, getController())) {
			BeltTileEntity belt = WorldHelper.getTileAt(level, blockPos);
			if (belt == null)
				continue;
			belt.color = Optional.ofNullable(colorIn);
			belt.setChanged();
			belt.sendData();
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> InstancedRenderDispatcher.enqueueUpdate(belt));
		}
	}

	public BeltTileEntity getControllerTE() {
		if (controller == null)
			return null;
		if (!level.isLoaded(controller))
			return null;
		BlockEntity te = level.getBlockEntity(controller);
		if (te == null || !(te instanceof BeltTileEntity))
			return null;
		return (BeltTileEntity) te;
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

	public BlockPos getController() {
		return controller == null ? worldPosition : controller;
	}

	public boolean isController() {
		return controller != null && worldPosition.getX() == controller.getX() && worldPosition.getY() == controller.getY()
			&& worldPosition.getZ() == controller.getZ();
	}

	public float getBeltMovementSpeed() {
		return getSpeed() / 480f;
	}

	public float getDirectionAwareBeltMovementSpeed() {
		int offset = getBeltFacing().getAxisDirection()
			.getStep();
		if (getBeltFacing().getAxis() == Axis.X)
			offset *= -1;
		return getBeltMovementSpeed() * offset;
	}

	public boolean hasPulley() {
		if (!AllBlocks.BELT.has(getBlockState()))
			return false;
		return getBlockState().getValue(BeltBlock.PART) != MIDDLE;
	}

	protected boolean isLastBelt() {
		if (getSpeed() == 0)
			return false;

		Direction direction = getBeltFacing();
		if (getBlockState().getValue(BeltBlock.SLOPE) == BeltSlope.VERTICAL)
			return false;

		BeltPart part = getBlockState().getValue(BeltBlock.PART);
		if (part == MIDDLE)
			return false;

		boolean movingPositively = (getSpeed() > 0 == (direction.getAxisDirection()
			.getStep() == 1)) ^ direction.getAxis() == Axis.X;
		return part == BeltPart.START ^ movingPositively;
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
		final Direction beltFacing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		final BeltSlope slope = blockState.getValue(BeltBlock.SLOPE);
		final BeltPart part = blockState.getValue(BeltBlock.PART);
		final Axis axis = beltFacing.getAxis();

		Direction movementFacing = Direction.get(axis == Axis.X ? NEGATIVE : POSITIVE, axis);
		boolean notHorizontal = blockState.getValue(BeltBlock.SLOPE) != HORIZONTAL;
		if (getSpeed() < 0)
			movementFacing = movementFacing.getOpposite();
		Vec3i movement = movementFacing.getNormal();

		boolean slopeBeforeHalf = (part == BeltPart.END) == (beltFacing.getAxisDirection() == POSITIVE);
		boolean onSlope = notHorizontal && (part == MIDDLE || slopeBeforeHalf == firstHalf || ignoreHalves);
		boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

		if (!onSlope)
			return movement;

		return new Vec3i(movement.getX(), movingUp ? 1 : -1, movement.getZ());
	}

	public Direction getMovementFacing() {
		Axis axis = getBeltFacing().getAxis();
		return Direction.fromAxisAndDirection(axis,
			getBeltMovementSpeed() < 0 ^ axis == Axis.X ? NEGATIVE : POSITIVE);
	}

	protected Direction getBeltFacing() {
		return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
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

	private Vec3 getWorldPositionOf(TransportedItemStack transported) {
		BeltTileEntity controllerTE = getControllerTE();
		if (controllerTE == null)
			return Vec3.ZERO;
		return BeltHelper.getVectorForOffset(controllerTE, transported.beltPosition);
	}

	public void setCasingType(CasingType type) {
		if (casing == type)
			return;
		if (casing != CasingType.NONE)
			level.levelEvent(2001, worldPosition,
				Block.getId(casing == CasingType.ANDESITE ? AllBlocks.ANDESITE_CASING.getDefaultState()
					: AllBlocks.BRASS_CASING.getDefaultState()));
		casing = type;
		boolean shouldBlockHaveCasing = type != CasingType.NONE;
		BlockState blockState = getBlockState();
		if (blockState.getValue(BeltBlock.CASING) != shouldBlockHaveCasing)
			KineticTileEntity.switchToBlockState(level, worldPosition, blockState.setValue(BeltBlock.CASING, shouldBlockHaveCasing));
		setChanged();
		sendData();
	}

	private boolean canInsertFrom(Direction side) {
		if (getSpeed() == 0)
			return false;
		BlockState state = getBlockState();
		if (state.hasProperty(BeltBlock.SLOPE)
			&& (state.getValue(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS || state.getValue(BeltBlock.SLOPE) == BeltSlope.VERTICAL))
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

		BlockEntity teAbove = level.getBlockEntity(worldPosition.above());
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
					.getStep() * .35f;
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
		nextBeltController.setChanged();
		nextBeltController.sendData();
		return empty;
	}

	public static final ModelProperty<CasingType> CASING_PROPERTY = new ModelProperty<>();

	@Override
	public IModelData getModelData() {
		return new ModelDataMap.Builder().withInitial(CASING_PROPERTY, casing)
			.build();
	}

	@Override
	protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
		return state.hasProperty(BeltBlock.SLOPE)
			&& (state.getValue(BeltBlock.SLOPE) == BeltSlope.UPWARD || state.getValue(BeltBlock.SLOPE) == BeltSlope.DOWNWARD);
	}

	@Override
	public float propagateRotationTo(KineticTileEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		if (target instanceof BeltTileEntity && !connectedViaAxes)
			return getController().equals(((BeltTileEntity) target).getController()) ? 1 : 0;
		return 0;
	}

	public void invalidateItemHandler() {
		itemHandler.invalidate();
	}

	@Override
	public boolean shouldRenderNormally() {
		if (level == null)
			return isController();
		BlockState state = getBlockState();
		return state != null && state.hasProperty(BeltBlock.PART) && state.getValue(BeltBlock.PART) == BeltPart.START;
	}

	@Override
	public ImmutableBox getVolume() {
		return getBeltVolume();
	}

	@Override
	public ListenerStatus status() {
		return ListenerStatus.OKAY;
	}

	@Override
	public void onLightUpdate(LightProvider world, LightLayer type, ImmutableBox changedVolume) {
		if (!this.remove) {
			GridAlignedBB vol = changedVolume.copy();
			if (vol.intersects(this.getVolume())) {
				vol.intersectAssign(this.getVolume());
				if (type == LightLayer.BLOCK) {
					updateBlockLight();
				} else if (type == LightLayer.SKY) {
					updateSkyLight();
				}

			}
		}
	}

	private GridAlignedBB getBeltVolume() {
		BlockPos endPos = BeltHelper.getPositionForOffset(this, beltLength - 1);

		GridAlignedBB bb = GridAlignedBB.from(worldPosition, endPos);
		bb.fixMinMax();
		return bb;
	}

	private void initializeLight() {
		if (beltLength > 0) {
			light = new byte[beltLength * 2];

			Vec3i vec = getBeltFacing().getNormal();
			BeltSlope slope = getBlockState().getValue(BeltBlock.SLOPE);
			int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
			for (int i = 0; i < beltLength * 2; i += 2) {
				light[i] = (byte) level.getBrightness(LightLayer.BLOCK, pos);
				light[i + 1] = (byte) level.getBrightness(LightLayer.SKY, pos);
				pos.move(vec.getX(), verticality, vec.getZ());
			}
		}
	}

	private void updateBlockLight() {
		Vec3i vec = getBeltFacing().getNormal();
		BeltSlope slope = getBlockState().getValue(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
		for (int i = 0; i < beltLength * 2; i += 2) {
			light[i] = (byte) level.getBrightness(LightLayer.BLOCK, pos);

			pos.move(vec.getX(), verticality, vec.getZ());
		}
	}

	private void updateSkyLight() {
		Vec3i vec = getBeltFacing().getNormal();
		BeltSlope slope = getBlockState().getValue(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
		for (int i = 1; i < beltLength * 2; i += 2) {
			light[i] = (byte) level.getBrightness(LightLayer.SKY, pos);

			pos.move(vec.getX(), verticality, vec.getZ());
		}
	}
}
