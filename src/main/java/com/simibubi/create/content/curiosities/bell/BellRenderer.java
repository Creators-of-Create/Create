package com.simibubi.create.content.curiosities.bell;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BellAttachment;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class BellRenderer<TE extends AbstractBellTileEntity> extends SafeTileEntityRenderer<TE> {

	public BellRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(TE te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		BlockState state = te.getBlockState();
		Direction facing = state.get(BellBlock.field_220133_a);
		BellAttachment attachment = state.get(BellBlock.field_220134_b);

		SuperByteBuffer bell = PartialBufferer.get(te.getBellPartial(), state);

		if (te.isRinging)
			bell.rotateCentered(te.ringDirection.rotateYCCW(), getSwingAngle(te.ringingTicks + partialTicks));

		float rY = AngleHelper.horizontalAngle(facing);
		if (attachment == BellAttachment.SINGLE_WALL || attachment == BellAttachment.DOUBLE_WALL)
			rY += 90;
		bell.rotateCentered(Direction.UP, (float) (rY / 180 * Math.PI));

		IVertexBuilder vb = buffer.getBuffer(RenderType.getCutout());
		int lightCoords = WorldRenderer.getLightmapCoordinates(te.getWorld(), state, te.getPos());
		bell.light(lightCoords).renderInto(ms, vb);
	}

	public static float getSwingAngle(float time) {
		float t = time / 1.5f;
		return 1.2f * MathHelper.sin(t / (float)Math.PI) / (2.5f + t / 3.0f);
	}

}
