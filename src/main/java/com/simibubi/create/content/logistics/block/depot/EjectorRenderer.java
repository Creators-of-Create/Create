package com.simibubi.create.content.logistics.block.depot;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.util.transform.Rotate;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.jozufozu.flywheel.util.transform.Translate;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftRenderer;
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
import net.minecraft.world.phys.Vec3;

public class EjectorRenderer extends ShaftRenderer<EjectorBlockEntity> {

	static final Vec3 pivot = VecHelper.voxelSpace(0, 11.25, 0.75);

	public EjectorRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRenderOffScreen(EjectorBlockEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(EjectorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.solid());
		float lidProgress = be.getLidProgress(partialTicks);
		float angle = lidProgress * 70;

		if (!Backend.canUseInstancing(be.getLevel())) {
			SuperByteBuffer model = CachedBufferer.partial(AllPartialModels.EJECTOR_TOP, be.getBlockState());
			applyLidAngle(be, angle, model);
			model.light(light)
					.renderInto(ms, vertexBuilder);
		}

		TransformStack msr = TransformStack.cast(ms);

		float maxTime =
				(float) (be.earlyTarget != null ? be.earlyTargetTime : be.launcher.getTotalFlyingTicks());
		for (IntAttached<ItemStack> intAttached : be.launchedItems) {
			float time = intAttached.getFirst() + partialTicks;
			if (time > maxTime)
				continue;

			ms.pushPose();
			Vec3 launchedItemLocation = be.getLaunchedItemLocation(time);
			msr.translate(launchedItemLocation.subtract(Vec3.atLowerCornerOf(be.getBlockPos())));
			Vec3 itemRotOffset = VecHelper.voxelSpace(0, 3, 0);
			msr.translate(itemRotOffset);
			msr.rotateY(AngleHelper.horizontalAngle(be.getFacing()));
			msr.rotateX(time * 40);
			msr.translateBack(itemRotOffset);
			Minecraft.getInstance()
				.getItemRenderer()
				.renderStatic(intAttached.getValue(), TransformType.GROUND, light, overlay, ms, buffer, 0);
			ms.popPose();
		}

		DepotBehaviour behaviour = be.getBehaviour(DepotBehaviour.TYPE);
		if (behaviour == null || behaviour.isEmpty())
			return;

		ms.pushPose();
		applyLidAngle(be, angle, msr);
		msr.centre()
			.rotateY(-180 - AngleHelper.horizontalAngle(be.getBlockState()
				.getValue(EjectorBlock.HORIZONTAL_FACING)))
			.unCentre();
		DepotRenderer.renderItemsOf(be, partialTicks, ms, buffer, light, overlay, behaviour);
		ms.popPose();
	}

	static <T extends Translate<T> & Rotate<T>> void applyLidAngle(KineticBlockEntity be, float angle, T tr) {
		applyLidAngle(be, pivot, angle, tr);
	}

	static <T extends Translate<T> & Rotate<T>> void applyLidAngle(KineticBlockEntity be, Vec3 rotationOffset, float angle, T tr) {
		tr.centre()
			.rotateY(180 + AngleHelper.horizontalAngle(be.getBlockState()
				.getValue(EjectorBlock.HORIZONTAL_FACING)))
			.unCentre()
			.translate(rotationOffset)
			.rotateX(-angle)
			.translateBack(rotationOffset);
	}

}
