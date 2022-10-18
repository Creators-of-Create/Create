package com.simibubi.create.content.contraptions.components.actors.controls;

import java.util.Random;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.logistics.block.redstone.NixieTubeRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.DyeHelper;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class ContraptionControlsMovement implements MovementBehaviour {

	@Override
	public ItemStack canBeDisabledVia(MovementContext context) {
		return null;
	}

	@Override
	public void startMoving(MovementContext context) {
		if (context.contraption instanceof ElevatorContraption && context.tileData != null)
			context.tileData.remove("Filter");
	}

	@Override
	public void stopMoving(MovementContext context) {
		ItemStack filter = getFilter(context);
		if (filter != null)
			context.tileData.putBoolean("Disabled", context.contraption.isActorTypeDisabled(filter)
				|| context.contraption.isActorTypeDisabled(ItemStack.EMPTY));
	}

	public static boolean isSameFilter(ItemStack stack1, ItemStack stack2) {
		if (stack1.isEmpty() && stack2.isEmpty())
			return true;
		return ItemHandlerHelper.canItemStacksStack(stack1, stack2);
	}

	private static Random r = new Random();

	public static ItemStack getFilter(MovementContext ctx) {
		CompoundTag tileData = ctx.tileData;
		if (tileData == null)
			return null;
		return ItemStack.of(tileData.getCompound("Filter"));
	}

	public static boolean isDisabledInitially(MovementContext ctx) {
		return ctx.tileData != null && ctx.tileData.getBoolean("Disabled");
	}

	@Override
	public void tick(MovementContext ctx) {
		if (!ctx.world.isClientSide())
			return;

		Contraption contraption = ctx.contraption;
		if (!(contraption instanceof ElevatorContraption ec)) {
			if (!(contraption.presentTileEntities.get(ctx.localPos)instanceof ContraptionControlsTileEntity cte))
				return;
			ItemStack filter = getFilter(ctx);
			int value =
				contraption.isActorTypeDisabled(filter) || contraption.isActorTypeDisabled(ItemStack.EMPTY) ? 4 * 45
					: 0;
			cte.indicator.setValue(value);
			cte.indicator.updateChaseTarget(value);
			cte.tickAnimations();
			return;
		}

		if (!(ctx.temporaryData instanceof ElevatorFloorSelection))
			ctx.temporaryData = new ElevatorFloorSelection();

		ElevatorFloorSelection efs = (ElevatorFloorSelection) ctx.temporaryData;
		tickFloorSelection(efs, ec);

		if (!(contraption.presentTileEntities.get(ctx.localPos)instanceof ContraptionControlsTileEntity cte))
			return;

		cte.tickAnimations();

		int currentY = (int) Math.round(contraption.entity.getY() + ec.getContactYOffset());
		boolean atTargetY = ec.clientYTarget == currentY;

		LerpedFloat indicator = cte.indicator;
		float currentIndicator = indicator.getChaseTarget();
		boolean below = atTargetY ? currentIndicator > 0 : ec.clientYTarget <= currentY;

		if (currentIndicator == 0 && !atTargetY) {
			int startingPoint = below ? 181 : -181;
			indicator.setValue(startingPoint);
			indicator.updateChaseTarget(startingPoint);
			cte.tickAnimations();
			return;
		}

		int currentStage = Mth.floor(((currentIndicator % 360) + 360) % 360);
		if (!atTargetY || currentStage / 45 != 0) {
			float increment = currentStage / 45 == (below ? 4 : 3) ? 2.25f : 33.75f;
			indicator.chase(currentIndicator + (below ? increment : -increment), 45f, Chaser.LINEAR);
			return;
		}

		indicator.setValue(0);
		indicator.updateChaseTarget(0);
		return;
	}

	public static void tickFloorSelection(ElevatorFloorSelection efs, ElevatorContraption ec) {
		if (ec.namesList.isEmpty()) {
			efs.currentShortName = "X";
			efs.currentLongName = "No Floors";
			efs.currentIndex = 0;
			efs.targetYEqualsSelection = true;
			return;
		}

		efs.currentIndex = Mth.clamp(efs.currentIndex, 0, ec.namesList.size() - 1);
		IntAttached<Couple<String>> entry = ec.namesList.get(efs.currentIndex);
		efs.currentTargetY = entry.getFirst();
		efs.currentShortName = entry.getSecond()
			.getFirst();
		efs.currentLongName = entry.getSecond()
			.getSecond();
		efs.targetYEqualsSelection = efs.currentTargetY == ec.clientYTarget;
	}

	@Override
	public boolean renderAsNormalTileEntity() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext ctx, VirtualRenderWorld renderWorld, ContraptionMatrices matrices,
		MultiBufferSource buffer) {

		if (!(ctx.temporaryData instanceof ElevatorFloorSelection efs))
			return;
		if (!AllBlocks.CONTRAPTION_CONTROLS.has(ctx.state))
			return;

		Entity cameraEntity = Minecraft.getInstance()
			.getCameraEntity();
		float playerDistance = (float) (ctx.position == null || cameraEntity == null ? 0
			: ctx.position.distanceToSqr(cameraEntity.getEyePosition()));

		float flicker = r.nextFloat();
		Couple<Integer> couple = DyeHelper.DYE_TABLE.get(efs.targetYEqualsSelection ? DyeColor.WHITE : DyeColor.ORANGE);
		int brightColor = couple.getFirst();
		int darkColor = couple.getSecond();
		int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4);
		Font fontRenderer = Minecraft.getInstance().font;
		float shadowOffset = .5f;

		String text = efs.currentShortName;
		String description = efs.currentLongName;
		PoseStack ms = matrices.getViewProjection();
		TransformStack msr = TransformStack.cast(ms);

		ms.pushPose();
		msr.translate(ctx.localPos);
		msr.rotateCentered(Direction.UP,
			AngleHelper.rad(AngleHelper.horizontalAngle(ctx.state.getValue(ContraptionControlsBlock.FACING))));
		ms.translate(0.275f + 0.125f, 1, 0.5f);
		msr.rotate(Direction.WEST, AngleHelper.rad(67.5f));

		float buttondepth = -.25f;
		if (ctx.contraption.presentTileEntities.get(ctx.localPos)instanceof ContraptionControlsTileEntity cte)
			buttondepth += -1 / 24f * cte.button.getValue(AnimationTickHolder.getPartialTicks(renderWorld));

		if (!text.isBlank() && playerDistance < 100) {
			int actualWidth = fontRenderer.width(text);
			int width = Math.max(actualWidth, 12);
			float scale = 1 / (5f * (width - .5f));
			float heightCentering = (width - 8f) / 2;

			ms.pushPose();
			ms.translate(0, .15f, buttondepth);
			ms.scale(scale, -scale, scale);
			ms.translate(Math.max(0, width - actualWidth) / 2, heightCentering, 0);
			NixieTubeRenderer.drawInWorldString(ms, buffer, text, flickeringBrightColor);
			ms.translate(shadowOffset, shadowOffset, -1 / 16f);
			NixieTubeRenderer.drawInWorldString(ms, buffer, text, Color.mixColors(darkColor, 0, .35f));
			ms.popPose();
		}

		if (!description.isBlank() && playerDistance < 20) {
			int actualWidth = fontRenderer.width(description);
			int width = Math.max(actualWidth, 55);
			float scale = 1 / (3f * (width - .5f));
			float heightCentering = (width - 8f) / 2;

			ms.pushPose();
			ms.translate(-.0635f, 0.06f, buttondepth);
			ms.scale(scale, -scale, scale);
			ms.translate(Math.max(0, width - actualWidth) / 2, heightCentering, 0);
			NixieTubeRenderer.drawInWorldString(ms, buffer, description, flickeringBrightColor);
			ms.popPose();
		}

		ms.popPose();

	}

	public static class ElevatorFloorSelection {
		public int currentIndex = 0;
		public int currentTargetY = 0;
		public boolean targetYEqualsSelection = true;
		public String currentShortName = "";
		public String currentLongName = "";
	}

}
