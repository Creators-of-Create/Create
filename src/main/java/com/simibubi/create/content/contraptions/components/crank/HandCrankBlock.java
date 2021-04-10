package com.simibubi.create.content.contraptions.components.crank;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HandCrankBlock extends DirectionalKineticBlock implements ITE<HandCrankTileEntity> {

	public HandCrankBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CRANK.get(state.get(FACING));
	}
	
	@OnlyIn(Dist.CLIENT)
	public AllBlockPartials getRenderedHandle() {
		return AllBlockPartials.HAND_CRANK_HANDLE;
	}
	
	public int getRotationSpeed() {
		return 32;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {

		withTileEntityDo(worldIn, pos, te -> te.turn(player.isSneaking()));
		player.addExhaustion(getRotationSpeed() * AllConfigs.SERVER.kinetics.crankHungerMultiplier.getF());
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction preferred = getPreferredFacing(context);
		if (preferred == null || (context.getPlayer() != null && context.getPlayer()
			.isSneaking()))
			return getDefaultState().with(FACING, context.getFace());
		return getDefaultState().with(FACING, preferred.getOpposite());
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction facing = state.get(FACING)
			.getOpposite();
		BlockPos neighbourPos = pos.offset(facing);
		BlockState neighbour = worldIn.getBlockState(neighbourPos);
		return !neighbour.getCollisionShape(worldIn, neighbourPos)
			.isEmpty();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isRemote)
			return;

		Direction blockFacing = state.get(FACING);
		if (fromPos.equals(pos.offset(blockFacing.getOpposite()))) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.HAND_CRANK.create();
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(FACING)
			.getOpposite();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING)
			.getAxis();
	}

	@Override
	public Class<HandCrankTileEntity> getTileEntityClass() {
		return HandCrankTileEntity.class;
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
