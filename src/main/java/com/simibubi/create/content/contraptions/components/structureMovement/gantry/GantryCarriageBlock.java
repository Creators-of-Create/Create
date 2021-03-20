package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.relays.advanced.GantryShaftBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class GantryCarriageBlock extends DirectionalAxisKineticBlock implements ITE<GantryCarriageTileEntity> {

	public GantryCarriageBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		Direction direction = state.get(FACING);
		BlockState shaft = world.getBlockState(pos.offset(direction.getOpposite()));
		return AllBlocks.GANTRY_SHAFT.has(shaft) && shaft.get(GantryShaftBlock.FACING)
			.getAxis() != direction.getAxis();
	}

	@Override
	public void updateDiagonalNeighbors(BlockState stateIn, IWorld worldIn, BlockPos pos, int flags, int count) {
		super.updateDiagonalNeighbors(stateIn, worldIn, pos, flags, count);
		withTileEntityDo(worldIn, pos, GantryCarriageTileEntity::checkValidGantryShaft);
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.GANTRY_PINION.create();
	}

	@Override
	protected Direction getFacingForPlacement(BlockItemUseContext context) {
		return context.getFace();
	}

	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		if (!player.isAllowEdit() || player.isSneaking())
			return ActionResultType.PASS;
		if (player.getHeldItem(handIn)
			.isEmpty()) {
			withTileEntityDo(worldIn, pos, te -> te.checkValidGantryShaft());
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState stateForPlacement = super.getStateForPlacement(context);
		Direction opposite = stateForPlacement.get(FACING)
			.getOpposite();
		return cycleAxisIfNecessary(stateForPlacement, opposite, context.getWorld()
			.getBlockState(context.getPos()
				.offset(opposite)));
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		if (!isValidPosition(state, world, pos))
			world.destroyBlock(pos, true);
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState otherState, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (state.get(FACING) != direction.getOpposite())
			return state;
		return cycleAxisIfNecessary(state, direction, otherState);
	}

	protected BlockState cycleAxisIfNecessary(BlockState state, Direction direction, BlockState otherState) {
		if (!AllBlocks.GANTRY_SHAFT.has(otherState))
			return state;
		if (otherState.get(GantryShaftBlock.FACING)
			.getAxis() == direction.getAxis())
			return state;
		if (isValidGantryShaftAxis(state, otherState))
			return state;
		return state.cycle(AXIS_ALONG_FIRST_COORDINATE);
	}

	public static boolean isValidGantryShaftAxis(BlockState pinionState, BlockState gantryState) {
		return getValidGantryShaftAxis(pinionState) == gantryState.get(GantryShaftBlock.FACING)
			.getAxis();
	}

	public static Axis getValidGantryShaftAxis(BlockState state) {
		if (!(state.getBlock() instanceof GantryCarriageBlock))
			return Axis.Y;
		IRotate block = (IRotate) state.getBlock();
		Axis rotationAxis = block.getRotationAxis(state);
		Axis facingAxis = state.get(FACING)
			.getAxis();
		for (Axis axis : Iterate.axes)
			if (axis != rotationAxis && axis != facingAxis)
				return axis;
		return Axis.Y;
	}

	public static Axis getValidGantryPinionAxis(BlockState state, Axis shaftAxis) {
		Axis facingAxis = state.get(FACING)
			.getAxis();
		for (Axis axis : Iterate.axes)
			if (axis != shaftAxis && axis != facingAxis)
				return axis;
		return Axis.Y;
	}

	@Override
	public Class<GantryCarriageTileEntity> getTileEntityClass() {
		return GantryCarriageTileEntity.class;
	}

}
