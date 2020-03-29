package com.simibubi.create.modules.contraptions.components.crafter;

import static com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer.standardKineticRotationTransform;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.block.SafeTileEntityRenderer;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.components.crafter.MechanicalCrafterTileEntity.Phase;
import com.simibubi.create.modules.contraptions.components.crafter.RecipeGridHandler.GroupedItems;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("deprecation")
public class MechanicalCrafterTileEntityRenderer extends SafeTileEntityRenderer<MechanicalCrafterTileEntity> {

	public static SpriteShiftEntry animatedTexture =
		SpriteShifter.get("block/crafter_thingies", "block/crafter_thingies");

	@Override
	public void renderWithGL(MechanicalCrafterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage) {
		GlStateManager.pushMatrix();
		Direction facing = te.getBlockState().get(HORIZONTAL_FACING);
		Vec3d vec = new Vec3d(facing.getDirectionVec()).scale(.58).add(.5, .5, .5);

		if (te.phase == Phase.EXPORTING) {
			Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(te.getBlockState());
			float progress =
				MathHelper.clamp((1000 - te.countDown + te.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
			vec = vec.add(new Vec3d(targetDirection.getDirectionVec()).scale(progress * .75f));
		}

		GlStateManager.translated(x + vec.x, y + vec.y, z + vec.z);
		GlStateManager.scalef(1 / 2f, 1 / 2f, 1 / 2f);
		float yRot = AngleHelper.horizontalAngle(facing);
		GlStateManager.rotated(yRot, 0, 1, 0);
		renderItems(te, partialTicks);
		GlStateManager.popMatrix();

		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, Tessellator.getInstance().getBuffer());
		TessellatorHelper.draw();
	}

	public void renderItems(MechanicalCrafterTileEntity te, float partialTicks) {
		RenderHelper.enableStandardItemLighting();

		if (te.phase == Phase.IDLE) {
			ItemStack stack = te.inventory.getStackInSlot(0);
			if (!stack.isEmpty()) {
				GlStateManager.pushMatrix();
				GlStateManager.translatef(0, 0, -1 / 256f);
				Minecraft.getInstance().getItemRenderer().renderItem(stack, TransformType.FIXED);
				GlStateManager.popMatrix();
			}
		} else {
			// render grouped items
			GroupedItems items = te.groupedItems;
			float distance = .5f;

			GlStateManager.pushMatrix();

			if (te.phase == Phase.CRAFTING) {
				items = te.groupedItemsBeforeCraft;
				items.calcStats();
				float progress =
					MathHelper.clamp((2000 - te.countDown + te.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
				float earlyProgress = MathHelper.clamp(progress * 2, 0, 1);
				float lateProgress = MathHelper.clamp(progress * 2 - 1, 0, 1);

				GlStateManager.scaled(1 - lateProgress, 1 - lateProgress, 1 - lateProgress);
				Vec3d centering =
					new Vec3d(-items.minX + (-items.width + 1) / 2f, -items.minY + (-items.height + 1) / 2f, 0)
							.scale(earlyProgress);
				GlStateManager.translated(centering.x * .5f, centering.y * .5f, 0);
				distance += (-4 * (progress - .5f) * (progress - .5f) + 1) * .25f;
			}

			boolean onlyRenderFirst = te.phase == Phase.INSERTING || te.phase == Phase.CRAFTING && te.countDown < 1000;
			final float spacing = distance;
			items.grid.forEach((pair, stack) -> {
				if (onlyRenderFirst && (pair.getLeft().intValue() != 0 || pair.getRight().intValue() != 0))
					return;

				GlStateManager.pushMatrix();
				GlStateManager.translatef(pair.getKey() * spacing, pair.getValue() * spacing, 0);
				TessellatorHelper.fightZFighting(pair.hashCode() + te.getPos().hashCode());
				Minecraft.getInstance().getItemRenderer().renderItem(stack, TransformType.FIXED);
				GlStateManager.popMatrix();
			});

			GlStateManager.popMatrix();

			if (te.phase == Phase.CRAFTING) {
				items = te.groupedItems;
				float progress =
					MathHelper.clamp((1000 - te.countDown + te.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
				float earlyProgress = MathHelper.clamp(progress * 2, 0, 1);
				float lateProgress = MathHelper.clamp(progress * 2 - 1, 0, 1);

				GlStateManager.rotated(earlyProgress * 2 * 360, 0, 0, 1);
				float upScaling = earlyProgress * 1.125f;
				float downScaling = 1 + (1 - lateProgress) * .125f;
				GlStateManager.scaled(upScaling, upScaling, upScaling);
				GlStateManager.scaled(downScaling, downScaling, downScaling);

				items.grid.forEach((pair, stack) -> {
					if (pair.getLeft().intValue() != 0 || pair.getRight().intValue() != 0)
						return;
					Minecraft.getInstance().getItemRenderer().renderItem(stack, TransformType.FIXED);
				});
			}

		}

		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public void renderFast(MechanicalCrafterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockState blockState = te.getBlockState();

		SuperByteBuffer superBuffer = AllBlockPartials.SHAFTLESS_COGWHEEL.renderOn(blockState);
		superBuffer.rotateCentered(Axis.X, (float) (Math.PI / 2));
		superBuffer.rotateCentered(Axis.Y,
				(float) (blockState.get(HORIZONTAL_FACING).getAxis() != Axis.X ? 0 : Math.PI / 2));
		standardKineticRotationTransform(superBuffer, te, getWorld()).translate(x, y, z).renderInto(buffer);

		Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(blockState);
		BlockPos pos = te.getPos();

		if ((te.covered || te.phase != Phase.IDLE) && te.phase != Phase.CRAFTING && te.phase != Phase.INSERTING) {
			SuperByteBuffer lidBuffer = renderAndTransform(AllBlockPartials.MECHANICAL_CRAFTER_LID, blockState, pos);
			lidBuffer.translate(x, y, z).renderInto(buffer);
		}

		if (MechanicalCrafterBlock.isValidTarget(getWorld(), pos.offset(targetDirection), blockState)) {
			SuperByteBuffer beltBuffer = renderAndTransform(AllBlockPartials.MECHANICAL_CRAFTER_BELT, blockState, pos);
			SuperByteBuffer beltFrameBuffer =
				renderAndTransform(AllBlockPartials.MECHANICAL_CRAFTER_BELT_FRAME, blockState, pos);

			if (te.phase == Phase.EXPORTING) {
				int textureIndex = (int) ((te.getCountDownSpeed() / 128f * AnimationTickHolder.ticks));
				beltBuffer.shiftUVtoSheet(animatedTexture.getOriginal(), animatedTexture.getTarget(),
						(textureIndex % 4) * 4, 0);
			} else {
				beltBuffer.shiftUVtoSheet(animatedTexture.getOriginal(), animatedTexture.getTarget(), 0, 0);
			}

			beltBuffer.translate(x, y, z).renderInto(buffer);
			beltFrameBuffer.translate(x, y, z).renderInto(buffer);

		} else {
			SuperByteBuffer arrowBuffer =
				renderAndTransform(AllBlockPartials.MECHANICAL_CRAFTER_ARROW, blockState, pos);
			arrowBuffer.translate(x, y, z).renderInto(buffer);
		}

	}

	private SuperByteBuffer renderAndTransform(AllBlockPartials renderBlock, BlockState crafterState, BlockPos pos) {
		SuperByteBuffer buffer = renderBlock.renderOn(crafterState);
		float xRot = crafterState.get(MechanicalCrafterBlock.POINTING).getXRotation();
		float yRot = AngleHelper.horizontalAngle(crafterState.get(HORIZONTAL_FACING));
		buffer.rotateCentered(Axis.X, (float) ((xRot) / 180 * Math.PI));
		buffer.rotateCentered(Axis.Y, (float) ((yRot + 90) / 180 * Math.PI));
		buffer.light(crafterState.getPackedLightmapCoords(getWorld(), pos));
		return buffer;
	}

}
