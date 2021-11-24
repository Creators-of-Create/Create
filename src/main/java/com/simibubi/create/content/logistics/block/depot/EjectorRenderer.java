package com.simibubi.create.content.logistics.block.depot;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EjectorRenderer extends KineticTileEntityRenderer {

	static final Vec3 pivot = VecHelper.voxelSpace(0, 11.25, 0.75);

	public EjectorRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRenderOffScreen(KineticTileEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		EjectorTileEntity ejector = (EjectorTileEntity) te;
		VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.solid());
		float lidProgress = ((EjectorTileEntity) te).getLidProgress(partialTicks);
		float angle = lidProgress * 70;

		if (!Backend.getInstance().canUseInstancing(te.getLevel())) {
			SuperByteBuffer model = CachedBufferer.partial(AllBlockPartials.EJECTOR_TOP, te.getBlockState());
			applyLidAngle(te, angle, model.matrixStacker());
			model.light(light)
					.renderInto(ms, vertexBuilder);
		}

		MatrixTransformStack msr = (MatrixTransformStack) TransformStack.cast(ms);

		float maxTime =
				(float) (ejector.earlyTarget != null ? ejector.earlyTargetTime : ejector.launcher.getTotalFlyingTicks());
		for (IntAttached<ItemStack> intAttached : ejector.launchedItems) {
			float time = intAttached.getFirst() + partialTicks;
			if (time > maxTime)
				continue;

			ms.pushPose();
			Vec3 launchedItemLocation = ejector.getLaunchedItemLocation(time);
			msr.translate(launchedItemLocation.subtract(Vec3.atLowerCornerOf(te.getBlockPos())));
			Vec3 itemRotOffset = VecHelper.voxelSpace(0, 3, 0);
			msr.translate(itemRotOffset);
			msr.rotateY(AngleHelper.horizontalAngle(ejector.getFacing()));
			msr.rotateX(time * 40);
			msr.translateBack(itemRotOffset);
			Minecraft.getInstance()
				.getItemRenderer()
				.renderStatic(intAttached.getValue(), TransformType.GROUND, light, overlay, ms, buffer, 0);
			ms.popPose();
		}

		DepotBehaviour behaviour = te.getBehaviour(DepotBehaviour.TYPE);
		if (behaviour == null || behaviour.isEmpty())
			return;

		ms.pushPose();
		applyLidAngle(te, angle, msr);
		msr.centre()
			.rotateY(-180 - AngleHelper.horizontalAngle(te.getBlockState()
				.getValue(EjectorBlock.HORIZONTAL_FACING)))
			.unCentre();
		DepotRenderer.renderItemsOf(te, partialTicks, ms, buffer, light, overlay, behaviour);
		ms.popPose();
	}

	static void applyLidAngle(KineticTileEntity te, float angle, TransformStack matrixStacker) {
		applyLidAngle(te, pivot, angle, matrixStacker);
	}

	static void applyLidAngle(KineticTileEntity te, Vec3 rotationOffset, float angle, TransformStack matrixStacker) {
		matrixStacker.centre()
			.rotateY(180 + AngleHelper.horizontalAngle(te.getBlockState()
				.getValue(EjectorBlock.HORIZONTAL_FACING)))
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
