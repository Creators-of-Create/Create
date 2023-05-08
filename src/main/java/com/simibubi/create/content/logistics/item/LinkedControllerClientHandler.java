package com.simibubi.create.content.logistics.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.linked.LinkBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipHelper.Palette;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.ControlsUtil;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class LinkedControllerClientHandler {

	public static final IGuiOverlay OVERLAY = LinkedControllerClientHandler::renderOverlay;

	public static Mode MODE = Mode.IDLE;
	public static int PACKET_RATE = 5;
	public static Collection<Integer> currentlyPressed = new HashSet<>();
	private static BlockPos lecternPos;
	private static BlockPos selectedLocation = BlockPos.ZERO;
	private static int packetCooldown;

	public static void toggleBindMode(BlockPos location) {
		if (MODE == Mode.IDLE) {
			MODE = Mode.BIND;
			selectedLocation = location;
		} else {
			MODE = Mode.IDLE;
			onReset();
		}
	}

	public static void toggle() {
		if (MODE == Mode.IDLE) {
			MODE = Mode.ACTIVE;
			lecternPos = null;
		} else {
			MODE = Mode.IDLE;
			onReset();
		}
	}

	public static void activateInLectern(BlockPos lecternAt) {
		if (MODE == Mode.IDLE) {
			MODE = Mode.ACTIVE;
			lecternPos = lecternAt;
		}
	}

	public static void deactivateInLectern() {
		if (MODE == Mode.ACTIVE && inLectern()) {
			MODE = Mode.IDLE;
			onReset();
		}
	}

	public static boolean inLectern() {
		return lecternPos != null;
	}

	protected static void onReset() {
		ControlsUtil.getControls()
			.forEach(kb -> kb.setDown(ControlsUtil.isActuallyPressed(kb)));
		packetCooldown = 0;
		selectedLocation = BlockPos.ZERO;

		if (inLectern())
			AllPackets.getChannel().sendToServer(new LinkedControllerStopLecternPacket(lecternPos));
		lecternPos = null;

		if (!currentlyPressed.isEmpty())
			AllPackets.getChannel().sendToServer(new LinkedControllerInputPacket(currentlyPressed, false));
		currentlyPressed.clear();

		LinkedControllerItemRenderer.resetButtons();
	}

	public static void tick() {
		LinkedControllerItemRenderer.tick();

		if (MODE == Mode.IDLE)
			return;
		if (packetCooldown > 0)
			packetCooldown--;

		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		ItemStack heldItem = player.getMainHandItem();

		if (player.isSpectator()) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		if (!inLectern() && !AllItems.LINKED_CONTROLLER.isIn(heldItem)) {
			heldItem = player.getOffhandItem();
			if (!AllItems.LINKED_CONTROLLER.isIn(heldItem)) {
				MODE = Mode.IDLE;
				onReset();
				return;
			}
		}

		if (inLectern() && AllBlocks.LECTERN_CONTROLLER.get()
			.getBlockEntityOptional(mc.level, lecternPos)
			.map(be -> !be.isUsedBy(mc.player))
			.orElse(true)) {
			deactivateInLectern();
			return;
		}

		if (mc.screen != null) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		if (InputConstants.isKeyDown(mc.getWindow()
			.getWindow(), GLFW.GLFW_KEY_ESCAPE)) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		Vector<KeyMapping> controls = ControlsUtil.getControls();
		Collection<Integer> pressedKeys = new HashSet<>();
		for (int i = 0; i < controls.size(); i++) {
			if (ControlsUtil.isActuallyPressed(controls.get(i)))
				pressedKeys.add(i);
		}

		Collection<Integer> newKeys = new HashSet<>(pressedKeys);
		Collection<Integer> releasedKeys = currentlyPressed;
		newKeys.removeAll(releasedKeys);
		releasedKeys.removeAll(pressedKeys);

		if (MODE == Mode.ACTIVE) {
			// Released Keys
			if (!releasedKeys.isEmpty()) {
				AllPackets.getChannel().sendToServer(new LinkedControllerInputPacket(releasedKeys, false, lecternPos));
				AllSoundEvents.CONTROLLER_CLICK.playAt(player.level, player.blockPosition(), 1f, .5f, true);
			}

			// Newly Pressed Keys
			if (!newKeys.isEmpty()) {
				AllPackets.getChannel().sendToServer(new LinkedControllerInputPacket(newKeys, true, lecternPos));
				packetCooldown = PACKET_RATE;
				AllSoundEvents.CONTROLLER_CLICK.playAt(player.level, player.blockPosition(), 1f, .75f, true);
			}

			// Keepalive Pressed Keys
			if (packetCooldown == 0) {
				if (!pressedKeys.isEmpty()) {
					AllPackets.getChannel().sendToServer(new LinkedControllerInputPacket(pressedKeys, true, lecternPos));
					packetCooldown = PACKET_RATE;
				}
			}
		}

		if (MODE == Mode.BIND) {
			VoxelShape shape = mc.level.getBlockState(selectedLocation)
				.getShape(mc.level, selectedLocation);
			if (!shape.isEmpty())
				CreateClient.OUTLINER.showAABB("controller", shape.bounds()
					.move(selectedLocation))
					.colored(0xB73C2D)
					.lineWidth(1 / 16f);

			for (Integer integer : newKeys) {
				LinkBehaviour linkBehaviour = BlockEntityBehaviour.get(mc.level, selectedLocation, LinkBehaviour.TYPE);
				if (linkBehaviour != null) {
					AllPackets.getChannel().sendToServer(new LinkedControllerBindPacket(integer, selectedLocation));
					Lang.translate("linked_controller.key_bound", controls.get(integer)
						.getTranslatedKeyMessage()
						.getString())
						.sendStatus(mc.player);
				}
				MODE = Mode.IDLE;
				break;
			}
		}

		currentlyPressed = pressedKeys;
		controls.forEach(kb -> kb.setDown(false));
	}

	public static void renderOverlay(ForgeGui gui, PoseStack poseStack, float partialTicks, int width1,
		int height1) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui)
			return;

		if (MODE != Mode.BIND)
			return;

		poseStack.pushPose();
		Screen tooltipScreen = new Screen(Components.immutableEmpty()) {
		};
		tooltipScreen.init(mc, width1, height1);

		Object[] keys = new Object[6];
		Vector<KeyMapping> controls = ControlsUtil.getControls();
		for (int i = 0; i < controls.size(); i++) {
			KeyMapping keyBinding = controls.get(i);
			keys[i] = keyBinding.getTranslatedKeyMessage()
				.getString();
		}

		List<Component> list = new ArrayList<>();
		list.add(Lang.translateDirect("linked_controller.bind_mode")
			.withStyle(ChatFormatting.GOLD));
		list.addAll(TooltipHelper.cutTextComponent(Lang.translateDirect("linked_controller.press_keybind", keys),
			Palette.ALL_GRAY));

		int width = 0;
		int height = list.size() * mc.font.lineHeight;
		for (Component iTextComponent : list)
			width = Math.max(width, mc.font.width(iTextComponent));
		int x = (width1 / 3) - width / 2;
		int y = height1 - height - 24;

		// TODO
		tooltipScreen.renderComponentTooltip(poseStack, list, x, y);

		poseStack.popPose();
	}

	public enum Mode {
		IDLE, ACTIVE, BIND
	}

}
