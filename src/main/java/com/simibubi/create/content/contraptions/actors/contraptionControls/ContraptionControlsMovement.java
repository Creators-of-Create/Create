package com.simibubi.create.content.contraptions.actors.contraptionControls;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
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
		if (context.contraption instanceof ElevatorContraption && context.blockEntityData != null)
			context.blockEntityData.remove("Filter");
	}

	@Override
	public void stopMoving(MovementContext context) {
		ItemStack filter = getFilter(context);
		if (filter != null)
			context.blockEntityData.putBoolean("Disabled", context.contraption.isActorTypeDisabled(filter)
				|| context.contraption.isActorTypeDisabled(ItemStack.EMPTY));
	}

	public static boolean isSameFilter(ItemStack stack1, ItemStack stack2) {
		if (stack1.isEmpty() && stack2.isEmpty())
			return true;
		return ItemHandlerHelper.canItemStacksStack(stack1, stack2);
	}

	public static ItemStack getFilter(MovementContext ctx) {
		CompoundTag blockEntityData = ctx.blockEntityData;
		if (blockEntityData == null)
			return null;
		return ItemStack.of(blockEntityData.getCompound("Filter"));
	}

	public static boolean isDisabledInitially(MovementContext ctx) {
		return ctx.blockEntityData != null && ctx.blockEntityData.getBoolean("Disabled");
	}

	@Override
	public void tick(MovementContext ctx) {
		if (!ctx.world.isClientSide())
			return;

		Contraption contraption = ctx.contraption;
		if (!(contraption instanceof ElevatorContraption ec)) {
			if (!(contraption.presentBlockEntities.get(ctx.localPos) instanceof ContraptionControlsBlockEntity cbe))
				return;
			ItemStack filter = getFilter(ctx);
			int value =
				contraption.isActorTypeDisabled(filter) || contraption.isActorTypeDisabled(ItemStack.EMPTY) ? 4 * 45
					: 0;
			cbe.indicator.setValue(value);
			cbe.indicator.updateChaseTarget(value);
			cbe.tickAnimations();
			return;
		}

		if (!(ctx.temporaryData instanceof ElevatorFloorSelection))
			ctx.temporaryData = new ElevatorFloorSelection();

		ElevatorFloorSelection efs = (ElevatorFloorSelection) ctx.temporaryData;
		tickFloorSelection(efs, ec);

		if (!(contraption.presentBlockEntities.get(ctx.localPos) instanceof ContraptionControlsBlockEntity cbe))
			return;

		cbe.tickAnimations();

		int currentY = (int) Math.round(contraption.entity.getY() + ec.getContactYOffset());
		boolean atTargetY = ec.clientYTarget == currentY;

		LerpedFloat indicator = cbe.indicator;
		float currentIndicator = indicator.getChaseTarget();
		boolean below = atTargetY ? currentIndicator > 0 : ec.clientYTarget <= currentY;

		if (currentIndicator == 0 && !atTargetY) {
			int startingPoint = below ? 181 : -181;
			indicator.setValue(startingPoint);
			indicator.updateChaseTarget(startingPoint);
			cbe.tickAnimations();
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

		if (ec.isTargetUnreachable(efs.currentTargetY))
			efs.currentLongName = Lang.translate("contraption.controls.floor_unreachable")
				.string();
	}

	@Override
	public boolean renderAsNormalBlockEntity() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext ctx, VirtualRenderWorld renderWorld, ContraptionMatrices matrices,
		MultiBufferSource buffer) {
		ContraptionControlsRenderer.renderInContraption(ctx, renderWorld, matrices, buffer);
	}

	public static class ElevatorFloorSelection {
		public int currentIndex = 0;
		public int currentTargetY = 0;
		public boolean targetYEqualsSelection = true;
		public String currentShortName = "";
		public String currentLongName = "";
	}

}
