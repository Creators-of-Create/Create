package com.simibubi.create.content.contraptions.components.flywheel.engine;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class EngineRenderer<T extends EngineTileEntity> extends SafeTileEntityRenderer<T> {

	public EngineRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(T te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light,
		int overlay) {

		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		Block block = te.getBlockState()
				.getBlock();
		if (block instanceof EngineBlock) {
			EngineBlock engineBlock = (EngineBlock) block;
			PartialModel frame = engineBlock.getFrameModel();
			if (frame != null) {
				Direction facing = te.getBlockState()
						.getValue(EngineBlock.FACING);
				float angle = AngleHelper.rad(AngleHelper.horizontalAngle(facing));
				PartialBufferer.get(frame, te.getBlockState())
						.rotateCentered(Direction.UP, angle)
						.translate(0, 0, -1)
						.light(light)
						.renderInto(ms, buffer.getBuffer(RenderType.solid()));
			}
		}
	}

}
