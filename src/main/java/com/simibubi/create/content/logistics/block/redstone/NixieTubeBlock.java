package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class NixieTubeBlock extends HorizontalBlock implements ITE<NixieTubeTileEntity> {

	public static final BooleanProperty CEILING = BooleanProperty.create("ceiling");

	public NixieTubeBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(CEILING, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(CEILING, HORIZONTAL_FACING));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return (state.get(CEILING) ? AllShapes.NIXIE_TUBE_CEILING : AllShapes.NIXIE_TUBE)
			.get(state.get(HORIZONTAL_FACING)
				.getAxis());
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos pos = context.getPos();
		boolean ceiling = context.getFace() == Direction.DOWN;
		Vector3d hitVec = context.getHitVec();
		if (hitVec != null)
			ceiling = hitVec.y - pos.getY() > .5f;
		return getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing()
			.getOpposite())
			.with(CEILING, ceiling);
	}

	@Override
	public void neighborChanged(BlockState p_220069_1_, World p_220069_2_, BlockPos p_220069_3_, Block p_220069_4_,
		BlockPos p_220069_5_, boolean p_220069_6_) {
		updateDisplayedValue(p_220069_1_, p_220069_2_, p_220069_3_);
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateDisplayedValue(state, worldIn, pos);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new NixieTubeTileEntity(AllTileEntities.NIXIE_TUBE.get());
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	private void updateDisplayedValue(BlockState state, World worldIn, BlockPos pos) {
		if (worldIn.isRemote)
			return;
		int power = getPower(worldIn, pos);
		String display = (power < 10 ? "0" : "") + power;
		withTileEntityDo(worldIn, pos, te -> te.display(display.charAt(0), display.charAt(1)));
	}

	static boolean isValidBlock(IBlockReader world, BlockPos pos, boolean above) {
		BlockState state = world.getBlockState(pos.up(above ? 1 : -1));
		return !state.getShape(world, pos)
			.isEmpty();
	}

	private int getPower(World worldIn, BlockPos pos) {
		int power = 0;
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getRedstonePower(pos.offset(direction), direction), power);
		for (Direction direction : Iterate.directions)
			power = Math.max(worldIn.getRedstonePower(pos.offset(direction), Direction.UP), power);
		return power;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return side != null;
	}

	@Override
	public Class<NixieTubeTileEntity> getTileEntityClass() {
		return NixieTubeTileEntity.class;
	}

}
