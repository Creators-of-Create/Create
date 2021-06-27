package com.simibubi.create.content.curiosities.bell;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
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
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class AbstractBellRenderer<TE extends AbstractBellTileEntity> extends SafeTileEntityRenderer<TE> {

	public AbstractBellRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(TE te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		BlockState state = te.getBlockState();
		int lightCoords = WorldRenderer.getLightmapCoordinates(te.getWorld(), state, te.getPos());
		IVertexBuilder vb = buffer.getBuffer(RenderType.getCutout());

		SuperByteBuffer bell = PartialBufferer.get(AllBlockPartials.BELL, state);

		float rY = AngleHelper.horizontalAngle(state.get(BellBlock.field_220133_a));
		bell.rotateCentered(Direction.UP, (float) (rY / 180 * Math.PI));

		float ringingTicks = (float)te.ringingTicks + partialTicks;
		if (te.isRinging) {
			float swing = MathHelper.sin(ringingTicks / (float)Math.PI) / (4.0F + ringingTicks / 3.0F);
//			if (te.ringDirection == Direction.NORTH) {
//				rX = -swing;
//			} else if (te.ringDirection == Direction.SOUTH) {
//				rX = swing;
//			} else if (te.ringDirection == Direction.EAST) {
//				rZ = -swing;
//			} else if (te.ringDirection == Direction.WEST) {
//				rZ = swing;
//			}
			bell.rotateCentered(te.ringDirection, swing);
		}

		bell.light(lightCoords).renderInto(ms, vb);
	}

}
