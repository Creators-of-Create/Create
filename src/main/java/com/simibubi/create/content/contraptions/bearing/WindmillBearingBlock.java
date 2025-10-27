package com.simibubi.create.content.contraptions.bearing;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WindmillBearingBlock extends BearingBlock implements IBE<WindmillBearingBlockEntity> {

	public WindmillBearingBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!player.mayBuild())
			return ItemInteractionResult.FAIL;
		if (player.isShiftKeyDown())
			return ItemInteractionResult.FAIL;
		if (stack.isEmpty()) {
			if (level.isClientSide)
				return ItemInteractionResult.SUCCESS;
			withBlockEntityDo(level, pos, be -> {
				if (be.running) {
					be.disassemble();
					return;
				}
				be.assembleNextTick = true;
			});
			return ItemInteractionResult.SUCCESS;
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public Class<WindmillBearingBlockEntity> getBlockEntityClass() {
		return WindmillBearingBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends WindmillBearingBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.WINDMILL_BEARING.get();
	}
}
