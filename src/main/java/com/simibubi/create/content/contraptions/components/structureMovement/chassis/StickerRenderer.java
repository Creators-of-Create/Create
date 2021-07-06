package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class StickerRenderer extends SafeTileEntityRenderer<StickerTileEntity> {

	public StickerRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(StickerTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {

		if (Backend.getInstance().canUseInstancing(te.getWorld())) return;

		BlockState state = te.getBlockState();
		SuperByteBuffer head = PartialBufferer.get(AllBlockPartials.STICKER_HEAD, state);
		float offset = te.piston.getValue(AnimationTickHolder.getPartialTicks(te.getWorld()));

		if (te.getWorld() != Minecraft.getInstance().world && !te.isVirtual())
			offset = state.get(StickerBlock.EXTENDED) ? 1 : 0;

		Direction facing = state.get(StickerBlock.FACING);
		head.matrixStacker()
				.nudge(te.hashCode())
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(AngleHelper.verticalAngle(facing) + 90)
			.unCentre()
			.translate(0, (offset * offset) * 4 / 16f, 0);

		head.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

}
