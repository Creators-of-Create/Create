package com.simibubi.create.content.contraptions.components.structureMovement;

import static net.minecraft.block.HorizontalFaceBlock.FACE;
import staticnet.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlockateProperties.AXIS;
import static net.minecraft.state.properties.BlockStateProperties.FACING;
import staticnet.minecraft.world.level.block.state.properties.BlockStatePropertiess.HORIZONTAL_FACING;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.foundation.utility.DirectionHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class StructureTransform {

	// Assuming structures cannot be rotated around multiple axes at once
	Rotation rotation;
	int angle;
	Axis rotationAxis;
	BlockPos offset;
	Mirror mirror;

	private StructureTransform(BlockPos offset, int angle, Axis axis, Rotation rotation, Mirror mirror) {
		this.offset = offset;
		this.angle = angle;
		rotationAxis = axis;
		this.rotation = rotation;
		this.mirror = mirror;
	}

	public StructureTransform(BlockPos offset, Axis axis, Rotation rotation, Mirror mirror) {
		this(offset, rotation == Rotation.NONE ? 0 : (4 - rotation.ordinal())*90, axis, rotation, mirror);
	}

	public StructureTransform(BlockPos offset, float xRotation, float yRotation, float zRotation) {
		this.offset = offset;
		if (xRotation != 0) {
			rotationAxis = Axis.X;
			angle = Math.round(xRotation / 90) * 90;
		}
		if (yRotation != 0) {
			rotationAxis = Axis.Y;
			angle = Math.round(yRotation / 90) * 90;
		}
		if (zRotation != 0) {
			rotationAxis = Axis.Z;
			angle = Math.round(zRotation / 90) * 90;
		}

		angle %= 360;
		if (angle < -90)
			angle += 360;

		this.rotation = Rotation.NONE;
		if (angle == -90 || angle == 270)
			this.rotation = Rotation.CLOCKWISE_90;
		if (angle == 90)
			this.rotation = Rotation.COUNTERCLOCKWISE_90;
		if (angle == 180)
			this.rotation = Rotation.CLOCKWISE_180;

		mirror = Mirror.NONE;
	}

	public Vec3 applyWithoutOffset(Vec3 localVec) {
		Vec3 vec = localVec;
		if (mirror != null)
			vec = VecHelper.mirrorCentered(vec, mirror);
		if (rotationAxis != null)
			vec = VecHelper.rotateCentered(vec, angle, rotationAxis);
		return vec;
	}

	public Vec3 apply(Vec3 localVec) {
		return applyWithoutOffset(localVec).add(Vec3.atLowerCornerOf(offset));
	}

	public BlockPos applyWithoutOffset(BlockPos localPos) {
		return new BlockPos(applyWithoutOffset(VecHelper.getCenterOf(localPos)));
	}

	public BlockPos apply(BlockPos localPos) {
		return applyWithoutOffset(localPos).offset(offset);
	}

	public void apply(BlockEntity te) {
		if (te instanceof ITransformableTE)
			((ITransformableTE) te).transform(this);
	}

	/**
	 * Minecraft does not support blockstate rotation around axes other than y. Add
	 * specific cases here for blockstates, that should react to rotations around
	 * horizontal axes
	 */
	public BlockState apply(BlockState state) {
		if (mirror != null)
			state = state.mirror(mirror);

		Block block = state.getBlock();

		if (rotationAxis == Axis.Y) {
			if (block instanceof BellBlock) {
				if (state.getValue(BlockStateProperties.BELL_ATTACHMENT) == BellAttachType.DOUBLE_WALL) {
					state = state.setValue(BlockStateProperties.BELL_ATTACHMENT, BellAttachType.SINGLE_WALL);
				}
				return state.setValue(FaceAttachedHorizontalDirectionalBlock.FACING,
					rotation.rotate(state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING)));
			}
			return state.rotate(rotation);
		}

		if (block instanceof AbstractChassisBlock)
			return rotateChassis(state);

		if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
			Direction stateFacing = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);
			AttachFace stateFace = state.getValue(FACE);
			Direction forcedAxis = rotationAxis == Axis.Z ? Direction.EAST : Direction.SOUTH;

			if (stateFacing.getAxis() == rotationAxis && stateFace == AttachFace.WALL)
				return state;

			for (int i = 0; i < rotation.ordinal(); i++) {
				stateFace = state.getValue(FACE);
				stateFacing = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING);

				boolean b = state.getValue(FACE) == AttachFace.CEILING;
				state = state.setValue(HORIZONTAL_FACING, b ? forcedAxis : forcedAxis.getOpposite());

				if (stateFace != AttachFace.WALL) {
					state = state.setValue(FACE, AttachFace.WALL);
					continue;
				}

				if (stateFacing.getAxisDirection() == AxisDirection.POSITIVE) {
					state = state.setValue(FACE, AttachFace.FLOOR);
					continue;
				}
				state = state.setValue(FACE, AttachFace.CEILING);
			}

			return state;
		}

		boolean halfTurn = rotation == Rotation.CLOCKWISE_180;
		if (block instanceof StairBlock) {
			state = transformStairs(state, halfTurn);
			return state;
		}

		if (AllBlocks.BELT.has(state)) {
			state = transformBelt(state, halfTurn);
			return state;
		}

		if (state.hasProperty(FACING)) {
			Direction newFacing = transformFacing(state.getValue(FACING));
			if (state.hasProperty(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE)) {
				if (rotationAxis == newFacing.getAxis() && rotation.ordinal() % 2 == 1)
					state = state.cycle(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
			}
			state = state.setValue(FACING, newFacing);

		} else if (state.hasProperty(AXIS)) {
			state = state.setValue(AXIS, transformAxis(state.getValue(AXIS)));

		} else if (halfTurn) {

			if (state.hasProperty(FACING)) {
				Direction stateFacing = state.getValue(FACING);
				if (stateFacing.getAxis() == rotationAxis)
					return state;
			}

			if (state.hasProperty(HORIZONTAL_FACING)) {
				Direction stateFacing = state.getValue(HORIZONTAL_FACING);
				if (stateFacing.getAxis() == rotationAxis)
					return state;
			}

			state = state.rotate(rotation);
			if (state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE)
				state = state.setValue(SlabBlock.TYPE,
					state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM ? SlabType.TOP : SlabType.BOTTOM);
		}

		return state;
	}

	protected BlockState transformStairs(BlockState state, boolean halfTurn) {
		if (state.getValue(StairBlock.FACING)
			.getAxis() != rotationAxis) {
			for (int i = 0; i < rotation.ordinal(); i++) {
				Direction direction = state.getValue(StairBlock.FACING);
				Half half = state.getValue(StairBlock.HALF);
				if (direction.getAxisDirection() == AxisDirection.POSITIVE ^ half == Half.BOTTOM
					^ direction.getAxis() == Axis.Z)
					state = state.cycle(StairBlock.HALF);
				else
					state = state.setValue(StairBlock.FACING, direction.getOpposite());
			}
		} else {
			if (halfTurn) {
				state = state.cycle(StairBlock.HALF);
			}
		}
		return state;
	}

	protected BlockState transformBelt(BlockState state, boolean halfTurn) {
		Direction initialDirection = state.getValue(BeltBlock.HORIZONTAL_FACING);
		boolean diagonal =
			state.getValue(BeltBlock.SLOPE) == BeltSlope.DOWNWARD || state.getValue(BeltBlock.SLOPE) == BeltSlope.UPWARD;

		if (!diagonal) {
			for (int i = 0; i < rotation.ordinal(); i++) {
				Direction direction = state.getValue(BeltBlock.HORIZONTAL_FACING);
				BeltSlope slope = state.getValue(BeltBlock.SLOPE);
				boolean vertical = slope == BeltSlope.VERTICAL;
				boolean horizontal = slope == BeltSlope.HORIZONTAL;
				boolean sideways = slope == BeltSlope.SIDEWAYS;

				Direction newDirection = direction.getOpposite();
				BeltSlope newSlope = BeltSlope.VERTICAL;

				if (vertical) {
					if (direction.getAxis() == rotationAxis) {
						newDirection = direction.getCounterClockWise();
						newSlope = BeltSlope.SIDEWAYS;
					} else {
						newSlope = BeltSlope.HORIZONTAL;
						newDirection = direction;
						if (direction.getAxis() == Axis.Z)
							newDirection = direction.getOpposite();
					}
				}

				if (sideways) {
					newDirection = direction;
					if (direction.getAxis() == rotationAxis)
						newSlope = BeltSlope.HORIZONTAL;
					else
						newDirection = direction.getCounterClockWise();
				}

				if (horizontal) {
					newDirection = direction;
					if (direction.getAxis() == rotationAxis)
						newSlope = BeltSlope.SIDEWAYS;
					else if (direction.getAxis() != Axis.Z)
						newDirection = direction.getOpposite();
				}

				state = state.setValue(BeltBlock.HORIZONTAL_FACING, newDirection);
				state = state.setValue(BeltBlock.SLOPE, newSlope);
			}

		} else if (initialDirection.getAxis() != rotationAxis) {
			for (int i = 0; i < rotation.ordinal(); i++) {
				Direction direction = state.getValue(BeltBlock.HORIZONTAL_FACING);
				Direction newDirection = direction.getOpposite();
				BeltSlope slope = state.getValue(BeltBlock.SLOPE);
				boolean upward = slope == BeltSlope.UPWARD;
				boolean downward = slope == BeltSlope.DOWNWARD;

				// Rotate diagonal
				if (direction.getAxisDirection() == AxisDirection.POSITIVE ^ downward ^ direction.getAxis() == Axis.Z) {
					state = state.setValue(BeltBlock.SLOPE, upward ? BeltSlope.DOWNWARD : BeltSlope.UPWARD);
				} else {
					state = state.setValue(BeltBlock.HORIZONTAL_FACING, newDirection);
				}
			}

		} else if (halfTurn) {
			Direction direction = state.getValue(BeltBlock.HORIZONTAL_FACING);
			Direction newDirection = direction.getOpposite();
			BeltSlope slope = state.getValue(BeltBlock.SLOPE);
			boolean vertical = slope == BeltSlope.VERTICAL;

			if (diagonal) {
				state = state.setValue(BeltBlock.SLOPE, slope == BeltSlope.UPWARD ? BeltSlope.DOWNWARD
					: slope == BeltSlope.DOWNWARD ? BeltSlope.UPWARD : slope);
			} else if (vertical) {
				state = state.setValue(BeltBlock.HORIZONTAL_FACING, newDirection);
			}
		}
		return state;
	}

	public Axis transformAxis(Axis axisIn) {
		Direction facing = Direction.get(AxisDirection.POSITIVE, axisIn);
		facing = transformFacing(facing);
		Axis axis = facing.getAxis();
		return axis;
	}

	public Direction transformFacing(Direction facing) {
		if (mirror != null)
			facing = mirror.mirror(facing);
		for (int i = 0; i < rotation.ordinal(); i++)
			facing = DirectionHelper.rotateAround(facing, rotationAxis);
		return facing;
	}

	private BlockState rotateChassis(BlockState state) {
		if (rotation == Rotation.NONE)
			return state;

		BlockState rotated = state.setValue(AXIS, transformAxis(state.getValue(AXIS)));
		AbstractChassisBlock block = (AbstractChassisBlock) state.getBlock();

		for (Direction face : Iterate.directions) {
			BooleanProperty glueableSide = block.getGlueableSide(rotated, face);
			if (glueableSide != null)
				rotated = rotated.setValue(glueableSide, false);
		}

		for (Direction face : Iterate.directions) {
			BooleanProperty glueableSide = block.getGlueableSide(state, face);
			if (glueableSide == null || !state.getValue(glueableSide))
				continue;
			Direction rotatedFacing = transformFacing(face);
			BooleanProperty rotatedGlueableSide = block.getGlueableSide(rotated, rotatedFacing);
			if (rotatedGlueableSide != null)
				rotated = rotated.setValue(rotatedGlueableSide, true);
		}

		return rotated;
	}

	public static StructureTransform fromBuffer(FriendlyByteBuf buffer) {
		BlockPos readBlockPos = buffer.readBlockPos();
		int readAngle = buffer.readInt();
		int axisIndex = buffer.readVarInt();
		int rotationIndex = buffer.readVarInt();
		int mirrorIndex = buffer.readVarInt();
		return new StructureTransform(readBlockPos, readAngle, axisIndex == -1 ? null : Axis.values()[axisIndex],
			rotationIndex == -1 ? null : Rotation.values()[rotationIndex],
			mirrorIndex == -1 ? null : Mirror.values()[mirrorIndex]);
	}

	public void writeToBuffer(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(offset);
		buffer.writeInt(angle);
		buffer.writeVarInt(rotationAxis == null ? -1 : rotationAxis.ordinal());
		buffer.writeVarInt(rotation == null ? -1 : rotation.ordinal());
		buffer.writeVarInt(mirror == null ? - 1 : mirror.ordinal());
	}

}
