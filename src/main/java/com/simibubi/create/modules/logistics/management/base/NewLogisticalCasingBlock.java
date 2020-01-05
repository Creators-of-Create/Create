package com.simibubi.create.modules.logistics.management.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class NewLogisticalCasingBlock extends Block {

	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	public static final IProperty<Part> PART = EnumProperty.create("part", Part.class);
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public NewLogisticalCasingBlock() {
		super(Properties.from(Blocks.DARK_OAK_PLANKS));
		setDefaultState(getDefaultState().with(PART, Part.NONE).with(AXIS, Axis.Y).with(ACTIVE, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();
		for (Direction face : Direction.values()) {
			BlockState neighbour = context.getWorld().getBlockState(context.getPos().offset(face));
			if (!AllBlocks.LOGISTICAL_CASING.typeOf(neighbour))
				continue;
			if (neighbour.get(PART) != Part.NONE && face.getAxis() != neighbour.get(AXIS))
				continue;
			state = state.with(PART, face.getAxisDirection() == AxisDirection.POSITIVE ? Part.START : Part.END);
			state = state.with(AXIS, face.getAxis());
		}

		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Part part = state.get(PART);

		if (part == Part.NONE)
			return AllShapes.LOGISTICAL_CASING_SINGLE_SHAPE;

		if (part == Part.MIDDLE)
			return AllShapes.LOGISTICAL_CASING_MIDDLE.get(state.get(AXIS));

		Direction facing = VoxelShaper.axisAsFace(state.get(AXIS));
		if (part == Part.END)
			facing = facing.getOpposite();

		return AllShapes.LOGISTICAL_CASING_CAP.get(facing);
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction face, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		Part part = state.get(PART);
		boolean neighbourPresent = AllBlocks.LOGISTICAL_CASING.typeOf(facingState);
		boolean alongAxis = face.getAxis() == state.get(AXIS);
		boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;
		boolean neighbourAlongAxis = neighbourPresent
				&& (facingState.get(PART) == Part.NONE || facingState.get(AXIS) == face.getAxis());

		if (part == Part.NONE && neighbourPresent && neighbourAlongAxis) {
			state = state.with(PART, positive ? Part.START : Part.END);
			return state.with(AXIS, face.getAxis());
		}

		if (!alongAxis)
			return state;

		if (part == Part.END) {
			if (positive && neighbourPresent && neighbourAlongAxis)
				return state.with(PART, Part.MIDDLE);
			if (!positive && !neighbourPresent)
				return state.with(PART, Part.NONE).with(AXIS, Axis.Y);
		}

		if (part == Part.START) {
			if (!positive && neighbourPresent && neighbourAlongAxis)
				return state.with(PART, Part.MIDDLE);
			if (positive && !neighbourPresent)
				return state.with(PART, Part.NONE).with(AXIS, Axis.Y);
		}

		if (part == Part.MIDDLE) {
			if (!positive && !neighbourPresent)
				return state.with(PART, Part.START);
			if (positive && !neighbourPresent)
				return state.with(PART, Part.END);
		}

		return state;
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(AXIS, PART, ACTIVE);
		super.fillStateContainer(builder);
	}

	public enum Part implements IStringSerializable {
		START, MIDDLE, END, NONE;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
	}

}
