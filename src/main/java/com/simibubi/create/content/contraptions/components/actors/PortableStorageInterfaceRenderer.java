package com.simibubi.create.content.contraptions.components.actors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;

public class PortableStorageInterfaceRenderer extends SafeTileEntityRenderer<PortableStorageInterfaceTileEntity> {

	public PortableStorageInterfaceRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(PortableStorageInterfaceTileEntity te, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {

		BlockState blockState = te.getBlockState();
		SuperByteBuffer middle = AllBlockPartials.PORTABLE_STORAGE_INTERFACE_MIDDLE.renderOn(blockState);
		SuperByteBuffer top = AllBlockPartials.PORTABLE_STORAGE_INTERFACE_TOP.renderOn(blockState);
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());

		ms.push();

		Direction facing = blockState.get(PortableStorageInterfaceBlock.FACING);
		MatrixStacker.of(ms)
			.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
			.unCentre();

		float progress = (float) ((AnimationTickHolder.getRenderTick() * .25f) % (Math.PI * 2));
		float bounce = (MathHelper.sin(progress) + 1) / 4f;
		
		if (bounce > 7/16f) {
			middle = AllBlockPartials.PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED.renderOn(blockState);
		}
		

		ms.translate(0, bounce, 0);
		
		ms.push();
		ms.translate(0, 6/16f, 0);
		middle.renderInto(ms, vb);
		ms.pop();
		
		ms.translate(0, bounce, 0);
		top.renderInto(ms, vb);

		ms.pop();
	}

}
