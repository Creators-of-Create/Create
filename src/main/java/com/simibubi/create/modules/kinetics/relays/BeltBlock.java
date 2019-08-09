package com.simibubi.create.modules.kinetics.relays;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IWithoutBlockItem;
import com.simibubi.create.modules.kinetics.base.HorizontalKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BeltBlock extends HorizontalKineticBlock implements IWithoutBlockItem {

	public static final IProperty<Slope> SLOPE = EnumProperty.create("slope", Slope.class);
	public static final IProperty<Part> PART = EnumProperty.create("part", Part.class);

	public BeltBlock() {
		super(Properties.from(Blocks.BROWN_WOOL));
		setDefaultState(getDefaultState().with(SLOPE, Slope.HORIZONTAL).with(PART, Part.START));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(SLOPE, PART);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BeltTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return false;
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (worldIn.isRemote)
			return;
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity == null)
			return;
		if (!(tileEntity instanceof BeltTileEntity))
			return;
		BeltTileEntity beltEntity = (BeltTileEntity) tileEntity;
		BlockPos controller = beltEntity.getController();
		beltEntity.setSource(null);
		beltEntity.remove();
		
		int limit = 1000;
		BlockPos toDestroy = controller;
		BlockState destroyedBlock = null;

		do {

			if (!toDestroy.equals(pos)) {
				destroyedBlock = worldIn.getBlockState(toDestroy);
				if (!AllBlocks.BELT.typeOf(destroyedBlock))
					break;

				BeltTileEntity te = (BeltTileEntity) worldIn.getTileEntity(toDestroy);
				boolean hasPulley = te.hasPulley();
				te.setSource(null);
				te.remove();
				
				if (hasPulley) {
					worldIn.setBlockState(toDestroy, AllBlocks.AXIS.get().getDefaultState()
							.with(BlockStateProperties.AXIS, getRotationAxis(destroyedBlock)), 3);
				} else {
					worldIn.destroyBlock(toDestroy, false);
				}

				if (destroyedBlock.get(PART) == Part.END)
					break;
			}

			Slope slope = state.get(SLOPE);
			Direction direction = state.get(HORIZONTAL_FACING);

			if (slope == Slope.VERTICAL) {
				toDestroy = toDestroy.up(direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1);
				continue;
			}

			toDestroy = toDestroy.offset(direction);
			if (slope != Slope.HORIZONTAL)
				toDestroy = toDestroy.up(slope == Slope.UPWARD ? 1 : -1);

		} while (limit-- > 0);

	}

	@Override
	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face) {
		if (face.getAxis() != getRotationAxis(state))
			return false;
		BeltTileEntity beltEntity = (BeltTileEntity) world.getTileEntity(pos);
		return beltEntity != null && beltEntity.hasPulley();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING).getAxis() == Axis.X ? Axis.Z : Axis.X;
	}

	public enum Slope implements IStringSerializable {
		HORIZONTAL, UPWARD, DOWNWARD, VERTICAL;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
	}

	public enum Part implements IStringSerializable {
		START, MIDDLE, END;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
	}

	public static List<BlockPos> getBeltChain(World world, BlockPos controllerPos) {
		List<BlockPos> positions = new LinkedList<>();

		BlockState blockState = world.getBlockState(controllerPos);
		if (!AllBlocks.BELT.typeOf(blockState))
			return positions;

		Slope slope = blockState.get(SLOPE);
		Direction direction = blockState.get(HORIZONTAL_FACING);

		int limit = 1000;
		BlockPos current = controllerPos;
		do {
			positions.add(current);

			if (!AllBlocks.BELT.typeOf(world.getBlockState(current)))
				break;
			if (world.getBlockState(current).get(PART) == Part.END)
				break;
			if (slope == Slope.VERTICAL) {
				current = current.up(direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -1);
				continue;
			}
			current = current.offset(direction);
			if (slope != Slope.HORIZONTAL)
				current = current.up(slope == Slope.UPWARD ? 1 : -1);
		} while (limit-- > 0);

		return positions;
	}

}
