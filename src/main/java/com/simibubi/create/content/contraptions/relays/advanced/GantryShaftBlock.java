package com.simibubi.create.content.contraptions.relays.advanced;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class GantryShaftBlock extends DirectionalKineticBlock {

	public static final Property<Part> PART = EnumProperty.create("part", Part.class);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public enum Part implements IStringSerializable {
		START, MIDDLE, END, SINGLE;

		@Override
		public String getString() {
			return Lang.asId(name());
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(PART, POWERED));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.EIGHT_VOXEL_POLE.get(state.get(FACING)
			.getAxis());
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState neighbour, IWorld world,
		BlockPos pos, BlockPos neighbourPos) {
		Direction facing = state.get(FACING);
		Axis axis = facing.getAxis();
		if (direction.getAxis() != axis)
			return state;
		boolean connect = AllBlocks.GANTRY_SHAFT.has(neighbour) && neighbour.get(FACING) == facing;

		Part part = state.get(PART);
		if (direction.getAxisDirection() == facing.getAxisDirection()) {
			if (connect) {
				if (part == Part.END)
					part = Part.MIDDLE;
				if (part == Part.SINGLE)
					part = Part.START;
			} else {
				if (part == Part.MIDDLE)
					part = Part.END;
				if (part == Part.START)
					part = Part.SINGLE;
			}
		} else {
			if (connect) {
				if (part == Part.START)
					part = Part.MIDDLE;
				if (part == Part.SINGLE)
					part = Part.END;
			} else {
				if (part == Part.MIDDLE)
					part = Part.START;
				if (part == Part.END)
					part = Part.SINGLE;
			}
		}

		return state.with(PART, part);
	}

	public GantryShaftBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(POWERED, false)
			.with(PART, Part.SINGLE));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = super.getStateForPlacement(context);
		BlockPos pos = context.getPos();
		World world = context.getWorld();
		Direction face = context.getFace();

		BlockState neighbour = world.getBlockState(pos.offset(state.get(FACING)
			.getOpposite()));

		BlockState clickedState =
			AllBlocks.GANTRY_SHAFT.has(neighbour) ? neighbour : world.getBlockState(pos.offset(face.getOpposite()));

		if (AllBlocks.GANTRY_SHAFT.has(clickedState) && clickedState.get(FACING)
			.getAxis() == state.get(FACING)
				.getAxis()) {
			Direction facing = clickedState.get(FACING);
			state = state.with(FACING, context.getPlayer() == null || !context.getPlayer()
				.isSneaking() ? facing : facing.getOpposite());
		}

		return state.with(POWERED, shouldBePowered(state, world, pos));
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		ActionResultType onWrenched = super.onWrenched(state, context);
		if (onWrenched.isAccepted()) {
			BlockPos pos = context.getPos();
			World world = context.getWorld();
			neighborChanged(world.getBlockState(pos), world, pos, state.getBlock(), pos, false);
		}
		return onWrenched;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		if (worldIn.isRemote)
			return;
		boolean previouslyPowered = state.get(POWERED);
		boolean shouldPower = worldIn.isBlockPowered(pos); // shouldBePowered(state, worldIn, pos);

		if (!previouslyPowered && !shouldPower && shouldBePowered(state, worldIn, pos)) {
			worldIn.setBlockState(pos, state.with(POWERED, true), 3);
			return;
		}

		if (previouslyPowered == shouldPower)
			return;

		// Collect affected gantry shafts
		List<BlockPos> toUpdate = new ArrayList<>();
		Direction facing = state.get(FACING);
		Axis axis = facing.getAxis();
		for (Direction d : Iterate.directionsInAxis(axis)) {
			BlockPos currentPos = pos.offset(d);
			while (true) {
				if (!worldIn.isBlockPresent(currentPos))
					break;
				BlockState currentState = worldIn.getBlockState(currentPos);
				if (!(currentState.getBlock() instanceof GantryShaftBlock))
					break;
				if (currentState.get(FACING) != facing)
					break;
				if (!shouldPower && currentState.get(POWERED) && worldIn.isBlockPowered(currentPos))
					return;
				if (currentState.get(POWERED) == shouldPower)
					break;
				toUpdate.add(currentPos);
				currentPos = currentPos.offset(d);
			}
		}

		toUpdate.add(pos);
		for (BlockPos blockPos : toUpdate) {
			BlockState blockState = worldIn.getBlockState(blockPos);
			TileEntity te = worldIn.getTileEntity(blockPos);
			if (te instanceof KineticTileEntity)
				((KineticTileEntity) te).detachKinetics();
			if (blockState.getBlock() instanceof GantryShaftBlock)
				worldIn.setBlockState(blockPos, blockState.with(POWERED, shouldPower), 2);
		}
	}

	protected boolean shouldBePowered(BlockState state, World worldIn, BlockPos pos) {
		boolean shouldPower = worldIn.isBlockPowered(pos);

		Direction facing = state.get(FACING);
		for (Direction d : Iterate.directionsInAxis(facing.getAxis())) {
			BlockPos neighbourPos = pos.offset(d);
			if (!worldIn.isBlockPresent(neighbourPos))
				continue;
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			if (!(neighbourState.getBlock() instanceof GantryShaftBlock))
				continue;
			if (neighbourState.get(FACING) != facing)
				continue;
			shouldPower |= neighbourState.get(POWERED);
		}

		return shouldPower;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(FACING)
			.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING)
			.getAxis();
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.GANTRY_SHAFT.create();
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return super.areStatesKineticallyEquivalent(oldState, newState)
			&& oldState.get(POWERED) == newState.get(POWERED);
	}

}
