package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContentObserverBlock extends HorizontalBlock implements ITE<ContentObserverTileEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public ContentObserverBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.CONTENT_OBSERVER.get(state.getValue(FACING));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CONTENT_OBSERVER.create();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, FACING);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = defaultBlockState();
		Capability<IItemHandler> itemCap = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
		Capability<IFluidHandler> fluidCap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

		Direction preferredFacing = null;
		for (Direction face : Iterate.horizontalDirections) {
			BlockPos offsetPos = context.getClickedPos()
				.relative(face);
			World world = context.getLevel();
			boolean canDetect = false;
			TileEntity tileEntity = world.getBlockEntity(offsetPos);

			if (TileEntityBehaviour.get(tileEntity, TransportedItemStackHandlerBehaviour.TYPE) != null)
				canDetect = true;
			else if (TileEntityBehaviour.get(tileEntity, FluidTransportBehaviour.TYPE) != null)
				canDetect = true;
			else if (tileEntity != null && (tileEntity.getCapability(itemCap)
				.isPresent()
				|| tileEntity.getCapability(fluidCap)
					.isPresent()))
				canDetect = true;
			else if (tileEntity instanceof FunnelTileEntity)
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
	public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return isSignalSource(blockState) && (side == null || side != blockState.getValue(FACING)
			.getOpposite()) ? 15 : 0;
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		worldIn.setBlock(pos, state.setValue(POWERED, false), 2);
		worldIn.updateNeighborsAt(pos, this);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return side != state.getValue(FACING)
			.getOpposite();
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
			worldIn.removeBlockEntity(pos);
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		InvManipulationBehaviour behaviour = TileEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
		if (behaviour != null)
			behaviour.onNeighborChanged(fromPos);
	}

	public void onFunnelTransfer(World world, BlockPos funnelPos, ItemStack transferred) {
		for (Direction direction : Iterate.horizontalDirections) {
			BlockPos detectorPos = funnelPos.relative(direction);
			BlockState detectorState = world.getBlockState(detectorPos);
			if (!AllBlocks.CONTENT_OBSERVER.has(detectorState))
				continue;
			if (detectorState.getValue(FACING) != direction.getOpposite())
				continue;
			withTileEntityDo(world, detectorPos, te -> {
				FilteringBehaviour filteringBehaviour = TileEntityBehaviour.get(te, FilteringBehaviour.TYPE);
				if (filteringBehaviour == null)
					return;
				if (!filteringBehaviour.test(transferred))
					return;
				te.activate(4);
			});
		}
	}

	@Override
	public Class<ContentObserverTileEntity> getTileEntityClass() {
		return ContentObserverTileEntity.class;
	}

}
