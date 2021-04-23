package com.simibubi.create.content.optics.radiant_beacon;

import javax.annotation.Nullable;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class RadiantBeaconBlock extends ProperDirectionalBlock implements ITE<RadiantBeaconTileEntity> {
	public RadiantBeaconBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(FACING, Direction.UP));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getNearestLookingDirection()
				.getOpposite());
	}

	@Override
	public Class<RadiantBeaconTileEntity> getTileEntityClass() {
		return RadiantBeaconTileEntity.class;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return world.getMaxLightLevel();
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.RADIANT_BEACON.create();
	}
}
