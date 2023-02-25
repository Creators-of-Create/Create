package com.simibubi.create.content.contraptions.components.waterwheel;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.content.contraptions.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class WaterWheelBlockEntity extends GeneratingKineticBlockEntity {

	public static Map<Axis, Set<BlockPos>> SMALL_OFFSETS = new IdentityHashMap<>();
	public static Map<Axis, Set<BlockPos>> LARGE_OFFSETS = new IdentityHashMap<>();

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
	public BlockState baseBlock; // <-- TODO use planks/corresponding logs in rendered instance

	public WaterWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		baseBlock = Blocks.SPRUCE_PLANKS.defaultBlockState();
	}

	protected int getSize() {
		return 1;
	}

	protected Set<BlockPos> getOffsetsToCheck() {
		return (getSize() == 1 ? SMALL_OFFSETS : LARGE_OFFSETS).get(getAxis());
	}
	
	public InteractionResult applyMaterialIfValid(ItemStack item) {
		if (!(item.getItem() instanceof BlockItem blockItem))
			return InteractionResult.PASS;
		BlockState material = blockItem.getBlock().defaultBlockState();
		if (material == this.baseBlock)
			return InteractionResult.PASS;
		if (!material.is(BlockTags.PLANKS))
			return InteractionResult.PASS;
		baseBlock = material;
		notifyUpdate();
		return InteractionResult.SUCCESS;
	}

	protected Axis getAxis() {
		Axis axis = Axis.X;
		BlockState blockState = getBlockState();
		if (blockState.getBlock()instanceof IRotate irotate)
			axis = irotate.getRotationAxis(blockState);
		return axis;
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

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		registerAwardables(behaviours, AllAdvancements.LAVA_WHEEL, AllAdvancements.WATER_WHEEL);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		flowScore = compound.getInt("FlowScore");

		BlockState prevMaterial = baseBlock;
		if (!compound.contains("Material"))
			return;

		JsonOps ops = JsonOps.INSTANCE;
		BlockState.CODEC.decode(ops, JsonParser.parseString(compound.getString("Material")))
			.result()
			.ifPresent(p -> baseBlock = p.getFirst());

		if (clientPacket && prevMaterial != baseBlock)
			redraw();
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

	public static final ModelProperty<BlockState> MATERIAL_PROPERTY = new ModelProperty<>();

	@Override
	public IModelData getModelData() {
		return new ModelDataMap.Builder().withInitial(MATERIAL_PROPERTY, baseBlock)
			.build();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("FlowScore", flowScore);
		JsonOps ops = JsonOps.INSTANCE;
		BlockState.CODEC.encode(baseBlock, ops, ops.empty())
			.result()
			.map(je -> je.toString())
			.ifPresent(s -> compound.putString("Material", s));
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return new AABB(worldPosition).inflate(getSize());
	}

	@Override
	public float getGeneratedSpeed() {
		return Mth.clamp(flowScore / getSize(), -2, 2) * 8 / getSize();
	}

}
