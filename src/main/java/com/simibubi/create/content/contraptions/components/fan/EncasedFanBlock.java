package com.simibubi.create.content.contraptions.components.fan;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.logistics.block.chute.AbstractChuteBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

	public EncasedFanBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ENCASED_FAN.create();
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
		blockUpdate(state, worldIn, pos);
	}

	@Override
	public void updateNeighbors(BlockState stateIn, IWorld worldIn, BlockPos pos, int flags) {
		super.updateNeighbors(stateIn, worldIn, pos, flags);
		blockUpdate(stateIn, worldIn, pos);
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState p_196243_4_, boolean p_196243_5_) {
		if (state.hasTileEntity() && (state.getBlock() != p_196243_4_.getBlock() || !p_196243_4_.hasTileEntity())) {
			withTileEntityDo(world, pos, EncasedFanTileEntity::updateChute);
			world.removeTileEntity(pos);
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		blockUpdate(state, worldIn, pos);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		Direction face = context.getFace();

		BlockState placedOn = world.getBlockState(pos.offset(face.getOpposite()));
		BlockState placedOnOpposite = world.getBlockState(pos.offset(face));
		if (AbstractChuteBlock.isChute(placedOn))
			return getDefaultState().with(FACING, face.getOpposite());
		if (AbstractChuteBlock.isChute(placedOnOpposite))
			return getDefaultState().with(FACING, face);

		Direction preferredFacing = getPreferredFacing(context);
		if (preferredFacing == null)
			preferredFacing = context.getNearestLookingDirection();
		return getDefaultState().with(FACING, context.getPlayer() != null && context.getPlayer()
			.isSneaking() ? preferredFacing : preferredFacing.getOpposite());
	}

	protected void blockUpdate(BlockState state, IWorld worldIn, BlockPos pos) {
		if (worldIn instanceof WrappedWorld)
			return;
		notifyFanTile(worldIn, pos);
		if (worldIn.isRemote())
			return;
		withTileEntityDo(worldIn, pos, te -> te.queueGeneratorUpdate());
	}

	protected void notifyFanTile(IWorld world, BlockPos pos) {
		withTileEntityDo(world, pos, EncasedFanTileEntity::blockInFrontChanged);
	}

	@Override
	public BlockState updateAfterWrenched(BlockState newState, ItemUseContext context) {
		blockUpdate(newState, context.getWorld(), context.getPos());
		return newState;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING)
			.getAxis();
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(FACING)
			.getOpposite();
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
