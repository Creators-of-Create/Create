package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.common.ToolType;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class CasingBlock extends Block implements IWrenchable {

	public CasingBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.FAIL;
	}

	@Override
	public ToolType getHarvestTool(BlockState state) {
		return null;
	}

	@Override
	public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
		for (ToolType toolType : player.getMainHandItem().getToolTypes()) {
			if (isToolEffective(state, toolType))
				return true;
		}		
		return super.canHarvestBlock(state, world, pos, player);
	}
	
	@Override
	public boolean isToolEffective(BlockState state, ToolType tool) {
		return tool == ToolType.AXE || tool == ToolType.PICKAXE;
	}

}
