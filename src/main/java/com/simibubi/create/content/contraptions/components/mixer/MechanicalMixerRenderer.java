package com.simibubi.create.content.contraptions.components.mixer;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class MechanicalMixerRenderer extends KineticTileEntityRenderer {

	public MechanicalMixerRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRenderOffScreen(KineticTileEntity te) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		BlockState blockState = te.getBlockState();
		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) te;

		VertexConsumer vb = buffer.getBuffer(RenderType.solid());

		SuperByteBuffer superBuffer = CachedBufferer.partial(AllBlockPartials.SHAFTLESS_COGWHEEL, blockState);
		standardKineticRotationTransform(superBuffer, te, light).renderInto(ms, vb);

		float renderedHeadOffset = mixer.getRenderedHeadOffset(partialTicks);
		float speed = mixer.getRenderedHeadRotationSpeed(partialTicks);
		float time = AnimationTickHolder.getRenderTime(te.getLevel());
		float angle = ((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI;

		SuperByteBuffer poleRender = CachedBufferer.partial(AllBlockPartials.MECHANICAL_MIXER_POLE, blockState);
		poleRender.translate(0, -renderedHeadOffset, 0)
				.light(light)
				.renderInto(ms, vb);

		SuperByteBuffer headRender = CachedBufferer.partial(AllBlockPartials.MECHANICAL_MIXER_HEAD, blockState);
		headRender.rotateCentered(Direction.UP, angle)
				.translate(0, -renderedHeadOffset, 0)
				.light(light)
				.renderInto(ms, vb);
	}

}
