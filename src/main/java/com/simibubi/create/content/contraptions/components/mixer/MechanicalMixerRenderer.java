package com.simibubi.create.content.contraptions.components.mixer;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class MechanicalMixerRenderer extends KineticTileEntityRenderer {

	public MechanicalMixerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public boolean shouldRenderOffScreen(KineticTileEntity te) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {

		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		BlockState blockState = te.getBlockState();
		MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) te;

		IVertexBuilder vb = buffer.getBuffer(RenderType.solid());

		SuperByteBuffer superBuffer = PartialBufferer.get(AllBlockPartials.SHAFTLESS_COGWHEEL, blockState);
		standardKineticRotationTransform(superBuffer, te, light).renderInto(ms, vb);

		float renderedHeadOffset = mixer.getRenderedHeadOffset(partialTicks);
		float speed = mixer.getRenderedHeadRotationSpeed(partialTicks);
		float time = AnimationTickHolder.getRenderTime(te.getLevel());
		float angle = ((time * speed * 6 / 10f) % 360) / 180 * (float) Math.PI;

		SuperByteBuffer poleRender = PartialBufferer.get(AllBlockPartials.MECHANICAL_MIXER_POLE, blockState);
		poleRender.translate(0, -renderedHeadOffset, 0)
				.light(light)
				.renderInto(ms, vb);

		SuperByteBuffer headRender = PartialBufferer.get(AllBlockPartials.MECHANICAL_MIXER_HEAD, blockState);
		headRender.rotateCentered(Direction.UP, angle)
				.translate(0, -renderedHeadOffset, 0)
				.light(light)
				.renderInto(ms, vb);
	}

}
