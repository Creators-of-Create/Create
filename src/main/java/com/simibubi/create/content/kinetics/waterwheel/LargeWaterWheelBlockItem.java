package com.simibubi.create.content.kinetics.waterwheel;

import net.createmod.catnip.api.platform.CatnipServices;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public class LargeWaterWheelBlockItem extends BlockItem {

	public LargeWaterWheelBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public InteractionResult place(BlockPlaceContext ctx) {
		InteractionResult result = super.place(ctx);
		if (result != InteractionResult.FAIL)
			return result;
		Direction clickedFace = ctx.getClickedFace();
		if (clickedFace.getAxis() != ((LargeWaterWheelBlock) getBlock()).getAxisForPlacement(ctx))
			result = super.place(BlockPlaceContext.at(ctx, ctx.getClickedPos()
				.relative(clickedFace), clickedFace));
		if (result == InteractionResult.FAIL && ctx.getLevel()
			.isClientSide())
			CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> LargeWaterWheelClient.showBounds(this, ctx));
		return result;
	}

}
