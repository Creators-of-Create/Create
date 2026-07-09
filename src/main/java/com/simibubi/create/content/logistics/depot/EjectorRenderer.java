package com.simibubi.create.content.logistics.depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.content.logistics.box.PackageItem;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.transform.Rotate;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.transform.Translate;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.data.IntAttached;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class EjectorRenderer extends ShaftRenderer<EjectorBlockEntity> {

	static final Vec3 pivot = VecHelper.voxelSpace(0, 11.25, 0.75);

	public EjectorRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	public boolean shouldRenderOffScreen(EjectorBlockEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(EjectorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		float lidProgress = be.getLidProgress(partialTicks);
		float angle = lidProgress * 70;

		if (!VisualizationManager.supportsVisualization(be.getLevel())) {
			SuperByteBuffer model = CachedBuffers.partial(AllPartialModels.EJECTOR_TOP, be.getBlockState());
			applyLidAngle(be, angle, model);
			model.light(light)
					.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid()));
		}

		var msr = TransformStack.of(ms);

		float maxTime =
				(float) (be.earlyTarget != null ? be.earlyTargetTime : be.launcher.getTotalFlyingTicks());
		for (IntAttached<ItemStack> intAttached : be.launchedItems) {
			float time = intAttached.getFirst() + partialTicks;
			if (time > maxTime)
				continue;

			ms.pushPose();
			Vec3 launchedItemLocation = be.getLaunchedItemLocation(time);
			msr.translate(launchedItemLocation.subtract(Vec3.atLowerCornerOf(be.getBlockPos())));
			Vec3 itemRotOffset = VecHelper.voxelSpace(0, 2, -1);
			msr.translate(itemRotOffset);

			if (PackageItem.isPackage(intAttached.getValue())) {
				ms.translate(0, 4 / 16f, 0);
				ms.scale(1.5f, 1.5f, 1.5f);
				msr.rotateYDegrees(time * 20);
			} else {
				ms.scale(.5f, .5f, .5f);
				msr.rotateYDegrees(AngleHelper.horizontalAngle(be.getFacing()));
				msr.rotateXDegrees(time * 40);
			}
			msr.translateBack(itemRotOffset);
			com.simibubi.create.foundation.render.LegacyItemRendererBridge.getItemRenderer()
				.renderStatic(intAttached.getValue(), ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
			ms.popPose();
		}

		DepotBehaviour behaviour = be.getBehaviour(DepotBehaviour.TYPE);
		if (behaviour == null || behaviour.isEmpty())
			return;

		ms.pushPose();
		applyLidAngle(be, angle, msr);
		msr.center()
			.rotateYDegrees(-180 - AngleHelper.horizontalAngle(be.getBlockState()
				.getValue(EjectorBlock.HORIZONTAL_FACING)))
			.uncenter();
		DepotRenderer.renderItemsOf(be, partialTicks, ms, buffer, light, overlay, behaviour);
		ms.popPose();
	}

	static <T extends Translate<T> & Rotate<T>> void applyLidAngle(KineticBlockEntity be, float angle, T tr) {
		applyLidAngle(be, pivot, angle, tr);
	}

	static void applyLidAngle(KineticBlockEntity be, float angle, SuperByteBuffer tr) {
		applyLidAngle(be, pivot, angle, tr);
	}

	static <T extends Translate<T> & Rotate<T>> void applyLidAngle(KineticBlockEntity be, Vec3 rotationOffset, float angle, T tr) {
		tr.center()
			.rotateYDegrees(180 + AngleHelper.horizontalAngle(be.getBlockState()
				.getValue(EjectorBlock.HORIZONTAL_FACING)))
			.uncenter()
			.translate(rotationOffset)
			.rotateXDegrees(-angle)
			.translateBack(rotationOffset);
	}

	static void applyLidAngle(KineticBlockEntity be, Vec3 rotationOffset, float angle, SuperByteBuffer tr) {
		tr.center()
			.rotateYDegrees(180 + AngleHelper.horizontalAngle(be.getBlockState()
				.getValue(EjectorBlock.HORIZONTAL_FACING)))
			.uncenter()
			.translate(rotationOffset)
			.rotateXDegrees(-angle)
			.translateBack(rotationOffset);
	}

}
