package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ClockworkBearingBlock extends BearingBlock implements IBE<ClockworkBearingBlockEntity> {

	public ClockworkBearingBlock(Properties properties) {
		super(properties);
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
				withBlockEntityDo(worldIn, pos, be -> {
					if (be.running) {
						be.disassemble();
						return;
					}
					be.assembleNextTick = true;
				});
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public Class<ClockworkBearingBlockEntity> getBlockEntityClass() {
		return ClockworkBearingBlockEntity.class;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		InteractionResult resultType = super.onWrenched(state, context);
		if (!context.getLevel().isClientSide && resultType.consumesAction())
			withBlockEntityDo(context.getLevel(), context.getClickedPos(), ClockworkBearingBlockEntity::disassemble);
		return resultType;
	}

	@Override
	public BlockEntityType<? extends ClockworkBearingBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CLOCKWORK_BEARING.get();
	}

}
