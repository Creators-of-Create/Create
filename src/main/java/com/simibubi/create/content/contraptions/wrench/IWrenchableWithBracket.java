package com.simibubi.create.content.contraptions.wrench;

import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public interface IWrenchableWithBracket extends IWrenchable {

	public Optional<ItemStack> removeBracket(IBlockReader world, BlockPos pos, boolean inOnReplacedContext);

	@Override
	default ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (tryRemoveBracket(context))
			return ActionResultType.SUCCESS;
		return IWrenchable.super.onWrenched(state, context);
	}

	default boolean tryRemoveBracket(ItemUseContext context) {
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Optional<ItemStack> bracket = removeBracket(world, pos, false);
		BlockState blockState = world.getBlockState(pos);
		if (bracket.isPresent()) {
			PlayerEntity player = context.getPlayer();
			if (!world.isClientSide && !player.isCreative())
				player.inventory.placeItemBackInInventory(world, bracket.get());
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
