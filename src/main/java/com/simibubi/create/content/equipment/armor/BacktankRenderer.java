package com.simibubi.create.content.equipment.armor;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedPartialBuffers;

import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.math.AngleHelper;
import net.createmod.ponder.utility.WorldTickHolder;
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
		SuperByteBuffer cogs = CachedPartialBuffers.partial(getCogsModel(blockState), blockState);
		cogs.centre()
			.rotateY(180 + AngleHelper.horizontalAngle(blockState.getValue(BacktankBlock.HORIZONTAL_FACING)))
			.unCentre()
			.translate(0, 6.5f / 16, 11f / 16)
			.rotate(Direction.EAST,
				AngleHelper.rad(be.getSpeed() / 4f * WorldTickHolder.getRenderTime(be.getLevel()) % 360))
			.translate(0, -6.5f / 16, -11f / 16);
		cogs.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	@Override
	protected SuperByteBuffer getRotatedModel(BacktankBlockEntity be, BlockState state) {
		return CachedPartialBuffers.partial(getShaftModel(state), state);
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
