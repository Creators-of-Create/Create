package com.simibubi.create.content.contraptions;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.simibubi.create.api.contraption.transformable.MovedBlockTransformerRegistries;
import com.simibubi.create.api.contraption.transformable.MovedBlockTransformerRegistries.BlockEntityTransformer;
import com.simibubi.create.api.contraption.transformable.MovedBlockTransformerRegistries.BlockTransformer;
import com.simibubi.create.api.contraption.transformable.TransformableBlock;
import com.simibubi.create.api.contraption.transformable.TransformableBlockEntity;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
	public static final StreamCodec<ByteBuf, StructureTransform> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, i -> i.offset,
		ByteBufCodecs.INT, i -> i.angle,
		CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.AXIS), i -> i.rotationAxis,
		CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.ROTATION), i -> i.rotation,
		CatnipStreamCodecBuilders.nullable(CatnipStreamCodecs.MIRROR), i -> i.mirror,
	    StructureTransform::new
	);

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

	public Vec3 unapplyWithoutOffset(Vec3 globalVec) {
		Vec3 vec = globalVec;
		if (rotationAxis != null)
			vec = VecHelper.rotateCentered(vec, -angle, rotationAxis);
		if (mirror != null)
			vec = VecHelper.mirrorCentered(vec, mirror);

		return vec;
	}

	public Vec3 apply(Vec3 localVec) {
		return applyWithoutOffset(localVec).add(Vec3.atLowerCornerOf(offset));
	}

	public BlockPos applyWithoutOffset(BlockPos localPos) {
		return BlockPos.containing(applyWithoutOffset(VecHelper.getCenterOf(localPos)));
	}

	public BlockPos apply(BlockPos localPos) {
		return applyWithoutOffset(localPos).offset(offset);
	}

	public BlockPos unapply(BlockPos globalPos) {
		return unapplyWithoutOffset(globalPos.subtract(offset));
	}

	public BlockPos unapplyWithoutOffset(BlockPos globalPos) {
		return BlockPos.containing(unapplyWithoutOffset(VecHelper.getCenterOf(globalPos)));
	}

	public void apply(BlockEntity be) {
		BlockEntityTransformer transformer = MovedBlockTransformerRegistries.BLOCK_ENTITY_TRANSFORMERS.get(be.getType());
		if (transformer != null) {
			transformer.transform(be, this);
		} else if (be instanceof TransformableBlockEntity itbe) {
			itbe.transform(be, this);
		}
	}

	/**
	 * Vanilla does not support block state rotation around axes other than Y. Add
	 * specific cases here for vanilla block states so that they can react to rotations
	 * around horizontal axes. For Create blocks, implement ITransformableBlock.
	 */
	public BlockState apply(BlockState state) {
		Block block = state.getBlock();
		BlockTransformer transformer = MovedBlockTransformerRegistries.BLOCK_TRANSFORMERS.get(block);
		if (transformer != null) {
			return transformer.transform(state, this);
		} else if (block instanceof TransformableBlock transformable) {
			return transformable.transform(state, this);
		}

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
}
