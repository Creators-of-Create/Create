package com.simibubi.create.foundation.blockEntity.behaviour;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ValueSettingsClient implements IGuiOverlay {
	
	private Minecraft mc;

	public int interactHeldTicks = -1;
	public BlockPos interactHeldPos = null;
	public BehaviourType<?> interactHeldBehaviour = null;
	public InteractionHand interactHeldHand = null;
	public Direction interactHeldFace = null;

	public List<MutableComponent> lastHoverTip;
	public int hoverTicks;
	public int hoverWarmup;

	public ValueSettingsClient() {
		mc = Minecraft.getInstance();
	}

	public void cancelIfWarmupAlreadyStarted(PlayerInteractEvent.RightClickBlock event) {
		if (interactHeldTicks != -1 && event.getPos()
			.equals(interactHeldPos)) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
		}
	}

	public void startInteractionWith(BlockPos pos, BehaviourType<?> behaviourType, InteractionHand hand,
		Direction side) {
		interactHeldTicks = 0;
		interactHeldPos = pos;
		interactHeldBehaviour = behaviourType;
		interactHeldHand = hand;
		interactHeldFace = side;
	}

	public void cancelInteraction() {
		interactHeldTicks = -1;
	}

	public void tick() {
		if (hoverWarmup > 0)
			hoverWarmup--;
		if (hoverTicks > 0)
			hoverTicks--;
		if (interactHeldTicks == -1)
			return;
		Player player = mc.player;

		if (!ValueSettingsInputHandler.canInteract(player) || AllBlocks.CLIPBOARD.isIn(player.getMainHandItem())) {
			cancelInteraction();
			return;
		}
		HitResult hitResult = mc.hitResult;
		if (!(hitResult instanceof BlockHitResult blockHitResult) || !blockHitResult.getBlockPos()
			.equals(interactHeldPos)) {
			cancelInteraction();
			return;
		}
		BlockEntityBehaviour behaviour = BlockEntityBehaviour.get(mc.level, interactHeldPos, interactHeldBehaviour);
		if (!(behaviour instanceof ValueSettingsBehaviour valueSettingBehaviour)
			|| !valueSettingBehaviour.testHit(blockHitResult.getLocation())) {
			cancelInteraction();
			return;
		}
		if (!mc.options.keyUse.isDown()) {
			AllPackets.getChannel()
				.sendToServer(
					new ValueSettingsPacket(interactHeldPos, 0, 0, interactHeldHand, interactHeldFace, false));
			cancelInteraction();
			return;
		}

		if (interactHeldTicks > 3)
			player.swinging = false;
		if (interactHeldTicks++ < 5)
			return;
		ScreenOpener
			.open(new ValueSettingsScreen(interactHeldPos, valueSettingBehaviour.createBoard(player, blockHitResult),
				valueSettingBehaviour.getValueSettings(), valueSettingBehaviour::newSettingHovered));
		interactHeldTicks = -1;
	}

	public void showHoverTip(List<MutableComponent> tip) {
		if (mc.screen != null)
			return;
		if (hoverWarmup < 6) {
			hoverWarmup += 2;
			return;
		} else
			hoverWarmup++;
		hoverTicks = hoverTicks == 0 ? 11 : Math.max(hoverTicks, 6);
		lastHoverTip = tip;
	}

	@Override
	public void render(ForgeGui gui, PoseStack poseStack, float partialTicks, int width, int height) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || !ValueSettingsInputHandler.canInteract(mc.player))
			return;
		if (hoverTicks == 0 || lastHoverTip == null)
			return;

		int x = width / 2;
		int y = height - 75 - lastHoverTip.size() * 12;
		float alpha = hoverTicks > 5 ? (11 - hoverTicks) / 5f : Math.min(1, hoverTicks / 5f);

		Color color = new Color(0xffffff);
		Color titleColor = new Color(0xFBDC7D);
		color.setAlpha(alpha);
		titleColor.setAlpha(alpha);

		for (int i = 0; i < lastHoverTip.size(); i++) {
			MutableComponent mutableComponent = lastHoverTip.get(i);
			mc.font.drawShadow(poseStack, mutableComponent, x - mc.font.width(mutableComponent) / 2, y,
				(i == 0 ? titleColor : color).getRGB());
			y += 12;
		}
	}

}
