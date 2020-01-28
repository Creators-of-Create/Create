package com.simibubi.create.modules.contraptions;

import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.IHaveConnectedTextures;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class CasingBlock extends Block implements IHaveConnectedTextures {

	String textureFrom;
	
	public CasingBlock(String textureFrom) {
		super(Properties.from(Blocks.ANDESITE));
		this.textureFrom = textureFrom;
	}

	@Override
	public ToolType getHarvestTool(BlockState state) {
		return null;
	}

	@Override
	public boolean canHarvestBlock(BlockState state, IBlockReader world, BlockPos pos, PlayerEntity player) {
		for (ToolType toolType : player.getHeldItemMainhand().getToolTypes()) {
			if (isToolEffective(state, toolType))
				return true;
		}		
		return super.canHarvestBlock(state, world, pos, player);
	}
	
	@Override
	public boolean isToolEffective(BlockState state, ToolType tool) {
		return tool == ToolType.AXE || tool == ToolType.PICKAXE;
	}

	@Override
	public ConnectedTextureBehaviour getBehaviour() {
		return new StandardCTBehaviour(CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, textureFrom, getRegistryName().getPath()));
	}

}
