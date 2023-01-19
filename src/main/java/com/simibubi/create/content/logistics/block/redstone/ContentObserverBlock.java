package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContentObserverBlock extends HorizontalDirectionalBlock implements IBE<ContentObserverBlockEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public ContentObserverBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return AllShapes.CONTENT_OBSERVER.get(state.getValue(FACING));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, FACING);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState();
		Capability<IItemHandler> itemCap = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		Capability<IFluidHandler> fluidCap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

		Direction preferredFacing = null;
		for (Direction face : Iterate.horizontalDirections) {
			BlockPos offsetPos = context.getClickedPos()
				.relative(face);
			Level world = context.getLevel();
			boolean canDetect = false;
			BlockEntity blockEntity = world.getBlockEntity(offsetPos);

			if (BlockEntityBehaviour.get(blockEntity, TransportedItemStackHandlerBehaviour.TYPE) != null)
				canDetect = true;
			else if (BlockEntityBehaviour.get(blockEntity, FluidTransportBehaviour.TYPE) != null)
				canDetect = true;
			else if (blockEntity != null && (blockEntity.getCapability(itemCap)
				.isPresent()
				|| blockEntity.getCapability(fluidCap)
					.isPresent()))
				canDetect = true;
			else if (blockEntity instanceof FunnelBlockEntity)
				canDetect = true;

			if (canDetect) {
				if (preferredFacing != null) {
					preferredFacing = null;
					break;
				}
				preferredFacing = face;
			}

		}

		if (preferredFacing != null)
			return state.setValue(FACING, preferredFacing);
		return state.setValue(FACING, context.getHorizontalDirection()
			.getOpposite());
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return state.getValue(POWERED);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return isSignalSource(blockState) && (side == null || side != blockState.getValue(FACING)
			.getOpposite()) ? 15 : 0;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		worldIn.setBlock(pos, state.setValue(POWERED, false), 2);
		worldIn.updateNeighborsAt(pos, this);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return side != state.getValue(FACING)
			.getOpposite();
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		IBE.onRemove(state, worldIn, pos, newState);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		InvManipulationBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
		if (behaviour != null)
			behaviour.onNeighborChanged(fromPos);
	}

	public void onFunnelTransfer(Level world, BlockPos funnelPos, ItemStack transferred) {
		for (Direction direction : Iterate.horizontalDirections) {
			BlockPos detectorPos = funnelPos.relative(direction);
			BlockState detectorState = world.getBlockState(detectorPos);
			if (!AllBlocks.CONTENT_OBSERVER.has(detectorState))
				continue;
			if (detectorState.getValue(FACING) != direction.getOpposite())
				continue;
			withBlockEntityDo(world, detectorPos, be -> {
				FilteringBehaviour filteringBehaviour = BlockEntityBehaviour.get(be, FilteringBehaviour.TYPE);
				if (filteringBehaviour == null)
					return;
				if (!filteringBehaviour.test(transferred))
					return;
				be.activate(4);
			});
		}
	}

	@Override
	public Class<ContentObserverBlockEntity> getBlockEntityClass() {
		return ContentObserverBlockEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends ContentObserverBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CONTENT_OBSERVER.get();
	}

}
