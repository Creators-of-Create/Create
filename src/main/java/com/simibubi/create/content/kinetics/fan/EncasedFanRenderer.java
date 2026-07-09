package com.simibubi.create.content.kinetics.fan;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
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
		VertexConsumer vb = buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.cutoutMipped());

		int lightBehind = com.simibubi.create.foundation.render.LegacyLightTexture.getLightColor(be.getLevel(), be.getBlockPos().relative(direction.getOpposite()));
		int lightInFront = com.simibubi.create.foundation.render.LegacyLightTexture.getLightColor(be.getLevel(), be.getBlockPos().relative(direction));

		SuperByteBuffer shaftHalf =
				CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), direction.getOpposite());
		SuperByteBuffer fanInner =
				CachedBuffers.partialFacing(AllPartialModels.ENCASED_FAN_INNER, be.getBlockState(), direction.getOpposite());

		float time = AnimationTickHolder.getRenderTime();
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
