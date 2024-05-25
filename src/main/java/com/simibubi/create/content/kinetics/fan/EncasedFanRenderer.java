package com.simibubi.create.content.kinetics.fan;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class EncasedFanRenderer extends KineticBlockEntityRenderer<EncasedFanBlockEntity> {

	public EncasedFanRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(EncasedFanBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (VisualizationManager.supportsVisualization(be.getLevel())) return;

		Direction direction = be.getBlockState()
				.getValue(FACING);
		VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

		int lightBehind = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(direction.getOpposite()));
		int lightInFront = LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos().relative(direction));

		SuperByteBuffer shaftHalf =
				CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), direction.getOpposite());
		SuperByteBuffer fanInner =
				CachedBufferer.partialFacing(AllPartialModels.ENCASED_FAN_INNER, be.getBlockState(), direction.getOpposite());

		float time = AnimationTickHolder.getRenderTime(be.getLevel());
		float speed = be.getSpeed() * 5;
		if (speed > 0)
			speed = Mth.clamp(speed, 80, 64 * 20);
		if (speed < 0)
			speed = Mth.clamp(speed, -64 * 20, -80);
		float angle = (time * speed * 3 / 10f) % 360;
		angle = angle / 180f * (float) Math.PI;

		standardKineticRotationTransform(shaftHalf, be, lightBehind).renderInto(ms, vb);
		kineticRotationTransform(fanInner, be, direction.getAxis(), angle, lightInFront).renderInto(ms, vb);
	}

}
