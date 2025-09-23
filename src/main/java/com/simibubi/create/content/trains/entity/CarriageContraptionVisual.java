package com.simibubi.create.content.trains.entity;

import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import com.simibubi.create.content.trains.bogey.BogeyVisual;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.util.RecyclingPoseStack;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class CarriageContraptionVisual extends ContraptionVisual<CarriageContraptionEntity> {
	private final PoseStack poseStack = new RecyclingPoseStack();

	@Nullable
	private Couple<@Nullable VisualizedBogey> bogeys;
	private Couple<Boolean> bogeyHidden = Couple.create(() -> false);

	public CarriageContraptionVisual(VisualizationContext context, CarriageContraptionEntity entity, float partialTick) {
		super(context, entity, partialTick);
		entity.bindInstance(this);

		animate(partialTick);
	}

	public void setBogeyVisibility(boolean first, boolean visible) {
		bogeyHidden.set(first, !visible);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		super.beginFrame(ctx);

		animate(ctx.partialTick());
	}

	@Override
	protected <T extends BlockEntity> void setupVisualizer(T be, float partialTicks) {
		if (entity.getContraption() instanceof CarriageContraption cc && cc.isHiddenInPortal(be.getBlockPos())) {
			return;
		}
		super.setupVisualizer(be, partialTicks);
	}

	@Override
	protected void setupActor(MutablePair<StructureBlockInfo, MovementContext> actor, VirtualRenderWorld renderLevel) {
		if (entity.getContraption() instanceof CarriageContraption cc && cc.isHiddenInPortal(actor.left.pos())) {
			return;
		}
		super.setupActor(actor, renderLevel);
	}

	/**
	 * @return True if we're ready to actually animate.
	 */
	private boolean checkCarriage(float pt) {
		if (bogeys != null) {
			return true;
		}

		var carriage = entity.getCarriage();

		if (entity.validForRender && carriage != null) {
			bogeys = carriage.bogeys.mapNotNull(bogey -> VisualizedBogey.of(visualizationContext, bogey, pt));
			updateLight(pt);
			return true;
		}

		return false;
	}

	private void animate(float partialTick) {
		if (!checkCarriage(partialTick)) {
			return;
		}

		float viewYRot = entity.getViewYRot(partialTick);
		float viewXRot = entity.getViewXRot(partialTick);
		int bogeySpacing = entity.getCarriage().bogeySpacing;

		poseStack.pushPose();

		Vector3f visualPosition = getVisualPosition(partialTick);
		TransformStack.of(poseStack)
			.translate(visualPosition);

		for (boolean current : Iterate.trueAndFalse) {
			VisualizedBogey visualizedBogey = bogeys.get(current);
			if (visualizedBogey == null)
				continue;

			if (bogeyHidden.get(current)) {
				visualizedBogey.visual.hide();
				continue;
			}

			poseStack.pushPose();
			CarriageBogey bogey = visualizedBogey.bogey;

			CarriageContraptionEntityRenderer.translateBogey(poseStack, bogey, bogeySpacing, viewYRot, viewXRot, partialTick);
			poseStack.translate(0, -1.5 - 1 / 128f, 0);

			CompoundTag bogeyData = bogey.bogeyData;
			if (bogeyData == null) {
				bogeyData = new CompoundTag();
			}
			visualizedBogey.visual.update(bogeyData, bogey.wheelAngle.getValue(partialTick), poseStack);
			poseStack.popPose();
		}

		poseStack.popPose();
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);

		if (bogeys == null)
			return;

		bogeys.forEach(bogey -> {
			if (bogey != null) {
				int packedLight = CarriageContraptionEntityRenderer.getBogeyLightCoords(entity, bogey.bogey, partialTick);
				bogey.visual.updateLight(packedLight);
			}
		});
	}

	@Override
	public void _delete() {
		super._delete();

		if (bogeys == null)
			return;

		bogeys.forEach(bogey -> {
			if (bogey != null) {
				bogey.visual.delete();
			}
		});
	}

	private record VisualizedBogey(CarriageBogey bogey, BogeyVisual visual) {
		@Nullable
		static VisualizedBogey of(VisualizationContext ctx, CarriageBogey bogey, float partialTick) {
			BogeyVisual visual = bogey.getStyle().createVisual(bogey.getSize(), ctx, partialTick, true);
			if (visual == null) {
				return null;
			}
			return new VisualizedBogey(bogey, visual);
		}
	}
}
