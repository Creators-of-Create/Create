package com.simibubi.create.content.kinetics.belt;

import static com.simibubi.create.content.kinetics.belt.BeltPart.MIDDLE;
import static com.simibubi.create.content.kinetics.belt.BeltSlope.HORIZONTAL;
import static net.minecraft.core.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.core.Direction.AxisDirection.POSITIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import dev.engine_room.flywheel.lib.box.Box;
import dev.engine_room.flywheel.lib.box.MutableBox;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import com.simibubi.create.content.kinetics.belt.transport.BeltMovementHandler;
import com.simibubi.create.content.kinetics.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import com.simibubi.create.content.kinetics.belt.transport.BeltTunnelInteractionHandler;
import com.simibubi.create.content.kinetics.belt.transport.ItemHandlerBeltSegment;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.tunnel.BrassTunnelBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.IItemHandler;

public class BeltBlockEntity extends KineticBlockEntity {

	public Map<Entity, TransportedEntityInfo> passengers;
	public Optional<DyeColor> color;
	public int beltLength;
	public int index;
	public Direction lastInsert;
	public CasingType casing;
	public boolean covered;

	protected BlockPos controller;
	protected BeltInventory inventory;
	protected LazyOptional<IItemHandler> itemHandler;

	public CompoundTag trackerUpdateTag;

	@OnlyIn(Dist.CLIENT)
	public BeltLighter lighter;

	public static enum CasingType {
		NONE, ANDESITE, BRASS;
	}

	public BeltBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		controller = BlockPos.ZERO;
		itemHandler = LazyOptional.empty();
		casing = CasingType.NONE;
		color = Optional.empty();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::canInsertFrom)
			.setInsertionHandler(this::tryInsertingFromSide).considerOccupiedWhen(this::isOccupied));
		behaviours.add(new TransportedItemStackHandlerBehaviour(this, this::applyToAllItems)
			.withStackPlacement(this::getWorldPositionOf));
	}

	@Override
	public void tick() {
		// Init belt
		if (beltLength == 0)
			BeltBlock.initBelt(level, worldPosition);

		super.tick();

		if (!AllBlocks.BELT.has(level.getBlockState(worldPosition)))
			return;

		initializeItemHandler();

		// Move Items
		if (!isController())
			return;

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			if (beltLength > 0 && lighter == null) {
				lighter = new BeltLighter();
			}
		});
		invalidateRenderBoundingBox();

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
	public AABB createRenderBoundingBox() {
		if (!isController())
			return super.createRenderBoundingBox();
		else
			return super.createRenderBoundingBox().inflate(beltLength + 1);
	}

	protected void initializeItemHandler() {
		if (level.isClientSide || itemHandler.isPresent())
			return;
		if (beltLength == 0 || controller == null)
			return;
		if (!level.isLoaded(controller))
			return;
		BlockEntity be = level.getBlockEntity(controller);
		if (be == null || !(be instanceof BeltBlockEntity))
			return;
		BeltInventory inventory = ((BeltBlockEntity) be).getInventory();
		if (inventory == null)
			return;
		IItemHandler handler = new ItemHandlerBeltSegment(inventory, index);
		itemHandler = LazyOptional.of(() -> handler);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!isItemHandlerCap(cap))
			return super.getCapability(cap, side);
		if (!isRemoved() && !itemHandler.isPresent())
			initializeItemHandler();
		return itemHandler.cast();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (isController())
			getInventory().ejectAll();
	}

	@Override
	public void invalidate() {
		super.invalidate();
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
		compound.putBoolean("Covered", covered);

		if (color.isPresent())
			NBTHelper.writeEnum(compound, "Dye", color.get());

		if (isController())
			compound.put("Inventory", getInventory().write());
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		int prevBeltLength = beltLength;
		super.read(compound, clientPacket);

		if (compound.getBoolean("IsController"))
			controller = worldPosition;

		color = compound.contains("Dye") ? Optional.of(NBTHelper.readEnum(compound, "Dye", DyeColor.class))
			: Optional.empty();

		if (!wasMoved) {
			if (!isController())
				controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));
			trackerUpdateTag = compound;
			index = compound.getInt("Index");
			beltLength = compound.getInt("Length");
			if (prevBeltLength != beltLength) {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
					if (lighter != null) {
						lighter.initializeLight();
					}
				});
			}
		}

		if (isController())
			getInventory().read(compound.getCompound("Inventory"));

		CasingType casingBefore = casing;
		boolean coverBefore = covered;
		casing = NBTHelper.readEnum(compound, "Casing", CasingType.class);
		covered = compound.getBoolean("Covered");

		if (!clientPacket)
			return;

		if (casingBefore == casing && coverBefore == covered)
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

	public boolean applyColor(DyeColor colorIn) {
		if (colorIn == null) {
			if (!color.isPresent())
				return false;
		} else if (color.isPresent() && color.get() == colorIn)
			return false;
		if (level.isClientSide())
			return true;

		for (BlockPos blockPos : BeltBlock.getBeltChain(level, getController())) {
			BeltBlockEntity belt = BeltHelper.getSegmentBE(level, blockPos);
			if (belt == null)
				continue;
			belt.color = Optional.ofNullable(colorIn);
			belt.setChanged();
			belt.sendData();
		}

		return true;
	}

	public BeltBlockEntity getControllerBE() {
		if (controller == null)
			return null;
		if (!level.isLoaded(controller))
			return null;
		BlockEntity be = level.getBlockEntity(controller);
		if (be == null || !(be instanceof BeltBlockEntity))
			return null;
		return (BeltBlockEntity) be;
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

	public BlockPos getController() {
		return controller == null ? worldPosition : controller;
	}

	public boolean isController() {
		return controller != null && worldPosition.getX() == controller.getX()
			&& worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
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
		return Direction.fromAxisAndDirection(axis, getBeltMovementSpeed() < 0 ^ axis == Axis.X ? NEGATIVE : POSITIVE);
	}

	protected Direction getBeltFacing() {
		return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	public BeltInventory getInventory() {
		if (!isController()) {
			BeltBlockEntity controllerBE = getControllerBE();
			if (controllerBE != null)
				return controllerBE.getInventory();
			return null;
		}
		if (inventory == null) {
			inventory = new BeltInventory(this);
		}
		return inventory;
	}

	private void applyToAllItems(float maxDistanceFromCenter,
		Function<TransportedItemStack, TransportedResult> processFunction) {
		BeltBlockEntity controller = getControllerBE();
		if (controller == null)
			return;
		BeltInventory inventory = controller.getInventory();
		if (inventory != null)
			inventory.applyToEachWithin(index + .5f, maxDistanceFromCenter, processFunction);
	}

	private Vec3 getWorldPositionOf(TransportedItemStack transported) {
		BeltBlockEntity controllerBE = getControllerBE();
		if (controllerBE == null)
			return Vec3.ZERO;
		return BeltHelper.getVectorForOffset(controllerBE, transported.beltPosition);
	}

	public void setCasingType(CasingType type) {
		if (casing == type)
			return;

		BlockState blockState = getBlockState();
		boolean shouldBlockHaveCasing = type != CasingType.NONE;

		if (level.isClientSide) {
			casing = type;
			level.setBlock(worldPosition, blockState.setValue(BeltBlock.CASING, shouldBlockHaveCasing), 0);
			requestModelDataUpdate();
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 16);
			return;
		}

		if (casing != CasingType.NONE)
			level.levelEvent(2001, worldPosition,
				Block.getId(casing == CasingType.ANDESITE ? AllBlocks.ANDESITE_CASING.getDefaultState()
					: AllBlocks.BRASS_CASING.getDefaultState()));
		if (blockState.getValue(BeltBlock.CASING) != shouldBlockHaveCasing)
			KineticBlockEntity.switchToBlockState(level, worldPosition,
				blockState.setValue(BeltBlock.CASING, shouldBlockHaveCasing));
		casing = type;
		setChanged();
		sendData();
	}

	private boolean canInsertFrom(Direction side) {
		if (getSpeed() == 0)
			return false;
		BlockState state = getBlockState();
		if (state.hasProperty(BeltBlock.SLOPE) && (state.getValue(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS
			|| state.getValue(BeltBlock.SLOPE) == BeltSlope.VERTICAL))
			return false;
		return getMovementFacing() != side.getOpposite();
	}

	private boolean isOccupied(Direction side) {
		BeltBlockEntity nextBeltController = getControllerBE();
		if (nextBeltController == null)
			return true;
		BeltInventory nextInventory = nextBeltController.getInventory();
		if (nextInventory == null)
			return true;
		if (getSpeed() == 0)
			return true;
		if (getMovementFacing() == side.getOpposite())
			return true;
		if (!nextInventory.canInsertAtFromSide(index, side))
			return true;
		return false;
	}

	private ItemStack tryInsertingFromSide(TransportedItemStack transportedStack, Direction side, boolean simulate) {
		BeltBlockEntity nextBeltController = getControllerBE();
		ItemStack inserted = transportedStack.stack;
		ItemStack empty = ItemStack.EMPTY;

		if (nextBeltController == null)
			return inserted;
		BeltInventory nextInventory = nextBeltController.getInventory();
		if (nextInventory == null)
			return inserted;

		BlockEntity teAbove = level.getBlockEntity(worldPosition.above());
		if (teAbove instanceof BrassTunnelBlockEntity) {
			BrassTunnelBlockEntity tunnelBE = (BrassTunnelBlockEntity) teAbove;
			if (tunnelBE.hasDistributionBehaviour()) {
				if (!tunnelBE.getStackToDistribute()
					.isEmpty())
					return inserted;
				if (!tunnelBE.testFlapFilter(side.getOpposite(), inserted))
					return inserted;
				if (!simulate) {
					BeltTunnelInteractionHandler.flapTunnel(nextInventory, index, side.getOpposite(), true);
					tunnelBE.setStackToDistribute(inserted, side.getOpposite());
				}
				return empty;
			}
		}

		if (isOccupied(side))
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

	@Override
	public ModelData getModelData() {
		return ModelData.builder()
			.with(BeltModel.CASING_PROPERTY, casing)
			.with(BeltModel.COVER_PROPERTY, covered)
			.build();
	}

	@Override
	protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
		return state.hasProperty(BeltBlock.SLOPE) && (state.getValue(BeltBlock.SLOPE) == BeltSlope.UPWARD
			|| state.getValue(BeltBlock.SLOPE) == BeltSlope.DOWNWARD);
	}

	@Override
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff,
		boolean connectedViaAxes, boolean connectedViaCogs) {
		if (target instanceof BeltBlockEntity && !connectedViaAxes)
			return getController().equals(((BeltBlockEntity) target).getController()) ? 1 : 0;
		return 0;
	}

	public void invalidateItemHandler() {
		itemHandler.invalidate();
	}

	public boolean shouldRenderNormally() {
		if (level == null)
			return isController();
		BlockState state = getBlockState();
		return state != null && state.hasProperty(BeltBlock.PART) && state.getValue(BeltBlock.PART) == BeltPart.START;
	}

	/**
	 * Hide this behavior in an inner class to avoid loading LightListener on servers.
	 */
	@OnlyIn(Dist.CLIENT)
	class BeltLighter {
		private byte[] light;

		public BeltLighter() {
			initializeLight();
		}

		/**
		 * Get the number of belt segments represented by the lighter.
		 * @return The number of segments.
		 */
		public int lightSegments() {
			return light == null ? 0 : light.length / 2;
		}

		/**
		 * Get the light value for a given segment.
		 * @param segment The segment to get the light value for.
		 * @return The light value.
		 */
		public int getPackedLight(int segment) {
			return light == null ? 0 : LightTexture.pack(light[segment * 2], light[segment * 2 + 1]);
		}

		public Box getVolume() {
			BlockPos endPos = BeltHelper.getPositionForOffset(BeltBlockEntity.this, beltLength - 1);
			var bb = MutableBox.from(worldPosition, endPos);
			bb.fixMinMax();
			return bb;
		}

		public void onLightUpdate(LightLayer type, SectionPos pos) {
			if (remove)
				return;
			if (level == null)
				return;

			initializeLight();
		}

		private void initializeLight() {
			light = new byte[beltLength * 2];

			Vec3i vec = getBeltFacing().getNormal();
			BeltSlope slope = getBlockState().getValue(BeltBlock.SLOPE);
			int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

			MutableBlockPos pos = new MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
			for (int i = 0; i < beltLength * 2; i += 2) {
				light[i] = (byte) level.getBrightness(LightLayer.BLOCK, pos);
				light[i + 1] = (byte) level.getBrightness(LightLayer.SKY, pos);
				pos.move(vec.getX(), verticality, vec.getZ());
			}
		}

		private void updateBlockLight() {
			Vec3i vec = getBeltFacing().getNormal();
			BeltSlope slope = getBlockState().getValue(BeltBlock.SLOPE);
			int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

			MutableBlockPos pos = new MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
			for (int i = 0; i < beltLength * 2; i += 2) {
				light[i] = (byte) level.getBrightness(LightLayer.BLOCK, pos);

				pos.move(vec.getX(), verticality, vec.getZ());
			}
		}

		private void updateSkyLight() {
			Vec3i vec = getBeltFacing().getNormal();
			BeltSlope slope = getBlockState().getValue(BeltBlock.SLOPE);
			int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;

			MutableBlockPos pos = new MutableBlockPos(controller.getX(), controller.getY(), controller.getZ());
			for (int i = 1; i < beltLength * 2; i += 2) {
				light[i] = (byte) level.getBrightness(LightLayer.SKY, pos);

				pos.move(vec.getX(), verticality, vec.getZ());
			}
		}
	}

	public void setCovered(boolean blockCoveringBelt) {
		if (blockCoveringBelt == covered)
			return;
		covered = blockCoveringBelt;
		notifyUpdate();
	}
}
