package com.simibubi.create.modules.contraptions.receivers.constructs;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;

public class ChassisBlock extends RotatedPillarBlock {

	public Type type;
	
	public enum Type {
		NORMAL, STICKY, RELOCATING;
	}
	
	public ChassisBlock(Type type) {
		super(Properties.from(Blocks.STRIPPED_SPRUCE_WOOD));
		this.type = type;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos placedOnPos = context.getPos().offset(context.getFace().getOpposite());
		BlockState blockState = context.getWorld().getBlockState(placedOnPos);
		if (blockState.getBlock() instanceof ChassisBlock)
			return getDefaultState().with(AXIS, blockState.get(AXIS));
		return super.getStateForPlacement(context);
	}

}
