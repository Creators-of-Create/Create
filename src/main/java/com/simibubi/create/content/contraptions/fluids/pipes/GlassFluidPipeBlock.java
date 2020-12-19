package com.simibubi.create.content.contraptions.fluids.pipes;

import com.simibubi.create.AllTileEntities;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GlassFluidPipeBlock extends AxisPipeBlock implements IWaterLoggable {

	public static final BooleanProperty ALT = BooleanProperty.create("alt");

	public GlassFluidPipeBlock(Properties p_i48339_1_) {
		super(p_i48339_1_);
		setDefaultState(getDefaultState().with(ALT, false).with(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> p_206840_1_) {
		super.fillStateContainer(p_206840_1_.add(ALT, BlockStateProperties.WATERLOGGED));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.GLASS_FLUID_PIPE.create();
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (tryRemoveBracket(context))
			return ActionResultType.SUCCESS;
		BlockState newState;
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		newState = toRegularPipe(world, pos, state).with(BlockStateProperties.WATERLOGGED, state.get(BlockStateProperties.WATERLOGGED));
		world.setBlockState(pos, newState, 3);
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		IFluidState ifluidstate = context.getWorld()
			.getFluidState(context.getPos());
		BlockState state = super.getStateForPlacement(context);
		return state == null ? null : state.with(BlockStateProperties.WATERLOGGED,
			ifluidstate.getFluid() == Fluids.WATER);
	}

	@Override
	public IFluidState getFluidState(BlockState state) {
		return state.get(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false)
			: Fluids.EMPTY.getDefaultState();
	}
}
