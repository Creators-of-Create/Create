package com.simibubi.create.content.contraptions.components.actors;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		withTileEntityDo(world, pos, PortableStorageInterfaceTileEntity::neighbourChanged);
	}

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction direction = context.getNearestLookingDirection();
		if (context.getPlayer() != null && context.getPlayer()
			.isSteppingCarefully())
			direction = direction.getOpposite();
		return defaultBlockState().setValue(FACING, direction.getOpposite());
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

	@Override
	public BlockEntityType<? extends PortableStorageInterfaceTileEntity> getTileEntityType() {
		return fluids ? AllTileEntities.PORTABLE_FLUID_INTERFACE.get()
			: AllTileEntities.PORTABLE_STORAGE_INTERFACE.get();
	}

}
