package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import javax.annotation.Nonnull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CartAssemblerBlockItem extends BlockItem {

	public CartAssemblerBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	@Nonnull
	public ActionResultType onItemUse(ItemUseContext context) {
		if (tryPlaceAssembler(context)) {
			context.getWorld().playSound(null, context.getPos(), SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
			return ActionResultType.SUCCESS;
		}
		return super.onItemUse(context);
	}

	public boolean tryPlaceAssembler(ItemUseContext context) {
		BlockPos pos = context.getPos();
		World world = context.getWorld();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		PlayerEntity player = context.getPlayer();

		if (player == null)
			return false;
		if (!(block instanceof AbstractRailBlock)) {
			Lang.sendStatus(player, "block.cart_assembler.invalid");
			return false;
		}

		RailShape shape = state.get(((AbstractRailBlock) block).getShapeProperty());
		if (shape != RailShape.EAST_WEST && shape != RailShape.NORTH_SOUTH)
			return false;

		BlockState newState = AllBlocks.CART_ASSEMBLER.getDefaultState()
			.with(CartAssemblerBlock.RAIL_SHAPE, shape);
		CartAssembleRailType newType = null;
		for (CartAssembleRailType type : CartAssembleRailType.values())
			if (block == type.railBlock)
				newType = type;
		if (newType == null)
			return false;
		if (world.isRemote)
			return true;

		newState = newState.with(CartAssemblerBlock.RAIL_TYPE, newType);
		world.setBlockState(pos, newState);
		if (!player.isCreative())
			context.getItem().shrink(1);
		return true;
	}
}