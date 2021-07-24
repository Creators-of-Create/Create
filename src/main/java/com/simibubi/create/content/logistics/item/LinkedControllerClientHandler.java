package com.simibubi.create.content.logistics.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.goggles.GoggleOverlayRenderer.TooltipScreen;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.linked.LinkBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class LinkedControllerClientHandler {

	enum Mode {
		IDLE, ACTIVE, BIND
	}

	public static Mode MODE = Mode.IDLE;
	public static int PACKET_RATE = 5;
	public static Collection<Integer> currentlyPressed = new HashSet<>();
	private static BlockPos lecternPos;
	private static BlockPos selectedLocation = BlockPos.ZERO;
	private static Vector<KeyBinding> controls;

	private static int packetCooldown;

	public static Vector<KeyBinding> getControls() {
		if (controls == null) {
			GameSettings gameSettings = Minecraft.getInstance().options;
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
	}

	protected static boolean isActuallyPressed(KeyBinding kb) {
		return InputMappings.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getWindow(),
			kb.getKey()
				.getValue());
	}

	public static void tick() {
		LinkedControllerItemRenderer.tick();
		if (MODE == Mode.IDLE)
			return;
		if (packetCooldown > 0)
			packetCooldown--;

		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
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

		if (InputMappings.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_ESCAPE)) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		Vector<KeyBinding> controls = getControls();
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

	public static void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay,
		float partialTicks) {
		if (MODE != Mode.BIND)
			return;
		Minecraft mc = Minecraft.getInstance();

		ms.pushPose();
		Screen tooltipScreen = new TooltipScreen(null);
		tooltipScreen.init(mc, mc.getWindow()
			.getGuiScaledWidth(),
			mc.getWindow()
				.getGuiScaledHeight());

		Object[] keys = new Object[6];
		Vector<KeyBinding> controls = getControls();
		for (int i = 0; i < controls.size(); i++) {
			KeyBinding keyBinding = controls.get(i);
			keys[i] = keyBinding.getTranslatedKeyMessage()
				.getString();
		}

		List<ITextComponent> list = new ArrayList<>();
		list.add(Lang.createTranslationTextComponent("linked_controller.bind_mode")
			.withStyle(TextFormatting.GOLD));
		list.addAll(
			TooltipHelper.cutTextComponent(Lang.createTranslationTextComponent("linked_controller.press_keybind", keys),
				TextFormatting.GRAY, TextFormatting.GRAY));

		int width = 0;
		int height = list.size() * mc.font.lineHeight;
		for (ITextComponent iTextComponent : list)
			width = Math.max(width, mc.font.width(iTextComponent));
		int x = (tooltipScreen.width / 3) - width / 2;
		int y = tooltipScreen.height - height;

		tooltipScreen.renderComponentTooltip(ms, list, x, y);

		ms.popPose();

	}

}
