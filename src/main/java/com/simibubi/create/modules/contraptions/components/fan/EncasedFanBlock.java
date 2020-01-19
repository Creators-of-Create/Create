package com.simibubi.create.modules.contraptions.components.fan;

import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EncasedFanBlock extends DirectionalKineticBlock implements IWithTileEntity<EncasedFanTileEntity> {

	public EncasedFanBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new EncasedFanTileEntity();
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		blockUpdate(state, worldIn, pos);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		blockUpdate(state, worldIn, pos);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction preferredFacing = getPreferredFacing(context);
		if (preferredFacing == null)
			preferredFacing = context.getNearestLookingDirection();
		return getDefaultState().with(FACING,
				context.isPlacerSneaking() ? preferredFacing : preferredFacing.getOpposite());
	}

	protected void blockUpdate(BlockState state, World worldIn, BlockPos pos) {
		notifyFanTile(worldIn, pos);
		if (worldIn.isRemote || state.get(FACING) != Direction.DOWN)
			return;
		withTileEntityDo(worldIn, pos, EncasedFanTileEntity::updateGenerator);
	}

	protected void notifyFanTile(IWorld world, BlockPos pos) {
		withTileEntityDo(world, pos, EncasedFanTileEntity::blockInFrontChanged);
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING).getAxis();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(FACING).getOpposite();
	}
	
	@Override
	public boolean showCapacityWithAnnotation() {
		return true;
	}

}
