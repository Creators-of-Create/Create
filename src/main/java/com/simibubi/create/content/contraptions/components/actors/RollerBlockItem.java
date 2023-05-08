package com.simibubi.create.content.contraptions.components.actors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RollerBlockItem extends BlockItem {

	public RollerBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public InteractionResult place(BlockPlaceContext ctx) {
		BlockPos clickedPos = ctx.getClickedPos();
		Level level = ctx.getLevel();
		BlockState blockStateBelow = level.getBlockState(clickedPos.below());
		if (!Block.isFaceFull(blockStateBelow.getCollisionShape(level, clickedPos.below()), Direction.UP))
			return super.place(ctx);
		Direction clickedFace = ctx.getClickedFace();
		return super.place(BlockPlaceContext.at(ctx, clickedPos.relative(Direction.UP), clickedFace));
	}

}
