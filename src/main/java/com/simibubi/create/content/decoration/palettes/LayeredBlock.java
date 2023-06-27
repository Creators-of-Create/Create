package com.simibubi.create.content.decoration.palettes;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LayeredBlock extends RotatedPillarBlock {

	public LayeredBlock(Properties p_55926_) {
		super(p_55926_);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState stateForPlacement = super.getStateForPlacement(pContext);
		BlockState placedOn = pContext.getLevel()
			.getBlockState(pContext.getClickedPos()
				.relative(pContext.getClickedFace()
					.getOpposite()));
		if (placedOn.getBlock() == this && (pContext.getPlayer() == null || !pContext.getPlayer()
			.isSteppingCarefully()))
			stateForPlacement = stateForPlacement.setValue(AXIS, placedOn.getValue(AXIS));
		return stateForPlacement;
	}

}
