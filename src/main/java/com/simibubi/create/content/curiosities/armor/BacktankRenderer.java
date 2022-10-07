package com.simibubi.create.content.curiosities.armor;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BacktankRenderer extends KineticTileEntityRenderer {
	public BacktankRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		BlockState blockState = te.getBlockState();
		SuperByteBuffer cogs = CachedBufferer.partial(getCogsModel(blockState), blockState);
		cogs.centre()
			.rotateY(180 + AngleHelper.horizontalAngle(blockState.getValue(BacktankBlock.HORIZONTAL_FACING)))
			.unCentre()
			.translate(0, 6.5f / 16, 11f / 16)
			.rotate(Direction.EAST,
				AngleHelper.rad(te.getSpeed() / 4f * AnimationTickHolder.getRenderTime(te.getLevel()) % 360))
			.translate(0, -6.5f / 16, -11f / 16);
		cogs.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te, BlockState state) {
		return CachedBufferer.partial(getShaftModel(state), state);
	}

	public static PartialModel getCogsModel(BlockState state) {
		if (AllBlocks.NETHERITE_BACKTANK.has(state)) {
			return AllBlockPartials.NETHERITE_BACKTANK_COGS;
		}
		return AllBlockPartials.COPPER_BACKTANK_COGS;
	}

	public static PartialModel getShaftModel(BlockState state) {
		if (AllBlocks.NETHERITE_BACKTANK.has(state)) {
			return AllBlockPartials.NETHERITE_BACKTANK_SHAFT;
		}
		return AllBlockPartials.COPPER_BACKTANK_SHAFT;
	}
}
