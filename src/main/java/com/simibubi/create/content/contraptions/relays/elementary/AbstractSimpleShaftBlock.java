package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.Optional;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.wrench.IWrenchableWithBracket;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public abstract class AbstractSimpleShaftBlock extends AbstractShaftBlock implements IWrenchableWithBracket {

	public AbstractSimpleShaftBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return IWrenchableWithBracket.super.onWrenched(state, context);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state != newState && !isMoving)
			removeBracket(world, pos, true).ifPresent(stack -> Block.popResource(world, pos, stack));
		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public Optional<ItemStack> removeBracket(BlockGetter world, BlockPos pos, boolean inOnReplacedContext) {
		BracketedTileEntityBehaviour behaviour = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (behaviour == null)
			return Optional.empty();
		BlockState bracket = behaviour.removeBracket(inOnReplacedContext);
		if (bracket == null)
			return Optional.empty();
		return Optional.of(new ItemStack(bracket.getBlock()));
	}

	@Override
	public BlockEntityType<? extends KineticTileEntity> getTileEntityType() {
		return AllTileEntities.BRACKETED_KINETIC.get();
	}

}
