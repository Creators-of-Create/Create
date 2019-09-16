package com.simibubi.create.modules.contraptions.receivers;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.modules.contraptions.relays.EncasedShaftBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EncasedFanBlock extends EncasedShaftBlock implements IWithTileEntity<EncasedFanTileEntity> {

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new EncasedFanTileEntity();
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		notifyFanTile(worldIn, pos);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		notifyFanTile(worldIn, pos);

		if (worldIn.isRemote || state.get(AXIS).isHorizontal())
			return;

		withTileEntityDo(worldIn, pos, EncasedFanTileEntity::updateGenerator);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		if (context.isPlacerSneaking())
			return super.getStateForPlacement(context);
		BlockState blockState = context.getWorld()
				.getBlockState(context.getPos().offset(context.getFace().getOpposite()));
		if (AllBlocks.ENCASED_FAN.typeOf(blockState))
			return getDefaultState().with(AXIS, blockState.get(AXIS));
		Axis preferred = getPreferredAxis(context);
		if (preferred != null)
			return getDefaultState().with(AXIS, preferred);
		return super.getStateForPlacement(context);
	}

	protected void notifyFanTile(IWorld world, BlockPos pos) {
		withTileEntityDo(world, pos, EncasedFanTileEntity::updateFrontBlock);
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	public static boolean canAirPassThrough(World world, BlockPos pos, Direction direction) {
		if (!world.isBlockPresent(pos))
			return true;
		BlockState state = world.getBlockState(pos);
		return !Block.hasSolidSide(state, world, pos, direction.getOpposite());
	}

}
