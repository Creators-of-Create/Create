package com.simibubi.create.lib.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface BlockEntityExtensions {
	CompoundTag create$getExtraCustomData();

	void create$deserializeNBT(BlockState state, CompoundTag nbt);

	AABB INFINITE_EXTENT_AABB = new net.minecraft.world.phys.AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

	//todo: hook this into rendering if needed?
	default AABB create$getRenderBoundingBox()
	{
		AABB bb = INFINITE_EXTENT_AABB;
		BlockState state = ((BlockEntity)this).getBlockState();
		Block block = state.getBlock();
		BlockPos pos = ((BlockEntity)this).getBlockPos();
		if (block == Blocks.ENCHANTING_TABLE)
		{
			bb = new AABB(pos, pos.offset(1, 1, 1));
		}
		else if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST)
		{
			bb = new AABB(pos.offset(-1, 0, -1), pos.offset(2, 2, 2));
		}
		else if (block == Blocks.STRUCTURE_BLOCK)
		{
			bb = INFINITE_EXTENT_AABB;
		}
		else if (block != null && block != Blocks.BEACON)
		{
			AABB cbb = null;
			try
			{
				VoxelShape collisionShape = state.getCollisionShape(((BlockEntity)this).getLevel(), pos);
				if (!collisionShape.isEmpty())
				{
					cbb = collisionShape.bounds().move(pos);
				}
			}
			catch (Exception e)
			{
				// We have to capture any exceptions that may occur here because BUKKIT servers like to send
				// the tile entity data BEFORE the chunk data, you know, the OPPOSITE of what vanilla does!
				// So we can not GARENTEE that the world state is the real state for the block...
				// So, once again in the long line of US having to accommodate BUKKIT breaking things,
				// here it is, assume that the TE is only 1 cubic block. Problem with this is that it may
				// cause the TileEntity renderer to error further down the line! But alas, nothing we can do.
				cbb = new net.minecraft.world.phys.AABB(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
			}
			if (cbb != null) bb = cbb;
		}
		return bb;
	}
}
