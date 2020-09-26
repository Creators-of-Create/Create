package com.simibubi.create.content.contraptions.components.actors;

import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class PortableStorageInterfaceRenderer extends SafeTileEntityRenderer<PortableStorageInterfaceTileEntity> {

	public PortableStorageInterfaceRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(PortableStorageInterfaceTileEntity te, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		BlockState blockState = te.getBlockState();
		float progress = te.getExtensionDistance(partialTicks);
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		render(blockState, progress, te.isConnected(), sbb -> sbb.light(light).renderInto(ms, vb), ms);
	}

	public static void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
		IRenderTypeBuffer buffer) {
		BlockState blockState = context.state;
		PortableStorageInterfaceTileEntity te = getTargetPSI(context);
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());
		float renderPartialTicks = Minecraft.getInstance()
			.getRenderPartialTicks();
		
		float progress = 0;
		boolean lit = false;
		if (te != null) {
			progress = te.getExtensionDistance(renderPartialTicks);
			lit = te.isConnected();
		}
		
		render(blockState, progress, lit, sbb -> sbb.light(msLocal.peek()
			.getModel())
			.renderInto(ms, vb), ms, msLocal);
	}

	protected static PortableStorageInterfaceTileEntity getTargetPSI(MovementContext context) {
		String _workingPos_ = PortableStorageInterfaceMovement._workingPos_;
		if (!context.contraption.stalled || !context.data.contains(_workingPos_))
			return null;

		BlockPos pos = NBTUtil.readBlockPos(context.data.getCompound(_workingPos_));
		TileEntity tileEntity = context.world.getTileEntity(pos);
		if (!(tileEntity instanceof PortableStorageInterfaceTileEntity))
			return null;

		PortableStorageInterfaceTileEntity psi = (PortableStorageInterfaceTileEntity) tileEntity;
		if (!psi.isTransferring())
			return null;
		return psi;
	}

	private static void render(BlockState blockState, float progress, boolean lit,
		Consumer<SuperByteBuffer> drawCallback, MatrixStack... matrixStacks) {
		for (MatrixStack ms : matrixStacks)
			ms.push();

		SuperByteBuffer middle = AllBlockPartials.PORTABLE_STORAGE_INTERFACE_MIDDLE.renderOn(blockState);
		SuperByteBuffer top = AllBlockPartials.PORTABLE_STORAGE_INTERFACE_TOP.renderOn(blockState);
		if (lit)
			middle = AllBlockPartials.PORTABLE_STORAGE_INTERFACE_MIDDLE_POWERED.renderOn(blockState);

		Direction facing = blockState.get(PortableStorageInterfaceBlock.FACING);
		for (MatrixStack ms : matrixStacks)
			MatrixStacker.of(ms)
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
				.unCentre();

		for (MatrixStack ms : matrixStacks) {
			ms.translate(0, progress / 2f, 0);
			ms.push();
			ms.translate(0, 6 / 16f, 0);
		}

		drawCallback.accept(middle);

		for (MatrixStack ms : matrixStacks) {
			ms.pop();
			ms.translate(0, progress / 2f, 0);
		}

		drawCallback.accept(top);

		for (MatrixStack ms : matrixStacks)
			ms.pop();
	}

}
