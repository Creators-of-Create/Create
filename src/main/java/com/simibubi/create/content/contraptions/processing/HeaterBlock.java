package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HeaterBlock extends Block implements ITE<HeaterTileEntity> {

	public HeaterBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.HEATER.create();
	}

	@Override
	public Class<HeaterTileEntity> getTileEntityClass() {
		return HeaterTileEntity.class;
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult blockRayTraceResult) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof HeaterTileEntity && ((HeaterTileEntity) te).tryUpdateFuel(player.getHeldItem(hand))) {
			if (!player.isCreative())
				player.getHeldItem(hand)
					.shrink(1);
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}
}
