package com.simibubi.create.content.optics.mirror;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.relays.encased.AbstractEncasedShaftBlock;
import com.simibubi.create.foundation.block.ITE;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MirrorBlock extends AbstractEncasedShaftBlock implements IRotate, ITE<MirrorTileEntity> {
	public MirrorBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.MIRROR.create();
	}


	@Override
	public Class<MirrorTileEntity> getTileEntityClass() {
		return MirrorTileEntity.class;
	}
}
