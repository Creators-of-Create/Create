package com.simibubi.create.content.logistics.packager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PackagerRenderer extends SmartBlockEntityRenderer<PackagerBlockEntity> {

	public PackagerRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(PackagerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		ItemStack renderedBox = be.getRenderedBox();
		float trayOffset = be.getTrayOffset(partialTicks);
		BlockState blockState = be.getBlockState();
		Direction facing = blockState.getValue(PackagerBlock.FACING)
			.getOpposite();
		
		if (!VisualizationManager.supportsVisualization(be.getLevel())) {
			var hatchModel = getHatchModel(be);

			SuperByteBuffer sbb = CachedBuffers.partial(hatchModel, blockState);
			sbb.translate(Vec3.atLowerCornerOf(facing.getUnitVec3i())
					.scale(.49999f))
				.rotateYCenteredDegrees(AngleHelper.horizontalAngle(facing))
				.rotateXCenteredDegrees(AngleHelper.verticalAngle(facing))
				.light(light)
				.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid()));

			sbb = CachedBuffers.partial(getTrayModel(blockState), blockState);
			sbb.translate(Vec3.atLowerCornerOf(facing.getUnitVec3i())
					.scale(trayOffset))
				.rotateYCenteredDegrees(facing.toYRot())
				.light(light)
				.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.cutoutMipped()));
		}

		if (!renderedBox.isEmpty()) {
			ms.pushPose();
			var msr = TransformStack.of(ms);
			msr.translate(Vec3.atLowerCornerOf(facing.getUnitVec3i())
					.scale(trayOffset))
				.translate(.5f, .5f, .5f)
				.rotateYDegrees(facing.toYRot())
				.translate(0, 2 / 16f, 0)
				.scale(1.49f, 1.49f, 1.49f);
			com.simibubi.create.foundation.render.LegacyItemRendererBridge.getItemRenderer()
				.renderStatic(renderedBox, ItemDisplayContext.FIXED, light, overlay, ms, buffer, be.getLevel(), 0);
			ms.popPose();
		}
	}

	public static PartialModel getTrayModel(BlockState blockState) {
		return AllBlocks.PACKAGER.has(blockState) ? AllPartialModels.PACKAGER_TRAY_REGULAR
			: AllPartialModels.PACKAGER_TRAY_DEFRAG;
	}

	public static PartialModel getHatchModel(PackagerBlockEntity be) {
		return isHatchOpen(be) ? AllPartialModels.PACKAGER_HATCH_OPEN : AllPartialModels.PACKAGER_HATCH_CLOSED;
	}

	public static boolean isHatchOpen(PackagerBlockEntity be) {
		return be.animationTicks > (be.animationInward ? 1 : 5)
			&& be.animationTicks < PackagerBlockEntity.CYCLE - (be.animationInward ? 5 : 1);
	}

}
