package com.simibubi.create.modules.logistics.transport.villager;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.logistics.management.base.ILogisticalCasingAttachment;
import com.simibubi.create.modules.logistics.management.base.LogisticalCasingTileEntity;
import com.simibubi.create.modules.logistics.transport.CardboardBoxEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class PackageFunnelBlock extends ProperDirectionalBlock implements IWithTileEntity<PackageFunnelTileEntity>, ILogisticalCasingAttachment {

	public PackageFunnelBlock() {
		super(Properties.from(Blocks.PISTON));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new PackageFunnelTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getFace());
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		if (context.getEntity() instanceof CardboardBoxEntity)
			return VoxelShapes.empty();
		return getShape(state, worldIn, pos, context);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction facing = state.get(FACING);
		BlockPos offset = pos.offset(facing.getOpposite());
		BlockState blockState = worldIn.getBlockState(offset);
		boolean isCasing = AllBlocks.LOGISTICAL_CASING.typeOf(blockState);
		return isCasing;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (state.isValidPosition(worldIn, pos))
			return;

		TileEntity tileentity = state.hasTileEntity() ? worldIn.getTileEntity(pos) : null;
		spawnDrops(state, worldIn, pos, tileentity);
		worldIn.removeBlock(pos, false);

		for (Direction direction : Direction.values())
			worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.PACKAGE_FUNNEL.get(state.get(FACING));
	}

	@Override
	public void onCasingUpdated(IWorld world, BlockPos pos, LogisticalCasingTileEntity te) {
		Direction facing = world.getBlockState(pos).get(FACING).getOpposite();
		if (!te.getPos().equals(pos.offset(facing)))
			return;
		withTileEntityDo(world, pos, PackageFunnelTileEntity::refreshAddressList);
	}

}
