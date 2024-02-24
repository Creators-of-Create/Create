package com.simibubi.create.content.trains;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsHandler;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.ControlsUtil;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class TrainHUD {

	public static final IGuiOverlay OVERLAY = TrainHUD::renderOverlay;

	static LerpedFloat displayedSpeed = LerpedFloat.linear();
	static LerpedFloat displayedThrottle = LerpedFloat.linear();
	static LerpedFloat displayedPromptSize = LerpedFloat.linear();

	static Double editedThrottle = null;
	static int hudPacketCooldown = 5;
	static int honkPacketCooldown = 5;

	public static Component currentPrompt;
	public static boolean currentPromptShadow;
	public static int promptKeepAlive = 0;

	static boolean usedToHonk;

	public static void tick() {
		if (promptKeepAlive > 0)
			promptKeepAlive--;
		else
			currentPrompt = null;

		Minecraft mc = Minecraft.getInstance();
		displayedPromptSize.chase(currentPrompt != null ? mc.font.width(currentPrompt) + 17 : 0, .5f, Chaser.EXP);
		displayedPromptSize.tickChaser();

		Carriage carriage = getCarriage();
		if (carriage == null)
			return;

		Train train = carriage.train;
		double value =
			Math.abs(train.speed) / (train.maxSpeed() * AllConfigs.server().trains.manualTrainSpeedModifier.getF());
		value = Mth.clamp(value + 0.05f, 0, 1);

		displayedSpeed.chase((int) (value * 18) / 18f, .5f, Chaser.EXP);
		displayedSpeed.tickChaser();
		displayedThrottle.chase(editedThrottle != null ? editedThrottle : train.throttle, .75f, Chaser.EXP);
		displayedThrottle.tickChaser();

		boolean isSprintKeyPressed = ControlsUtil.isActuallyPressed(mc.options.keySprint);

		if (isSprintKeyPressed && honkPacketCooldown-- <= 0) {
			train.determineHonk(mc.level);
			if (train.lowHonk != null) {
				AllPackets.getChannel().sendToServer(new HonkPacket.Serverbound(train, true));
				honkPacketCooldown = 5;
				usedToHonk = true;
			}
		}

		if (!isSprintKeyPressed && usedToHonk) {
			AllPackets.getChannel().sendToServer(new HonkPacket.Serverbound(train, false));
			honkPacketCooldown = 0;
			usedToHonk = false;
		}

		if (editedThrottle == null)
			return;
		if (Mth.equal(editedThrottle, train.throttle)) {
			editedThrottle = null;
			hudPacketCooldown = 5;
			return;
		}

		if (hudPacketCooldown-- <= 0) {
			AllPackets.getChannel().sendToServer(new TrainHUDUpdatePacket.Serverbound(train, editedThrottle));
			hudPacketCooldown = 5;
		}
	}

	private static Carriage getCarriage() {
		if (!(ControlsHandler.getContraption() instanceof CarriageContraptionEntity cce))
			return null;
		return cce.getCarriage();
	}

	public static void renderOverlay(ForgeGui gui, PoseStack poseStack, float partialTicks, int width,
		int height) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;

		if (!(ControlsHandler.getContraption() instanceof CarriageContraptionEntity cce))
			return;
		Carriage carriage = cce.getCarriage();
		if (carriage == null)
			return;
		Entity cameraEntity = Minecraft.getInstance()
			.getCameraEntity();
		if (cameraEntity == null)
			return;
		BlockPos localPos = ControlsHandler.getControlsPos();
		if (localPos == null)
			return;

		poseStack.pushPose();
		poseStack.translate(width / 2 - 91, height - 29, 0);

		// Speed, Throttle

		AllGuiTextures.TRAIN_HUD_FRAME.render(poseStack, -2, 1);
		AllGuiTextures.TRAIN_HUD_SPEED_BG.render(poseStack, 0, 0);

		int w = (int) (AllGuiTextures.TRAIN_HUD_SPEED.width * displayedSpeed.getValue(partialTicks));
		int h = AllGuiTextures.TRAIN_HUD_SPEED.height;

		AllGuiTextures.TRAIN_HUD_SPEED.bind();
		GuiComponent.blit(poseStack, 0, 0, 0, AllGuiTextures.TRAIN_HUD_SPEED.startX,
			AllGuiTextures.TRAIN_HUD_SPEED.startY, w, h, 256, 256);

		int promptSize = (int) displayedPromptSize.getValue(partialTicks);
		if (promptSize > 1) {

			poseStack.pushPose();
			poseStack.translate(promptSize / -2f + 91, -27, 100);

			AllGuiTextures.TRAIN_PROMPT_L.render(poseStack, -3, 0);
			AllGuiTextures.TRAIN_PROMPT_R.render(poseStack, promptSize, 0);
			GuiComponent.blit(poseStack, 0, 0, 0, AllGuiTextures.TRAIN_PROMPT.startX + (128 - promptSize / 2f),
				AllGuiTextures.TRAIN_PROMPT.startY, promptSize, AllGuiTextures.TRAIN_PROMPT.height, 256, 256);

			poseStack.popPose();

			Font font = mc.font;
			if (currentPrompt != null && font.width(currentPrompt) < promptSize - 10) {
				poseStack.pushPose();
				poseStack.translate(font.width(currentPrompt) / -2f + 82, -27, 100);
				if (currentPromptShadow)
					font.drawShadow(poseStack, currentPrompt, 9, 4, 0x544D45);
				else
					font.draw(poseStack, currentPrompt, 9, 4, 0x544D45);
				poseStack.popPose();
			}
		}

		AllGuiTextures.TRAIN_HUD_DIRECTION.render(poseStack, 77, -20);

		w = (int) (AllGuiTextures.TRAIN_HUD_THROTTLE.width * (1 - displayedThrottle.getValue(partialTicks)));
		AllGuiTextures.TRAIN_HUD_THROTTLE.bind();
		int invW = AllGuiTextures.TRAIN_HUD_THROTTLE.width - w;
		GuiComponent.blit(poseStack, invW, 0, 0, AllGuiTextures.TRAIN_HUD_THROTTLE.startX + invW,
			AllGuiTextures.TRAIN_HUD_THROTTLE.startY, w, h, 256, 256);
		AllGuiTextures.TRAIN_HUD_THROTTLE_POINTER.render(poseStack,
			Math.max(1, AllGuiTextures.TRAIN_HUD_THROTTLE.width - w) - 3, -2);

		// Direction

		StructureBlockInfo info = cce.getContraption()
			.getBlocks()
			.get(localPos);
		Direction initialOrientation = cce.getInitialOrientation()
			.getCounterClockWise();
		boolean inverted = false;
		if (info != null && info.state.hasProperty(ControlsBlock.FACING))
			inverted = !info.state.getValue(ControlsBlock.FACING)
				.equals(initialOrientation);

		boolean reversing = ControlsHandler.currentlyPressed.contains(1);
		inverted ^= reversing;
		int angleOffset = (ControlsHandler.currentlyPressed.contains(2) ? -45 : 0)
			+ (ControlsHandler.currentlyPressed.contains(3) ? 45 : 0);
		if (reversing)
			angleOffset *= -1;

		float snapSize = 22.5f;
		float diff = AngleHelper.getShortestAngleDiff(cameraEntity.getYRot(), cce.yaw) + (inverted ? -90 : 90);
		if (Math.abs(diff) < 60)
			diff = 0;

		float angle = diff + angleOffset;
		float snappedAngle = (snapSize * Math.round(angle / snapSize)) % 360f;

		poseStack.translate(91, -9, 0);
		poseStack.scale(0.925f, 0.925f, 1);
		PlacementHelpers.textured(poseStack, 0, 0, 1, snappedAngle);

		poseStack.popPose();
	}

	public static boolean onScroll(double delta) {
		Carriage carriage = getCarriage();
		if (carriage == null)
			return false;

		double prevThrottle = editedThrottle == null ? carriage.train.throttle : editedThrottle;
		editedThrottle = Mth.clamp(prevThrottle + (delta > 0 ? 1 : -1) / 18f, 1 / 18f, 1);
		return true;
	}

}
