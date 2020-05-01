package com.simibubi.create.modules.contraptions.components.actors;

import com.simibubi.create.modules.contraptions.components.contraptions.IPortableBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class HarvesterBlock extends AttachedActorBlock implements IPortableBlock {

	public static MovementBehaviour MOVEMENT = new HarvesterMovementBehaviour();

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new HarvesterTileEntity();
	}

//	@Override // TOOD 1.15 register layer
//	public BlockRenderLayer getRenderLayer() {
//		return BlockRenderLayer.CUTOUT;
//	}

	@Override
	public MovementBehaviour getMovementBehaviour() {
		return MOVEMENT;
	}

}
