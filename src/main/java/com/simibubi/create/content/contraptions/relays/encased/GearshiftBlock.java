package com.simibubi.create.content.contraptions.relays.encased;

import java.util.Random;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.RotationPropagator;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.gearbox.GearshiftTileEntity;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class GearshiftBlock extends AbstractEncasedShaftBlock implements ITE<GearshiftTileEntity> {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public GearshiftBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return AllTileEntities.GEARSHIFT.create();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(POWERED,
				context.getLevel().hasNeighborSignal(context.getClickedPos()));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != worldIn.hasNeighborSignal(pos)) {
			detachKinetics(worldIn, pos, true);
			worldIn.setBlock(pos, state.cycle(POWERED), 2);
		}
	}

	@Override
	public Class<GearshiftTileEntity> getTileEntityClass() {
		return GearshiftTileEntity.class;
	}

	public void detachKinetics(Level worldIn, BlockPos pos, boolean reAttachNextTick) {
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof KineticTileEntity))
			return;
		RotationPropagator.handleRemoved(worldIn, pos, (KineticTileEntity) te);

		// Re-attach next tick
		if (reAttachNextTick)
			worldIn.getBlockTicks().scheduleTick(pos, this, 0, TickPriority.EXTREMELY_HIGH);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof KineticTileEntity))
			return;
		KineticTileEntity kte = (KineticTileEntity) te;
		RotationPropagator.handleAdded(worldIn, pos, kte);
	}
}
