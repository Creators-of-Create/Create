package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import javax.annotation.Nonnull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.tracks.ControllerRailBlock;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
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
	public ActionResultType useOn(ItemUseContext context) {
		if (tryPlaceAssembler(context)) {
			context.getLevel()
				.playSound(null, context.getClickedPos(), SoundEvents.STONE_PLACE, SoundCategory.BLOCKS, 1, 1);
			return ActionResultType.SUCCESS;
		}
		return super.useOn(context);
	}

	public boolean tryPlaceAssembler(ItemUseContext context) {
		BlockPos pos = context.getClickedPos();
		World world = context.getLevel();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		PlayerEntity player = context.getPlayer();

		if (player == null)
			return false;
		if (!(block instanceof AbstractRailBlock)) {
			Lang.sendStatus(player, "block.cart_assembler.invalid");
			return false;
		}

		RailShape shape = state.getValue(((AbstractRailBlock) block).getShapeProperty());
		if (shape != RailShape.EAST_WEST && shape != RailShape.NORTH_SOUTH)
			return false;

		BlockState newState = AllBlocks.CART_ASSEMBLER.getDefaultState()
			.setValue(CartAssemblerBlock.RAIL_SHAPE, shape);
		CartAssembleRailType newType = null;
		for (CartAssembleRailType type : CartAssembleRailType.values())
			if (type.matches(state))
				newType = type;
		if (newType == null)
			return false;
		if (world.isClientSide)
			return true;

		newState = newState.setValue(CartAssemblerBlock.RAIL_TYPE, newType);
		if (state.hasProperty(ControllerRailBlock.BACKWARDS))
			newState = newState.setValue(CartAssemblerBlock.BACKWARDS, state.getValue(ControllerRailBlock.BACKWARDS));
		else {
			Direction direction = player.getMotionDirection();
			newState =
				newState.setValue(CartAssemblerBlock.BACKWARDS, direction.getAxisDirection() == AxisDirection.POSITIVE);
		}

		world.setBlockAndUpdate(pos, newState);
		if (!player.isCreative())
			context.getItemInHand()
				.shrink(1);
		return true;
	}
}