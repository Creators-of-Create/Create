package com.simibubi.create.content.contraptions;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

public class StructureTransform {

	// Assuming structures cannot be rotated around multiple axes at once
	public Axis rotationAxis;
	public BlockPos offset;
	public int angle;
	public Rotation rotation;
	public Mirror mirror;

	private StructureTransform(BlockPos offset, int angle, Axis axis, Rotation rotation, Mirror mirror) {
		this.offset = offset;
		this.angle = angle;
		rotationAxis = axis;
		this.rotation = rotation;
		this.mirror = mirror;
	}

	public StructureTransform(BlockPos offset, Axis axis, Rotation rotation, Mirror mirror) {
		this(offset, rotation == Rotation.NONE ? 0 : (4 - rotation.ordinal()) * 90, axis, rotation, mirror);
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

	public Vec3 applyWithoutOffsetUncentered(Vec3 localVec) {
		Vec3 vec = localVec;
		if (mirror != null)
			vec = VecHelper.mirror(vec, mirror);
		if (rotationAxis != null)
			vec = VecHelper.rotate(vec, angle, rotationAxis);
		return vec;
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

	public void apply(BlockEntity be) {
		if (be instanceof ITransformableBlockEntity)
			((ITransformableBlockEntity) be).transform(this);
	}

	/**
	 * Vanilla does not support block state rotation around axes other than Y. Add
	 * specific cases here for vanilla block states so that they can react to rotations
	 * around horizontal axes. For Create blocks, implement ITransformableBlock.
	 */
	public BlockState apply(BlockState state) {
		Block block = state.getBlock();
		if (block instanceof ITransformableBlock transformable)
			return transformable.transform(state, this);

		if (mirror != null)
			state = state.mirror(mirror);

		if (rotationAxis == Axis.Y) {
			if (block instanceof BellBlock) {
				if (state.getValue(BlockStateProperties.BELL_ATTACHMENT) == BellAttachType.DOUBLE_WALL)
					state = state.setValue(BlockStateProperties.BELL_ATTACHMENT, BellAttachType.SINGLE_WALL);
				return state.setValue(BellBlock.FACING,
					rotation.rotate(state.getValue(BellBlock.FACING)));
			}

			return state.rotate(rotation);
		}

		if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
			DirectionProperty facingProperty = FaceAttachedHorizontalDirectionalBlock.FACING;
			EnumProperty<AttachFace> faceProperty = FaceAttachedHorizontalDirectionalBlock.FACE;
			Direction stateFacing = state.getValue(facingProperty);
			AttachFace stateFace = state.getValue(faceProperty);
			boolean z = rotationAxis == Axis.Z;
			Direction forcedAxis = z ? Direction.WEST : Direction.SOUTH;

			if (stateFacing.getAxis() == rotationAxis && stateFace == AttachFace.WALL)
				return state;

			for (int i = 0; i < rotation.ordinal(); i++) {
				stateFace = state.getValue(faceProperty);
				stateFacing = state.getValue(facingProperty);

				boolean b = state.getValue(faceProperty) == AttachFace.CEILING;
				state = state.setValue(facingProperty, b ? forcedAxis : forcedAxis.getOpposite());

				if (stateFace != AttachFace.WALL) {
					state = state.setValue(faceProperty, AttachFace.WALL);
					continue;
				}

				if (stateFacing.getAxisDirection() == (z ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE)) {
					state = state.setValue(faceProperty, AttachFace.FLOOR);
					continue;
				}
				state = state.setValue(faceProperty, AttachFace.CEILING);
			}

			return state;
		}

		boolean halfTurn = rotation == Rotation.CLOCKWISE_180;
		if (block instanceof StairBlock) {
			state = transformStairs(state, halfTurn);
			return state;
		}

		if (state.hasProperty(FACING)) {
			state = state.setValue(FACING, rotateFacing(state.getValue(FACING)));
		} else if (state.hasProperty(AXIS)) {
			state = state.setValue(AXIS, rotateAxis(state.getValue(AXIS)));
		} else if (halfTurn) {
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

	public Direction mirrorFacing(Direction facing) {
		if (mirror != null)
			return mirror.mirror(facing);
		return facing;
	}

	public Axis rotateAxis(Axis axis) {
		Direction facing = Direction.get(AxisDirection.POSITIVE, axis);
		return rotateFacing(facing).getAxis();
	}

	public Direction rotateFacing(Direction facing) {
		for (int i = 0; i < rotation.ordinal(); i++)
			facing = facing.getClockWise(rotationAxis);
		return facing;
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
