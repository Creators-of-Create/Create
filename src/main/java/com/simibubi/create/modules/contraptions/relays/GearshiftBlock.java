package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;
import com.simibubi.create.modules.contraptions.RotationPropagator;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class GearshiftBlock extends EncasedShaftBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public GearshiftBlock() {
		super();
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new GearshiftTileEntity();
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Red;
		return new ItemDescription(color).withSummary("A controllable rotation switch for connected shafts.")
				.withBehaviour("When Powered", h("Reverses", color) + " the incoming rotation on the other side.").createTabs();
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return super.getStateForPlacement(context).with(POWERED,
				Boolean.valueOf(context.getWorld().isBlockPowered(context.getPos())));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos)) {
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
			if (!previouslyPowered)
				RotationPropagator.handleRemoved(worldIn, pos, (KineticTileEntity) worldIn.getTileEntity(pos));
		}
	}

	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return super.isAxisTowards(world, pos, state, face);
	}

}
