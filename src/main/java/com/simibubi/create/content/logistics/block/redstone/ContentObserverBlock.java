package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
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
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Random;

public class ContentObserverBlock extends HorizontalBlock implements ITE<ContentObserverTileEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public ContentObserverBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.CONTENT_OBSERVER.get(state.get(HORIZONTAL_FACING));
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
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED, HORIZONTAL_FACING);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		Direction preferredFacing = null;
		for (Direction face : Iterate.horizontalDirections) {
			BlockPos offsetPos = context.getPos()
				.offset(face);
			World world = context.getWorld();
			boolean canDetect = false;
			TileEntity tileEntity = world.getTileEntity(offsetPos);

			if (TileEntityBehaviour.get(tileEntity, TransportedItemStackHandlerBehaviour.TYPE) != null)
				canDetect = true;
			else if (tileEntity != null && tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
				.isPresent())
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
			return state.with(HORIZONTAL_FACING, preferredFacing);
		return state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing()
			.getOpposite());
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return state.get(POWERED);
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return canProvidePower(blockState) && (side == null || side != blockState.get(HORIZONTAL_FACING)
			.getOpposite()) ? 15 : 0;
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		worldIn.setBlockState(pos, state.with(POWERED, false), 2);
		worldIn.notifyNeighborsOfStateChange(pos, this);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return side != state.get(HORIZONTAL_FACING)
			.getOpposite();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
			worldIn.removeTileEntity(pos);
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
			BlockPos detectorPos = funnelPos.offset(direction);
			BlockState detectorState = world.getBlockState(detectorPos);
			if (!AllBlocks.CONTENT_OBSERVER.has(detectorState))
				continue;
			if (detectorState.get(HORIZONTAL_FACING) != direction.getOpposite())
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
