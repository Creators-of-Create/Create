package com.simibubi.create.content.contraptions.components.crafter;

import static com.simibubi.create.content.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer.standardKineticRotationTransform;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity.Phase;
import com.simibubi.create.content.contraptions.components.crafter.RecipeGridHandler.GroupedItems;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class MechanicalCrafterRenderer extends SafeTileEntityRenderer<MechanicalCrafterTileEntity> {

	public MechanicalCrafterRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(MechanicalCrafterTileEntity te, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		ms.pushPose();
		Direction facing = te.getBlockState()
			.getValue(HORIZONTAL_FACING);
		Vector3d vec = Vector3d.atLowerCornerOf(facing.getNormal()).scale(.58)
			.add(.5, .5, .5);

		if (te.phase == Phase.EXPORTING) {
			Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(te.getBlockState());
			float progress =
				MathHelper.clamp((1000 - te.countDown + te.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
			vec = vec.add(Vector3d.atLowerCornerOf(targetDirection.getNormal()).scale(progress * .75f));
		}

		ms.translate(vec.x, vec.y, vec.z);
		ms.scale(1 / 2f, 1 / 2f, 1 / 2f);
		float yRot = AngleHelper.horizontalAngle(facing);
		ms.mulPose(Vector3f.YP.rotationDegrees(yRot));
		renderItems(te, partialTicks, ms, buffer, light, overlay);
		ms.popPose();

		renderFast(te, partialTicks, ms, buffer, light);
	}

	public void renderItems(MechanicalCrafterTileEntity te, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {
		if (te.phase == Phase.IDLE) {
			ItemStack stack = te.getInventory().getItem(0);
			if (!stack.isEmpty()) {
				ms.pushPose();
				ms.translate(0, 0, -1 / 256f);
				Minecraft.getInstance()
					.getItemRenderer()
					.renderStatic(stack, TransformType.FIXED, light, overlay, ms, buffer);
				ms.popPose();
			}
		} else {
			// render grouped items
			GroupedItems items = te.groupedItems;
			float distance = .5f;

			ms.pushPose();

			if (te.phase == Phase.CRAFTING) {
				items = te.groupedItemsBeforeCraft;
				items.calcStats();
				float progress =
					MathHelper.clamp((2000 - te.countDown + te.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
				float earlyProgress = MathHelper.clamp(progress * 2, 0, 1);
				float lateProgress = MathHelper.clamp(progress * 2 - 1, 0, 1);

				ms.scale(1 - lateProgress, 1 - lateProgress, 1 - lateProgress);
				Vector3d centering =
					new Vector3d(-items.minX + (-items.width + 1) / 2f, -items.minY + (-items.height + 1) / 2f, 0)
						.scale(earlyProgress);
				ms.translate(centering.x * .5f, centering.y * .5f, 0);
				distance += (-4 * (progress - .5f) * (progress - .5f) + 1) * .25f;
			}

			boolean onlyRenderFirst = te.phase == Phase.INSERTING || te.phase == Phase.CRAFTING && te.countDown < 1000;
			final float spacing = distance;
			items.grid.forEach((pair, stack) -> {
				if (onlyRenderFirst && (pair.getLeft()
					.intValue() != 0
					|| pair.getRight()
						.intValue() != 0))
					return;

				ms.pushPose();
				Integer x = pair.getKey();
				Integer y = pair.getValue();
				ms.translate(x * spacing, y * spacing, 0);
				MatrixTransformStack.of(ms)
					.nudge(x * 13 + y + te.getBlockPos()
						.hashCode());
				Minecraft.getInstance()
					.getItemRenderer()
					.renderStatic(stack, TransformType.FIXED, light, overlay, ms, buffer);
				ms.popPose();
			});

			ms.popPose();

			if (te.phase == Phase.CRAFTING) {
				items = te.groupedItems;
				float progress =
					MathHelper.clamp((1000 - te.countDown + te.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
				float earlyProgress = MathHelper.clamp(progress * 2, 0, 1);
				float lateProgress = MathHelper.clamp(progress * 2 - 1, 0, 1);

				ms.mulPose(Vector3f.ZP.rotationDegrees(earlyProgress * 2 * 360));
				float upScaling = earlyProgress * 1.125f;
				float downScaling = 1 + (1 - lateProgress) * .125f;
				ms.scale(upScaling, upScaling, upScaling);
				ms.scale(downScaling, downScaling, downScaling);

				items.grid.forEach((pair, stack) -> {
					if (pair.getLeft()
						.intValue() != 0
						|| pair.getRight()
							.intValue() != 0)
						return;
					Minecraft.getInstance()
						.getItemRenderer()
						.renderStatic(stack, TransformType.FIXED, light, overlay, ms, buffer);
				});
			}

		}
	}

	public void renderFast(MechanicalCrafterTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light) {
		BlockState blockState = te.getBlockState();
		IVertexBuilder vb = buffer.getBuffer(RenderType.solid());

		if (!Backend.getInstance().canUseInstancing(te.getLevel())) {
			SuperByteBuffer superBuffer = PartialBufferer.get(AllBlockPartials.SHAFTLESS_COGWHEEL, blockState);
			standardKineticRotationTransform(superBuffer, te, light);
			superBuffer.rotateCentered(Direction.UP, (float) (blockState.getValue(HORIZONTAL_FACING).getAxis() != Direction.Axis.X ? 0 : Math.PI / 2));
			superBuffer.rotateCentered(Direction.EAST, (float) (Math.PI / 2));
			superBuffer.renderInto(ms, vb);
		}

		Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(blockState);
		BlockPos pos = te.getBlockPos();

		if ((te.covered || te.phase != Phase.IDLE) && te.phase != Phase.CRAFTING && te.phase != Phase.INSERTING) {
			SuperByteBuffer lidBuffer =
				renderAndTransform(AllBlockPartials.MECHANICAL_CRAFTER_LID, blockState);
			lidBuffer.light(light).renderInto(ms, vb);
		}

		if (MechanicalCrafterBlock.isValidTarget(te.getLevel(), pos.relative(targetDirection), blockState)) {
			SuperByteBuffer beltBuffer =
				renderAndTransform(AllBlockPartials.MECHANICAL_CRAFTER_BELT, blockState);
			SuperByteBuffer beltFrameBuffer =
				renderAndTransform(AllBlockPartials.MECHANICAL_CRAFTER_BELT_FRAME, blockState);

			if (te.phase == Phase.EXPORTING) {
				int textureIndex = (int) ((te.getCountDownSpeed() / 128f * AnimationTickHolder.getTicks()));
				beltBuffer.shiftUVtoSheet(AllSpriteShifts.CRAFTER_THINGIES, (textureIndex % 4) / 4f, 0, 1);
			}

			beltBuffer.light(light).renderInto(ms, vb);
			beltFrameBuffer.light(light).renderInto(ms, vb);

		} else {
			SuperByteBuffer arrowBuffer =
				renderAndTransform(AllBlockPartials.MECHANICAL_CRAFTER_ARROW, blockState);
			arrowBuffer.light(light).renderInto(ms, vb);
		}

	}

	private SuperByteBuffer renderAndTransform(PartialModel renderBlock, BlockState crafterState) {
		SuperByteBuffer buffer = PartialBufferer.get(renderBlock, crafterState);
		float xRot = crafterState.getValue(MechanicalCrafterBlock.POINTING)
				.getXRotation();
		float yRot = AngleHelper.horizontalAngle(crafterState.getValue(HORIZONTAL_FACING));
		buffer.rotateCentered(Direction.UP, (float) ((yRot + 90) / 180 * Math.PI));
		buffer.rotateCentered(Direction.EAST, (float) ((xRot) / 180 * Math.PI));
		return buffer;
	}

}
