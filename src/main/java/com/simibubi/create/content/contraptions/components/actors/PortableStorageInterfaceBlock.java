package com.simibubi.create.content.contraptions.components.actors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PortableStorageInterfaceBlock extends WrenchableDirectionalBlock
	implements ITE<PortableStorageInterfaceTileEntity> {

	boolean fluids;

	public static PortableStorageInterfaceBlock forItems(Properties p_i48415_1_) {
		return new PortableStorageInterfaceBlock(p_i48415_1_, false);
	}

	public static PortableStorageInterfaceBlock forFluids(Properties p_i48415_1_) {
		return new PortableStorageInterfaceBlock(p_i48415_1_, true);
	}

	private PortableStorageInterfaceBlock(Properties p_i48415_1_, boolean fluids) {
		super(p_i48415_1_);
		this.fluids = fluids;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return (fluids ? AllTileEntities.PORTABLE_FLUID_INTERFACE : AllTileEntities.PORTABLE_STORAGE_INTERFACE)
			.create();
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		withTileEntityDo(world, pos, PortableStorageInterfaceTileEntity::neighbourChanged);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getNearestLookingDirection()
			.getOpposite());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.PORTABLE_STORAGE_INTERFACE.get(state.getValue(FACING));
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		return getTileEntityOptional(worldIn, pos).map(te -> te.isConnected() ? 15 : 0)
			.orElse(0);
	}

	@Override
	public Class<PortableStorageInterfaceTileEntity> getTileEntityClass() {
		return PortableStorageInterfaceTileEntity.class;
	}

}
