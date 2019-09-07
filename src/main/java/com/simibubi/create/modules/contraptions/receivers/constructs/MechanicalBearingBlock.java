package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;
import com.simibubi.create.modules.contraptions.base.DirectionalKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MechanicalBearingBlock extends DirectionalKineticBlock {

	public MechanicalBearingBlock() {
		super(Properties.from(Blocks.PISTON));
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Red;
		return new ItemDescription(color)
				.withSummary("Rotates attached structures around its axis. Can be used to generate rotational energy.")
				.createTabs();
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MechanicalBearingTileEntity();
	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(FACING).getOpposite();
	}
	
	@Override
	protected boolean hasStaticPart() {
		return true;
	}

}
