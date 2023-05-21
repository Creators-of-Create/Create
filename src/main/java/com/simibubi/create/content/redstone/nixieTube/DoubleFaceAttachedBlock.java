package com.simibubi.create.content.redstone.nixieTube;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;

public class DoubleFaceAttachedBlock extends HorizontalDirectionalBlock {

	public enum DoubleAttachFace implements StringRepresentable {
		FLOOR("floor"), WALL("wall"), WALL_REVERSED("wall_reversed"), CEILING("ceiling");

		private final String name;

		private DoubleAttachFace(String p_61311_) {
			this.name = p_61311_;
		}

		public String getSerializedName() {
			return this.name;
		}

		public int xRot() {
			return this == FLOOR ? 0 : this == CEILING ? 180 : 90;
		}
	}

	public static final EnumProperty<DoubleAttachFace> FACE =
		EnumProperty.create("double_face", DoubleAttachFace.class);

	public DoubleFaceAttachedBlock(BlockBehaviour.Properties p_53182_) {
		super(p_53182_);
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		for (Direction direction : pContext.getNearestLookingDirections()) {
			BlockState blockstate;
			if (direction.getAxis() == Direction.Axis.Y) {
				blockstate = this.defaultBlockState()
					.setValue(FACE, direction == Direction.UP ? DoubleAttachFace.CEILING : DoubleAttachFace.FLOOR)
					.setValue(FACING, pContext.getHorizontalDirection());
			} else {
				Vec3 n = Vec3.atLowerCornerOf(direction.getClockWise()
					.getNormal());
				DoubleAttachFace face = DoubleAttachFace.WALL;
				if (pContext.getPlayer() != null) {
					Vec3 lookAngle = pContext.getPlayer()
						.getLookAngle();
					if (lookAngle.dot(n) < 0)
						face = DoubleAttachFace.WALL_REVERSED;
				}
				blockstate = this.defaultBlockState()
					.setValue(FACE, face)
					.setValue(FACING, direction.getOpposite());
			}

			if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
				return blockstate;
			}
		}

		return null;
	}

	protected static Direction getConnectedDirection(BlockState pState) {
		switch ((DoubleAttachFace) pState.getValue(FACE)) {
		case CEILING:
			return Direction.DOWN;
		case FLOOR:
			return Direction.UP;
		default:
			return pState.getValue(FACING);
		}
	}
}
