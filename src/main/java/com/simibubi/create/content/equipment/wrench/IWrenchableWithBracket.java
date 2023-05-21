package com.simibubi.create.content.equipment.wrench;

import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.FluidPropagator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IWrenchableWithBracket extends IWrenchable {

	public Optional<ItemStack> removeBracket(BlockGetter world, BlockPos pos, boolean inOnReplacedContext);

	@Override
	default InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (tryRemoveBracket(context))
			return InteractionResult.SUCCESS;
		return IWrenchable.super.onWrenched(state, context);
	}

	default boolean tryRemoveBracket(UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Optional<ItemStack> bracket = removeBracket(world, pos, false);
		BlockState blockState = world.getBlockState(pos);
		if (bracket.isPresent()) {
			Player player = context.getPlayer();
			if (!world.isClientSide && !player.isCreative())
				player.getInventory().placeItemBackInInventory(bracket.get());
			if (!world.isClientSide && AllBlocks.FLUID_PIPE.has(blockState)) {
				Axis preferred = FluidPropagator.getStraightPipeAxis(blockState);
				Direction preferredDirection =
					preferred == null ? Direction.UP : Direction.get(AxisDirection.POSITIVE, preferred);
				BlockState updated = AllBlocks.FLUID_PIPE.get()
					.updateBlockState(blockState, preferredDirection, null, world, pos);
				if (updated != blockState)
					world.setBlockAndUpdate(pos, updated);
			}
			return true;
		}
		return false;
	}

}
