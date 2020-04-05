package com.simibubi.create.modules.contraptions.redstone;

import java.util.Random;

import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AnalogLeverBlock extends HorizontalFaceBlock implements ITE<AnalogLeverTileEntity> {

	public AnalogLeverBlock() {
		super(Properties.from(Blocks.LEVER));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new AnalogLeverTileEntity();
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (worldIn.isRemote) {
			addParticles(state, worldIn, pos, 1.0F);
			return true;
		}

		try {
			boolean sneak = player.isSneaking();
			AnalogLeverTileEntity te = getTileEntity(worldIn, pos);
			te.changeState(sneak);
			float f = .25f + ((te.state + 5) / 15f) * .5f;
			worldIn.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.2F, f);
		} catch (TileEntityException e) {}

		return true;
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		try {
			return getTileEntity(blockAccess, pos).state;
		} catch (TileEntityException e) {
			return 0;
		}
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return getFacing(blockState) == side ? getWeakPower(blockState, blockAccess, pos, side) : 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		try {
			AnalogLeverTileEntity tileEntity = getTileEntity(worldIn, pos);
			if (tileEntity.state != 0 && rand.nextFloat() < 0.25F)
				addParticles(stateIn, worldIn, pos, 0.5F);
		} catch (TileEntityException e) {}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		try {
			AnalogLeverTileEntity tileEntity = getTileEntity(worldIn, pos);
			if (!isMoving && state.getBlock() != newState.getBlock()) {
				if (tileEntity.state != 0)
					updateNeighbors(state, worldIn, pos);
				worldIn.removeTileEntity(pos);
			}
		} catch (TileEntityException e) {}
	}

	private static void addParticles(BlockState state, IWorld worldIn, BlockPos pos, float alpha) {
		Direction direction = state.get(HORIZONTAL_FACING).getOpposite();
		Direction direction1 = getFacing(state).getOpposite();
		double d0 = (double) pos.getX() + 0.5D + 0.1D * (double) direction.getXOffset()
				+ 0.2D * (double) direction1.getXOffset();
		double d1 = (double) pos.getY() + 0.5D + 0.1D * (double) direction.getYOffset()
				+ 0.2D * (double) direction1.getYOffset();
		double d2 = (double) pos.getZ() + 0.5D + 0.1D * (double) direction.getZOffset()
				+ 0.2D * (double) direction1.getZOffset();
		worldIn.addParticle(new RedstoneParticleData(1.0F, 0.0F, 0.0F, alpha), d0, d1, d2, 0.0D, 0.0D, 0.0D);
	}

	static void updateNeighbors(BlockState state, World world, BlockPos pos) {
		world.notifyNeighborsOfStateChange(pos, state.getBlock());
		world.notifyNeighborsOfStateChange(pos.offset(getFacing(state).getOpposite()), state.getBlock());
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return Blocks.LEVER.getShape(state, worldIn, pos, context);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(HORIZONTAL_FACING, FACE));
	}

	@Override
	public Class<AnalogLeverTileEntity> getTileEntityClass() {
		return AnalogLeverTileEntity.class;
	}

}
