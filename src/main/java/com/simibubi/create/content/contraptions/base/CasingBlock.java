package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class CasingBlock extends Block implements IWrenchable {

	public CasingBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		return ActionResultType.FAIL;
	}

	@Override
	public ToolType getHarvestTool(BlockState state) {
		return null;
	}

	@Override
	public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
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
