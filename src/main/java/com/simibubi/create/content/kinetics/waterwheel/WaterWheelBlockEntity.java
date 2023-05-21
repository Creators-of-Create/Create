package com.simibubi.create.content.kinetics.waterwheel;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WaterWheelBlockEntity extends GeneratingKineticBlockEntity {

	public static final Map<Axis, Set<BlockPos>> SMALL_OFFSETS = new EnumMap<>(Axis.class);
	public static final Map<Axis, Set<BlockPos>> LARGE_OFFSETS = new EnumMap<>(Axis.class);

	static {
		for (Axis axis : Iterate.axes) {
			HashSet<BlockPos> offsets = new HashSet<>();
			for (Direction d : Iterate.directions)
				if (d.getAxis() != axis)
					offsets.add(BlockPos.ZERO.relative(d));
			SMALL_OFFSETS.put(axis, offsets);

			offsets = new HashSet<>();
			for (Direction d : Iterate.directions) {
				if (d.getAxis() == axis)
					continue;
				BlockPos centralOffset = BlockPos.ZERO.relative(d, 2);
				offsets.add(centralOffset);
				for (Direction d2 : Iterate.directions) {
					if (d2.getAxis() == axis)
						continue;
					if (d2.getAxis() == d.getAxis())
						continue;
					offsets.add(centralOffset.relative(d2));
				}
			}
			LARGE_OFFSETS.put(axis, offsets);
		}
	}

	public int flowScore;
	public BlockState material;

	public WaterWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		material = Blocks.SPRUCE_PLANKS.defaultBlockState();
		setLazyTickRate(60);
	}

	protected int getSize() {
		return 1;
	}

	protected Set<BlockPos> getOffsetsToCheck() {
		return (getSize() == 1 ? SMALL_OFFSETS : LARGE_OFFSETS).get(getAxis());
	}

	public InteractionResult applyMaterialIfValid(ItemStack stack) {
		if (!(stack.getItem()instanceof BlockItem blockItem))
			return InteractionResult.PASS;
		BlockState material = blockItem.getBlock()
			.defaultBlockState();
		if (material == this.material)
			return InteractionResult.PASS;
		if (!material.is(BlockTags.PLANKS))
			return InteractionResult.PASS;
		if (level.isClientSide() && !isVirtual())
			return InteractionResult.SUCCESS;
		this.material = material;
		notifyUpdate();
		level.levelEvent(2001, worldPosition, Block.getId(material));
		return InteractionResult.SUCCESS;
	}

	protected Axis getAxis() {
		Axis axis = Axis.X;
		BlockState blockState = getBlockState();
		if (blockState.getBlock()instanceof IRotate irotate)
			axis = irotate.getRotationAxis(blockState);
		return axis;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();

		// Water can change flow direction without notifying neighbours
		determineAndApplyFlowScore();
	}

	public void determineAndApplyFlowScore() {
		Vec3 wheelPlane =
			Vec3.atLowerCornerOf(new Vec3i(1, 1, 1).subtract(Direction.get(AxisDirection.POSITIVE, getAxis())
				.getNormal()));

		int flowScore = 0;
		boolean lava = false;
		for (BlockPos blockPos : getOffsetsToCheck()) {
			BlockPos targetPos = blockPos.offset(worldPosition);
			Vec3 flowAtPos = getFlowVectorAtPosition(targetPos).multiply(wheelPlane);
			lava |= FluidHelper.isLava(level.getFluidState(targetPos)
				.getType());

			if (flowAtPos.lengthSqr() == 0)
				continue;

			flowAtPos = flowAtPos.normalize();
			Vec3 normal = Vec3.atLowerCornerOf(blockPos)
				.normalize();

			Vec3 positiveMotion = VecHelper.rotate(normal, 90, getAxis());
			double dot = flowAtPos.dot(positiveMotion);
			if (Math.abs(dot) > .5)
				flowScore += Math.signum(dot);
		}

		if (flowScore != 0 && !level.isClientSide())
			award(lava ? AllAdvancements.LAVA_WHEEL : AllAdvancements.WATER_WHEEL);

		setFlowScoreAndUpdate(flowScore);
	}

	public Vec3 getFlowVectorAtPosition(BlockPos pos) {
		FluidState fluid = level.getFluidState(pos);
		Vec3 vec = fluid.getFlow(level, pos);
		BlockState blockState = level.getBlockState(pos);
		if (blockState.getBlock() == Blocks.BUBBLE_COLUMN)
			vec = new Vec3(0, blockState.getValue(BubbleColumnBlock.DRAG_DOWN) ? -1 : 1, 0);
		return vec;
	}

	public void setFlowScoreAndUpdate(int score) {
		if (flowScore == score)
			return;
		flowScore = score;
		updateGeneratedRotation();
		setChanged();
	}

	private void redraw() {
		if (!isVirtual())
			requestModelDataUpdate();
		if (hasLevel()) {
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
			level.getChunkSource()
				.getLightEngine()
				.checkBlock(worldPosition);
		}
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		registerAwardables(behaviours, AllAdvancements.LAVA_WHEEL, AllAdvancements.WATER_WHEEL);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		flowScore = compound.getInt("FlowScore");

		BlockState prevMaterial = material;
		if (!compound.contains("Material"))
			return;

		material = NbtUtils.readBlockState(compound.getCompound("Material"));
		if (material.isAir())
			material = Blocks.SPRUCE_PLANKS.defaultBlockState();

		if (clientPacket && prevMaterial != material)
			redraw();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("FlowScore", flowScore);
		compound.put("Material", NbtUtils.writeBlockState(material));
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition).inflate(getSize());
	}

	@Override
	public float getGeneratedSpeed() {
		return Mth.clamp(flowScore, -1, 1) * 8 / getSize();
	}

}
