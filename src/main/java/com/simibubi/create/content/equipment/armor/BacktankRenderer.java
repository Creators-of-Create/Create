package com.simibubi.create.content.equipment.armor;

import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BacktankRenderer extends KineticBlockEntityRenderer<BacktankBlockEntity> {
	public BacktankRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(BacktankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		BlockState blockState = be.getBlockState();
		SuperByteBuffer cogs = CachedBufferer.partial(getCogsModel(blockState), blockState);
		cogs.center()
			.rotateY(180 + AngleHelper.horizontalAngle(blockState.getValue(BacktankBlock.HORIZONTAL_FACING)))
			.uncenter()
			.translate(0, 6.5f / 16, 11f / 16)
			.rotate(AngleHelper.rad(be.getSpeed() / 4f * AnimationTickHolder.getRenderTime(be.getLevel()) % 360),
				Direction.EAST)
			.translate(0, -6.5f / 16, -11f / 16);
		cogs.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	@Override
	protected SuperByteBuffer getRotatedModel(BacktankBlockEntity be, BlockState state) {
		return CachedBufferer.partial(getShaftModel(state), state);
	}

	public static PartialModel getCogsModel(BlockState state) {
		if (AllBlocks.NETHERITE_BACKTANK.has(state)) {
			return AllPartialModels.NETHERITE_BACKTANK_COGS;
		}
		return AllPartialModels.COPPER_BACKTANK_COGS;
	}

	public static PartialModel getShaftModel(BlockState state) {
		if (AllBlocks.NETHERITE_BACKTANK.has(state)) {
			return AllPartialModels.NETHERITE_BACKTANK_SHAFT;
		}
		return AllPartialModels.COPPER_BACKTANK_SHAFT;
	}
}
