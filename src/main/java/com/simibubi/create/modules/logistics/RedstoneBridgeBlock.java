package com.simibubi.create.modules.logistics;

import java.util.List;

import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.ITooltip;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.TooltipHolder;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneBridgeBlock extends ProperDirectionalBlock implements ITooltip {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public static final VoxelShape UP_SHAPE = makeCuboidShape(2, 0, 2, 14, 3, 14),
			DOWN_SHAPE = makeCuboidShape(2, 13, 2, 14, 16, 14);

	public static final VoxelShape SOUTH_SHAPE = makeCuboidShape(3, 1, -1, 13, 15, 2),
			NORTH_SHAPE = makeCuboidShape(3, 1, 14, 13, 15, 17), EAST_SHAPE = makeCuboidShape(-1, 1, 3, 2, 15, 13),
			WEST_SHAPE = makeCuboidShape(14, 1, 3, 17, 15, 13);

	private TooltipHolder info;

	public RedstoneBridgeBlock() {
		super(Properties.from(Blocks.DARK_OAK_LOG));
		info = new TooltipHolder(this);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new RedstoneBridgeTileEntity();
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return state.get(FACING) == Direction.UP;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockPos neighbourPos = pos.offset(state.get(FACING).getOpposite());
		BlockState neighbour = worldIn.getBlockState(neighbourPos);
		return Block.hasSolidSide(neighbour, worldIn, neighbourPos, state.get(FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();
		state = state.with(FACING, context.getFace());
		return state;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction facing = state.get(FACING);

		if (facing == Direction.UP)
			return UP_SHAPE;
		if (facing == Direction.DOWN)
			return DOWN_SHAPE;
		if (facing == Direction.EAST)
			return EAST_SHAPE;
		if (facing == Direction.WEST)
			return WEST_SHAPE;
		if (facing == Direction.NORTH)
			return NORTH_SHAPE;
		if (facing == Direction.SOUTH)
			return SOUTH_SHAPE;

		return VoxelShapes.empty();
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		info.addInformation(tooltip);
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Yellow;
		return new ItemDescription(color)
				.withSummary("Endpoints for " + h("Wireless Redstone", color) + " connections. Can be assigned "
						+ h("Frequencies", color) + " using any item. Signal can travel distances up to "
						+ h("128m", color))
				.withBehaviour("When Powered",
						"Bridges of the same " + h("Frequency", color) + " will provide a Redstone signal.")
				.withControl("When R-Clicked with an Item",
						"Sets the " + h("Frequency", color) + " to that item. A total of "
								+ h("two different items", color)
								+ " can be used in combination for defining a Frequency.")
				.createTabs();
	}

}
