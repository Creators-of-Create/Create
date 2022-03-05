package com.simibubi.create.content.logistics.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import io.github.fabricators_of_create.porting_lib.util.KeyBindingHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LinkedControllerClientHandler {

	public static Mode MODE = Mode.IDLE;
	public static int PACKET_RATE = 5;
	public static Collection<Integer> currentlyPressed = new HashSet<>();
	private static BlockPos lecternPos;
	private static BlockPos selectedLocation = BlockPos.ZERO;
	private static Vector<KeyMapping> controls;

	private static int packetCooldown;

	public static Vector<KeyMapping> getControls() {
		if (controls == null) {
			Options gameSettings = Minecraft.getInstance().options;
			controls = new Vector<>(6);
			controls.add(gameSettings.keyUp);
			controls.add(gameSettings.keyDown);
			controls.add(gameSettings.keyLeft);
			controls.add(gameSettings.keyRight);
			controls.add(gameSettings.keyJump);
			controls.add(gameSettings.keyShift);
		}
		return controls;
	}

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
		}  else {
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
		getControls().forEach(kb -> kb.setDown(isActuallyPressed(kb)));
		packetCooldown = 0;
		selectedLocation = BlockPos.ZERO;

		if (inLectern())
			AllPackets.channel.sendToServer(new LinkedControllerStopLecternPacket(lecternPos));
		lecternPos = null;

		if (!currentlyPressed.isEmpty())
			AllPackets.channel.sendToServer(new LinkedControllerInputPacket(currentlyPressed, false));
		currentlyPressed.clear();

		LinkedControllerItemRenderer.resetButtons();
	}

	protected static boolean isActuallyPressed(KeyMapping kb) {
		return InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getWindow(),
				KeyBindingHelper.getKeyCode(kb)
				.getValue());
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

		if (inLectern() && AllBlocks.LECTERN_CONTROLLER.get().getTileEntityOptional(mc.level, lecternPos)
			.map(te -> !te.isUsedBy(mc.player))
			.orElse(true)) {
			deactivateInLectern();
			return;
		}

		if (mc.screen != null) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		if (InputConstants.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_ESCAPE)) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		Vector<KeyMapping> controls = getControls();
		Collection<Integer> pressedKeys = new HashSet<>();
		for (int i = 0; i < controls.size(); i++) {
			if (isActuallyPressed(controls.get(i)))
				pressedKeys.add(i);
		}

		Collection<Integer> newKeys = new HashSet<>(pressedKeys);
		Collection<Integer> releasedKeys = currentlyPressed;
		newKeys.removeAll(releasedKeys);
		releasedKeys.removeAll(pressedKeys);

		if (MODE == Mode.ACTIVE) {
			// Released Keys
			if (!releasedKeys.isEmpty()) {
				AllPackets.channel.sendToServer(new LinkedControllerInputPacket(releasedKeys, false, lecternPos));
				AllSoundEvents.CONTROLLER_CLICK.playAt(player.level, player.blockPosition(), 1f, .5f, true);
			}

			// Newly Pressed Keys
			if (!newKeys.isEmpty()) {
				AllPackets.channel.sendToServer(new LinkedControllerInputPacket(newKeys, true, lecternPos));
				packetCooldown = PACKET_RATE;
				AllSoundEvents.CONTROLLER_CLICK.playAt(player.level, player.blockPosition(), 1f, .75f, true);
			}

			// Keepalive Pressed Keys
			if (packetCooldown == 0) {
				if (!pressedKeys.isEmpty()) {
					AllPackets.channel.sendToServer(new LinkedControllerInputPacket(pressedKeys, true, lecternPos));
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
				LinkBehaviour linkBehaviour = TileEntityBehaviour.get(mc.level, selectedLocation, LinkBehaviour.TYPE);
				if (linkBehaviour != null) {
					AllPackets.channel.sendToServer(new LinkedControllerBindPacket(integer, selectedLocation));
					Lang.sendStatus(mc.player, "linked_controller.key_bound", controls.get(integer)
						.getTranslatedKeyMessage()
						.getString());
				}
				MODE = Mode.IDLE;
				break;
			}
		}

		currentlyPressed = pressedKeys;
		controls.forEach(kb -> kb.setDown(false));
	}

	public static void renderOverlay(PoseStack poseStack, float partialTicks, Window window) {
		if (MODE != Mode.BIND)
			return;
		Minecraft mc = Minecraft.getInstance();

		poseStack.pushPose();
		Screen tooltipScreen = new Screen(TextComponent.EMPTY) {};
		tooltipScreen.init(mc, window.getGuiScaledWidth(), window.getGuiScaledHeight());

		Object[] keys = new Object[6];
		Vector<KeyMapping> controls = getControls();
		for (int i = 0; i < controls.size(); i++) {
			KeyMapping keyBinding = controls.get(i);
			keys[i] = keyBinding.getTranslatedKeyMessage()
				.getString();
		}

		List<Component> list = new ArrayList<>();
		list.add(Lang.createTranslationTextComponent("linked_controller.bind_mode")
			.withStyle(ChatFormatting.GOLD));
		list.addAll(
			TooltipHelper.cutTextComponent(Lang.createTranslationTextComponent("linked_controller.press_keybind", keys),
				ChatFormatting.GRAY, ChatFormatting.GRAY));

		int width = 0;
		int height = list.size() * mc.font.lineHeight;
		for (Component iTextComponent : list)
			width = Math.max(width, mc.font.width(iTextComponent));
		int x = (mc.getWindow().getGuiScaledWidth() / 3) - width / 2;
		int y = mc.getWindow().getGuiScaledHeight() - height;

		// TODO
		tooltipScreen.renderComponentTooltip(poseStack, list, x, y);

		poseStack.popPose();
	}

	public enum Mode {
		IDLE, ACTIVE, BIND
	}

}
