package com.simibubi.create.content.logistics.block.depot;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;

public class EjectorRenderer extends KineticTileEntityRenderer {

	static final Vector3d pivot = VecHelper.voxelSpace(0, 11.25, 0.75);

	public EjectorRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public boolean isGlobalRenderer(KineticTileEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		EjectorTileEntity ejector = (EjectorTileEntity) te;
		IVertexBuilder vertexBuilder = buffer.getBuffer(RenderType.getSolid());
		float lidProgress = ((EjectorTileEntity) te).getLidProgress(partialTicks);
		float angle = lidProgress * 70;

		if (!Backend.getInstance().canUseInstancing(te.getWorld())) {
			SuperByteBuffer model = PartialBufferer.get(AllBlockPartials.EJECTOR_TOP, te.getBlockState());
			applyLidAngle(te, angle, model.matrixStacker());
			model.light(light)
					.renderInto(ms, vertexBuilder);
		}

		MatrixStacker msr = MatrixStacker.of(ms);

		float maxTime =
				(float) (ejector.earlyTarget != null ? ejector.earlyTargetTime : ejector.launcher.getTotalFlyingTicks());
		for (IntAttached<ItemStack> intAttached : ejector.launchedItems) {
			float time = intAttached.getFirst() + partialTicks;
			if (time > maxTime)
				continue;

			ms.push();
			Vector3d launchedItemLocation = ejector.getLaunchedItemLocation(time);
			msr.translate(launchedItemLocation.subtract(Vector3d.of(te.getPos())));
			Vector3d itemRotOffset = VecHelper.voxelSpace(0, 3, 0);
			msr.translate(itemRotOffset);
			msr.rotateY(AngleHelper.horizontalAngle(ejector.getFacing()));
			msr.rotateX(time * 40);
			msr.translateBack(itemRotOffset);
			Minecraft.getInstance()
				.getItemRenderer()
				.renderItem(intAttached.getValue(), TransformType.GROUND, light, overlay, ms, buffer);
			ms.pop();
		}

		DepotBehaviour behaviour = te.getBehaviour(DepotBehaviour.TYPE);
		if (behaviour == null || behaviour.isEmpty())
			return;

		ms.push();
		applyLidAngle(te, angle, msr);
		msr.centre()
			.rotateY(-180 - AngleHelper.horizontalAngle(te.getBlockState()
				.get(EjectorBlock.HORIZONTAL_FACING)))
			.unCentre();
		DepotRenderer.renderItemsOf(te, partialTicks, ms, buffer, light, overlay, behaviour);
		ms.pop();
	}

	static void applyLidAngle(KineticTileEntity te, float angle, MatrixStacker matrixStacker) {
		applyLidAngle(te, pivot, angle, matrixStacker);
	}

	static void applyLidAngle(KineticTileEntity te, Vector3d rotationOffset, float angle, MatrixStacker matrixStacker) {
		matrixStacker.centre()
			.rotateY(180 + AngleHelper.horizontalAngle(te.getBlockState()
				.get(EjectorBlock.HORIZONTAL_FACING)))
			.unCentre()
			.translate(rotationOffset)
			.rotateX(-angle)
			.translateBack(rotationOffset);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

}
