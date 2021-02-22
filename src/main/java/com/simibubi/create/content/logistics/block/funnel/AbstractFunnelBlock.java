package com.simibubi.create.content.logistics.block.funnel;

import javax.annotation.Nullable;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractFunnelBlock extends Block implements ITE<FunnelTileEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	protected AbstractFunnelBlock(Properties p_i48377_1_) {
		super(p_i48377_1_);
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(POWERED, context.getWorld()
			.isBlockPowered(context.getPos()));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(POWERED));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
		BlockHelper.addReducedDestroyEffects(state, world, pos, manager);
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isRemote)
			return;
		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos))
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
	}

	public static ItemStack tryInsert(World worldIn, BlockPos pos, ItemStack toInsert, boolean simulate) {
		FilteringBehaviour filter = TileEntityBehaviour.get(worldIn, pos, FilteringBehaviour.TYPE);
		InvManipulationBehaviour inserter = TileEntityBehaviour.get(worldIn, pos, InvManipulationBehaviour.TYPE);
		if (inserter == null)
			return toInsert;
		if (filter != null && !filter.test(toInsert))
			return toInsert;
		if (simulate)
			inserter.simulate();
		ItemStack insert = inserter.insert(toInsert);

		if (!simulate && insert.getCount() != toInsert.getCount()) {
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			if (tileEntity instanceof FunnelTileEntity) {
				FunnelTileEntity funnelTileEntity = (FunnelTileEntity) tileEntity;
				funnelTileEntity.onTransfer(toInsert);
				if (funnelTileEntity.hasFlap())
					funnelTileEntity.flap(true);
			}
		}
		return insert;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.FUNNEL.create();
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		Block block = world.getBlockState(pos.offset(getFunnelFacing(state).getOpposite()))
			.getBlock();
		return !(block instanceof AbstractFunnelBlock);
	}

	@Nullable
	public static boolean isFunnel(BlockState state) {
		return state.getBlock() instanceof AbstractFunnelBlock;
	}

	@Nullable
	public static Direction getFunnelFacing(BlockState state) {
		if (!(state.getBlock() instanceof AbstractFunnelBlock))
			return null;
		return ((AbstractFunnelBlock) state.getBlock()).getFacing(state);
	}

	protected abstract Direction getFacing(BlockState state);

	@Override
	public void onReplaced(BlockState p_196243_1_, World p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity() && (p_196243_1_.getBlock() != p_196243_4_.getBlock() && !isFunnel(p_196243_4_)
			|| !p_196243_4_.hasTileEntity())) {
			TileEntityBehaviour.destroy(p_196243_2_, p_196243_3_, FilteringBehaviour.TYPE);
			p_196243_2_.removeTileEntity(p_196243_3_);
		}
	}

	@Override
	public Class<FunnelTileEntity> getTileEntityClass() {
		return FunnelTileEntity.class;
	}

}
