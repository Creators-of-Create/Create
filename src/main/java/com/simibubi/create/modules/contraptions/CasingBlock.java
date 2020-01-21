package com.simibubi.create.modules.contraptions;

import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.IHaveConnectedTextures;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.ToolType;

public class CasingBlock extends Block implements IHaveConnectedTextures {

	String textureFrom;
	
	public CasingBlock(String textureFrom) {
		super(Properties.from(Blocks.ANDESITE));
		this.textureFrom = textureFrom;
	}

	@Override
	public boolean isToolEffective(BlockState state, ToolType tool) {
		return tool == ToolType.PICKAXE || tool == ToolType.AXE;
	}

	@Override
	public ConnectedTextureBehaviour getBehaviour() {
		return new StandardCTBehaviour(CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, textureFrom, getRegistryName().getPath()));
	}

}
