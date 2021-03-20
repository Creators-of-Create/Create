package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public abstract class BearingBlock extends DirectionalKineticBlock {

	public BearingBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(FACING).getOpposite();
	}
	
	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING).getAxis();
	}

	@Override
	public boolean showCapacityWithAnnotation() {
		return true;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		ActionResultType resultType = super.onWrenched(state, context);
		if (!context.getWorld().isRemote && resultType.isAccepted()) {
			TileEntity te = context.getWorld().getTileEntity(context.getPos());
			if (te instanceof MechanicalBearingTileEntity) {
				((MechanicalBearingTileEntity) te).disassemble();
			}
		}
		return resultType;
	}
}
