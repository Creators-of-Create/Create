package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ClockworkBearingBlock extends BearingBlock implements ITE<ClockworkBearingTileEntity> {

	public ClockworkBearingBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.CLOCKWORK_BEARING.create();
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos,
			PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (!player.isAllowEdit())
			return ActionResultType.FAIL;
		if (player.isSneaking())
			return ActionResultType.FAIL;
		if (player.getHeldItem(handIn).isEmpty()) {
			if (!worldIn.isRemote) {
				withTileEntityDo(worldIn, pos, te -> {
					if (te.running) {
						te.disassemble();
						return;
					}
					te.assembleNextTick = true;
				});
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	@Override
	public Class<ClockworkBearingTileEntity> getTileEntityClass() {
		return ClockworkBearingTileEntity.class;
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		ActionResultType resultType = super.onWrenched(state, context);
		if (!context.getWorld().isRemote && resultType.isAccepted())
			withTileEntityDo(context.getWorld(), context.getPos(), ClockworkBearingTileEntity::disassemble);
		return resultType;
	}

}
