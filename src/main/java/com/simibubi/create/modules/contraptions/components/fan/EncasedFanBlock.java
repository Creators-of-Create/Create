package com.simibubi.create.modules.contraptions.components.fan;

import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class EncasedFanBlock extends DirectionalKineticBlock implements ITE<EncasedFanTileEntity> {

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
				context.getPlayer().isSneaking() ? preferredFacing : preferredFacing.getOpposite());
	}

	protected void blockUpdate(BlockState state, World worldIn, BlockPos pos) {
		notifyFanTile(worldIn, pos);
		if (worldIn.isRemote)
			return;
		withTileEntityDo(worldIn, pos, te -> te.updateGenerator(state.get(FACING)));
	}

	protected void notifyFanTile(IWorld world, BlockPos pos) {
		withTileEntityDo(world, pos, EncasedFanTileEntity::blockInFrontChanged);
	}

	@Override
	public BlockState updateAfterWrenched(BlockState newState, ItemUseContext context) {
		blockUpdate(newState, context.getWorld(), context.getPos());
		return newState;
	}

//	@Override // TODO 1.15 register layer
//	public BlockRenderLayer getRenderLayer() {
//		return BlockRenderLayer.CUTOUT;
//	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING).getAxis();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(FACING).getOpposite();
	}
	
	@Override
	public boolean showCapacityWithAnnotation() {
		return true;
	}

	@Override
	public Class<EncasedFanTileEntity> getTileEntityClass() {
		return EncasedFanTileEntity.class;
	}

}
