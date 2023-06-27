package com.simibubi.create.foundation.block.connected;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ConnectedTextureBehaviour {

	@Nullable
	public abstract CTSpriteShiftEntry getShift(BlockState state, Direction direction,
		@NotNull TextureAtlasSprite sprite);

	// TODO: allow more than one data type per state/face?
	@Nullable
	public abstract CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction);

	public boolean buildContextForOccludedDirections() {
		return false;
	}

	protected boolean isBeingBlocked(BlockState state, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		BlockPos blockingPos = otherPos.relative(face);
		BlockState blockState = reader.getBlockState(pos);
		BlockState blockingState = reader.getBlockState(blockingPos);

		if (!Block.isFaceFull(blockingState.getShape(reader, blockingPos), face.getOpposite()))
			return false;
		if (face.getAxis()
			.choose(pos.getX(), pos.getY(), pos.getZ()) != face.getAxis()
				.choose(otherPos.getX(), otherPos.getY(), otherPos.getZ()))
			return false;

		return connectsTo(state,
			getCTBlockState(reader, blockState, face.getOpposite(), pos.relative(face), blockingPos), reader, pos,
			blockingPos, face);
	}

	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
		return connectsTo(state, other, reader, pos, otherPos, face);
	}

	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face) {
		return !isBeingBlocked(state, reader, pos, otherPos, face) && state.getBlock() == other.getBlock();
	}

	private boolean testConnection(BlockAndTintGetter reader, BlockPos currentPos, BlockState connectiveCurrentState,
		Direction textureSide, final Direction horizontal, final Direction vertical, int sh, int sv) {
		BlockState trueCurrentState = reader.getBlockState(currentPos);
		BlockPos targetPos = currentPos.relative(horizontal, sh)
			.relative(vertical, sv);
		BlockState connectiveTargetState =
			getCTBlockState(reader, trueCurrentState, textureSide, currentPos, targetPos);
		return connectsTo(connectiveCurrentState, connectiveTargetState, reader, currentPos, targetPos, textureSide,
			sh == 0 ? null : sh == -1 ? horizontal.getOpposite() : horizontal,
			sv == 0 ? null : sv == -1 ? vertical.getOpposite() : vertical);
	}

	public BlockState getCTBlockState(BlockAndTintGetter reader, BlockState reference, Direction face, BlockPos fromPos,
		BlockPos toPos) {
		BlockState blockState = reader.getBlockState(toPos);
		return blockState.getAppearance(reader, toPos, face, reference, fromPos);
	}

	protected boolean reverseUVs(BlockState state, Direction face) {
		return false;
	}

	protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
		return reverseUVs(state, face);
	}

	protected boolean reverseUVsVertically(BlockState state, Direction face) {
		return reverseUVs(state, face);
	}

	protected Direction getUpDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
		Axis axis = face.getAxis();
		return axis.isHorizontal() ? Direction.UP : Direction.NORTH;
	}

	protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
		Axis axis = face.getAxis();
		return axis == Axis.X ? Direction.SOUTH : Direction.WEST;
	}

	public CTContext buildContext(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face,
		ContextRequirement requirement) {
		boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;
		Direction h = getRightDirection(reader, pos, state, face);
		Direction v = getUpDirection(reader, pos, state, face);
		h = positive ? h.getOpposite() : h;
		if (face == Direction.DOWN) {
			v = v.getOpposite();
			h = h.getOpposite();
		}

		final Direction horizontal = h;
		final Direction vertical = v;

		boolean flipH = reverseUVsHorizontally(state, face);
		boolean flipV = reverseUVsVertically(state, face);
		int sh = flipH ? -1 : 1;
		int sv = flipV ? -1 : 1;

		CTContext context = new CTContext();

		if (requirement.up) {
			context.up = testConnection(reader, pos, state, face, horizontal, vertical, 0, sv);
		}
		if (requirement.down) {
			context.down = testConnection(reader, pos, state, face, horizontal, vertical, 0, -sv);
		}
		if (requirement.left) {
			context.left = testConnection(reader, pos, state, face, horizontal, vertical, -sh, 0);
		}
		if (requirement.right) {
			context.right = testConnection(reader, pos, state, face, horizontal, vertical, sh, 0);
		}

		if (requirement.topLeft) {
			context.topLeft =
				context.up && context.left && testConnection(reader, pos, state, face, horizontal, vertical, -sh, sv);
		}
		if (requirement.topRight) {
			context.topRight =
				context.up && context.right && testConnection(reader, pos, state, face, horizontal, vertical, sh, sv);
		}
		if (requirement.bottomLeft) {
			context.bottomLeft = context.down && context.left
				&& testConnection(reader, pos, state, face, horizontal, vertical, -sh, -sv);
		}
		if (requirement.bottomRight) {
			context.bottomRight = context.down && context.right
				&& testConnection(reader, pos, state, face, horizontal, vertical, sh, -sv);
		}

		return context;
	}

	public static class CTContext {
		public static final CTContext EMPTY = new CTContext();

		public boolean up, down, left, right;
		public boolean topLeft, topRight, bottomLeft, bottomRight;
	}

	public static class ContextRequirement {
		public final boolean up, down, left, right;
		public final boolean topLeft, topRight, bottomLeft, bottomRight;

		public ContextRequirement(boolean up, boolean down, boolean left, boolean right, boolean topLeft,
			boolean topRight, boolean bottomLeft, boolean bottomRight) {
			this.up = up;
			this.down = down;
			this.left = left;
			this.right = right;
			this.topLeft = topLeft;
			this.topRight = topRight;
			this.bottomLeft = bottomLeft;
			this.bottomRight = bottomRight;
		}

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {
			private boolean up, down, left, right;
			private boolean topLeft, topRight, bottomLeft, bottomRight;

			public Builder up() {
				up = true;
				return this;
			}

			public Builder down() {
				down = true;
				return this;
			}

			public Builder left() {
				left = true;
				return this;
			}

			public Builder right() {
				right = true;
				return this;
			}

			public Builder topLeft() {
				topLeft = true;
				return this;
			}

			public Builder topRight() {
				topRight = true;
				return this;
			}

			public Builder bottomLeft() {
				bottomLeft = true;
				return this;
			}

			public Builder bottomRight() {
				bottomRight = true;
				return this;
			}

			public Builder horizontal() {
				left();
				right();
				return this;
			}

			public Builder vertical() {
				up();
				down();
				return this;
			}

			public Builder axisAligned() {
				horizontal();
				vertical();
				return this;
			}

			public Builder corners() {
				topLeft();
				topRight();
				bottomLeft();
				bottomRight();
				return this;
			}

			public Builder all() {
				axisAligned();
				corners();
				return this;
			}

			public ContextRequirement build() {
				return new ContextRequirement(up, down, left, right, topLeft, topRight, bottomLeft, bottomRight);
			}
		}
	}

	public static abstract class Base extends ConnectedTextureBehaviour {
		@Override
		@Nullable
		public abstract CTSpriteShiftEntry getShift(BlockState state, Direction direction,
			@Nullable TextureAtlasSprite sprite);

		@Override
		@Nullable
		public CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
			CTSpriteShiftEntry shift = getShift(state, direction, null);
			if (shift == null) {
				return null;
			}
			return shift.getType();
		}
	}

}
