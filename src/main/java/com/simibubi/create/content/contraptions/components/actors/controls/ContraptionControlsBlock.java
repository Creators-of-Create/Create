package com.simibubi.create.content.contraptions.components.actors.controls;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.ControlsBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ContraptionControlsBlock extends ControlsBlock implements IBE<ContraptionControlsBlockEntity> {

	public ContraptionControlsBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		return onBlockEntityUse(pLevel, pPos, cte -> {
			cte.pressButton();
			if (!pLevel.isClientSide()) {
				cte.disabled = !cte.disabled;
				cte.notifyUpdate();
				ContraptionControlsBlockEntity.sendStatus(pPlayer, cte.filtering.getFilter(), !cte.disabled);
				AllSoundEvents.CONTROLLER_CLICK.play(cte.getLevel(), null, cte.getBlockPos(), 1,
					cte.disabled ? 0.8f : 1.5f);
			}
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
		boolean pIsMoving) {
		withBlockEntityDo(pLevel, pPos, ContraptionControlsBlockEntity::updatePoweredState);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.CONTRAPTION_CONTROLS.get(pState.getValue(FACING));
	}

	@Override
	public Class<ContraptionControlsBlockEntity> getBlockEntityClass() {
		return ContraptionControlsBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ContraptionControlsBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.CONTRAPTION_CONTROLS.get();
	}

}
