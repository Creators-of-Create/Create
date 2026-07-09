package com.simibubi.create.content.decoration;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

public class RoofBlockCTBehaviour extends ConnectedTextureBehaviour.Base {

	private CTSpriteShiftEntry shift;

	public RoofBlockCTBehaviour(CTSpriteShiftEntry shift) {
		this.shift = shift;
	}

	@Override
	public @Nullable CTSpriteShiftEntry getShift(BlockState state, Direction direction,
		@Nullable TextureAtlasSprite sprite) {
		if (direction == Direction.UP)
			return shift;
		return null;
	}

	@Override
	public boolean buildContextForOccludedDirections() {
		return true;
	}

	@Override
	public CTContext buildContext(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face,
		ContextRequirement requirement) {

		if (isUprightStair(state))
			return getStairMapping(state);

		return super.buildContext(reader, pos, state, face, requirement);
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {

		if (connects(reader, pos, state, other)
			|| connectsHigh(reader, pos, state, other, reader.getBlockState(otherPos.above())))
			return true;
		if (primaryOffset != null && secondaryOffset != null)
			return false;

		for (boolean p : Iterate.trueAndFalse) {
			Direction offset = p ? primaryOffset : secondaryOffset;
			if (offset == null)
				continue;
			if (offset.getAxis()
				.isVertical())
				continue;

			if (connectsHigh(reader, pos, state, reader.getBlockState(pos.relative(offset.getClockWise())),
				reader.getBlockState(pos.relative(offset.getClockWise())
					.above()))
				|| connectsHigh(reader, pos, state, reader.getBlockState(pos.relative(offset.getCounterClockWise())),
					reader.getBlockState(pos.relative(offset.getCounterClockWise())
						.above())))
				return true;
		}

		return false;
	}

	public boolean isUprightStair(BlockState state) {
		return state.hasProperty(StairBlock.SHAPE) && state.getOptionalValue(StairBlock.HALF)
			.orElse(Half.TOP) == Half.BOTTOM;
	}

	public CTContext getStairMapping(BlockState state) {
		CTContext context = new CTContext();
		StairsShape shape = state.getValue(StairBlock.SHAPE);
		Direction facing = state.getValue(StairBlock.FACING);

		if (shape == StairsShape.OUTER_LEFT)
			facing = facing.getCounterClockWise();
		if (shape == StairsShape.INNER_LEFT)
			facing = facing.getCounterClockWise();

		int type = shape == StairsShape.STRAIGHT ? 0
			: (shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT) ? 1 : 2;
		int rot = facing.get2DDataValue();
		context.up = type >= 2;
		context.right = type % 2 == 1;
		context.left = rot >= 2;
		context.down = rot % 2 == 1;
		return context;
	}

	protected boolean connects(BlockAndTintGetter reader, BlockPos pos, BlockState state, BlockState other) {
		double top = state.getCollisionShape(reader, pos)
			.max(Axis.Y);
		double topOther = other.getSoundType() != SoundType.COPPER ? 0
			: other.getCollisionShape(reader, pos)
				.max(Axis.Y);
		return Mth.equal(top, topOther);
	}

	protected boolean connectsHigh(BlockAndTintGetter reader, BlockPos pos, BlockState state, BlockState other,
		BlockState aboveOther) {
		if (state.getBlock() instanceof SlabBlock && other.getBlock() instanceof SlabBlock)
			if (state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM && other.getValue(SlabBlock.TYPE) != SlabType.BOTTOM)
				return true;

		if (state.getBlock() instanceof SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
			double top = state.getCollisionShape(reader, pos)
				.max(Axis.Y);
			double topOther = other.getCollisionShape(reader, pos)
				.max(Axis.Y);
			return !Mth.equal(top, topOther) && topOther > top;
		}

		double topAboveOther = aboveOther.getCollisionShape(reader, pos)
			.max(Axis.Y);
		return topAboveOther > 0;
	}

	@Override
	public @Nullable CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
		return isUprightStair(state) ? AllCTTypes.ROOF_STAIR : AllCTTypes.ROOF;
	}

}
