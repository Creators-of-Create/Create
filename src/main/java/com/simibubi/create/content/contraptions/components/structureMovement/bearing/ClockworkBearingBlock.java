package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ClockworkBearingBlock extends BearingBlock implements ITE<ClockworkBearingTileEntity> {

	public ClockworkBearingBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return AllTileEntities.CLOCKWORK_BEARING.create(pos, state);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult hit) {
		if (!player.mayBuild())
			return InteractionResult.FAIL;
		if (player.isShiftKeyDown())
			return InteractionResult.FAIL;
		if (player.getItemInHand(handIn).isEmpty()) {
			if (!worldIn.isClientSide) {
				withTileEntityDo(worldIn, pos, te -> {
					if (te.running) {
						te.disassemble();
						return;
					}
					te.assembleNextTick = true;
				});
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public Class<ClockworkBearingTileEntity> getTileEntityClass() {
		return ClockworkBearingTileEntity.class;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		InteractionResult resultType = super.onWrenched(state, context);
		if (!context.getLevel().isClientSide && resultType.consumesAction())
			withTileEntityDo(context.getLevel(), context.getClickedPos(), ClockworkBearingTileEntity::disassemble);
		return resultType;
	}

}
