package com.simibubi.create.foundation.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.fml.network.NetworkHooks;

public interface IWithContainerTileEntity<T extends TileEntity & IWithContainer<T, ?>> extends ITE<T> {

	default void open(IWorld world, BlockPos pos, PlayerEntity player) {
		if (world.isRemote())
			return;
		try {
			T te = getTileEntity(world, pos);
			NetworkHooks.openGui((ServerPlayerEntity) player, te, te::sendToContainer);
		} catch (TileEntityException e) {}
	}

}
