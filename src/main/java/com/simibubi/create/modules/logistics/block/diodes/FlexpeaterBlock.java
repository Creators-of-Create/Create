package com.simibubi.create.modules.logistics.block.diodes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IHaveScrollableValue;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class FlexpeaterBlock extends RedstoneDiodeBlock
		implements IWithTileEntity<FlexpeaterTileEntity>, IHaveScrollableValue {

	public static BooleanProperty POWERING = BooleanProperty.create("powering");
	private static Vec3d VALUE_POS = new Vec3d(2 / 16f, 5 / 16f, 5 / 16f);

	public FlexpeaterBlock() {
		super(Properties.from(Blocks.REPEATER));
		setDefaultState(getDefaultState().with(POWERED, false).with(POWERING, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED, POWERING, HORIZONTAL_FACING);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllBlocks.FLEXPEATER.get() == this ? new FlexpeaterTileEntity() : new FlexPulsepeaterTileEntity();
	}

	@Override
	protected int getActiveSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
		return state.get(POWERING) ? 15 : 0;
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(HORIZONTAL_FACING) == side ? this.getActiveSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int getDelay(BlockState p_196346_1_) {
		return 0;
	}
	
	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.get(HORIZONTAL_FACING).getAxis();
	}

	@Override
	public int getCurrentValue(BlockState state, IWorld world, BlockPos pos) {
		FlexpeaterTileEntity te = (FlexpeaterTileEntity) world.getTileEntity(pos);
		if (te == null)
			return 0;
		return te.getDisplayValue();
	}

	@Override
	public void onScroll(BlockState state, IWorld world, BlockPos pos, double delta) {
		withTileEntityDo(world, pos, te -> te.increment((int) Math.signum(delta)));
	}

	@Override
	public String getValueName(BlockState state, IWorld world, BlockPos pos) {
		FlexpeaterTileEntity te = (FlexpeaterTileEntity) world.getTileEntity(pos);
		if (te == null)
			return "";
		return Lang.translate("generic.delay") + " (" + Lang.translate("generic.unit." + te.getUnit()) + ")";
	}
	
	@Override
	public String getValueSuffix(BlockState state, IWorld world, BlockPos pos) {
		FlexpeaterTileEntity te = (FlexpeaterTileEntity) world.getTileEntity(pos);
		if (te == null)
			return "";
		return "" + te.getUnit().charAt(0);
	}

	@Override
	public Vec3d getValueBoxPosition(BlockState state, IWorld world, BlockPos pos) {
		return VALUE_POS;
	}

	@Override
	public Direction getValueBoxDirection(BlockState state, IWorld world, BlockPos pos) {
		return Direction.UP;
	}

}
