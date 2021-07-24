package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class AdjustablePulleyBlock extends EncasedBeltBlock implements ITE<AdjustablePulleyTileEntity> {

	public static BooleanProperty POWERED = BlockStateProperties.POWERED;

	public AdjustablePulleyBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ADJUSTABLE_PULLEY.create();
	}

	@Override
	public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, worldIn, pos, oldState, isMoving);
		if (oldState.getBlock() == state.getBlock())
			return;
		withTileEntityDo(worldIn, pos, AdjustablePulleyTileEntity::neighborChanged);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return super.getStateForPlacement(context).setValue(POWERED, context.getLevel()
			.hasNeighborSignal(context.getClickedPos()));
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return super.areStatesKineticallyEquivalent(oldState, newState)
			&& oldState.getValue(POWERED) == newState.getValue(POWERED);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		withTileEntityDo(worldIn, pos, AdjustablePulleyTileEntity::neighborChanged);

		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != worldIn.hasNeighborSignal(pos))
			worldIn.setBlock(pos, state.cycle(POWERED), 18);
	}

	@Override
	public Class<AdjustablePulleyTileEntity> getTileEntityClass() {
		return AdjustablePulleyTileEntity.class;
	}

}
