package com.simibubi.create.content.contraptions.components.fan;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.net.minimport com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

ecraft.world.level.block.state.properties.BlockStatePropertiesng.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class EncasedFanRenderer extends KineticTileEntityRenderer {

	public EncasedFanRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		Direction direction = te.getBlockState()
				.getValue(FACING);
		VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

		int lightBehind = LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().relative(direction.getOpposite()));
		int lightInFront = LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().relative(direction));

		SuperByteBuffer shaftHalf =
				PartialBufferer.getFacing(AllBlockPartials.SHAFT_HALF, te.getBlockState(), direction.getOpposite());
		SuperByteBuffer fanInner =
				PartialBufferer.getFacing(AllBlockPartials.ENCASED_FAN_INNER, te.getBlockState(), direction.getOpposite());

		float time = AnimationTickHolder.getRenderTime(te.getLevel());
		float speed = te.getSpeed() * 5;
		if (speed > 0)
			speed = Mth.clamp(speed, 80, 64 * 20);
		if (speed < 0)
			speed = Mth.clamp(speed, -64 * 20, -80);
		float angle = (time * speed * 3 / 10f) % 360;
		angle = angle / 180f * (float) Math.PI;

		standardKineticRotationTransform(shaftHalf, te, lightBehind).renderInto(ms, vb);
		kineticRotationTransform(fanInner, te, direction.getAxis(), angle, lightInFront).renderInto(ms, vb);
	}

}
