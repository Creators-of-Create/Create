package com.simibubi.create.content.logistics.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
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
	private static BlockPos selectedLocation = BlockPos.ZERO;
	private static Vector<KeyBinding> controls;

	private static int packetCooldown;

	public static Vector<KeyBinding> getControls() {
		if (controls == null) {
			GameSettings gameSettings = Minecraft.getInstance().gameSettings;
			controls = new Vector<>(6);
			controls.add(gameSettings.keyBindForward);
			controls.add(gameSettings.keyBindBack);
			controls.add(gameSettings.keyBindLeft);
			controls.add(gameSettings.keyBindRight);
			controls.add(gameSettings.keyBindJump);
			controls.add(gameSettings.keySneak);
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
		if (MODE == Mode.IDLE)
			MODE = Mode.ACTIVE;
		else {
			MODE = Mode.IDLE;
			onReset();
		}
	}

	protected static void onReset() {
		getControls().forEach(kb -> kb.setPressed(isActuallyPressed(kb)));
		packetCooldown = 0;
		selectedLocation = BlockPos.ZERO;

		if (!currentlyPressed.isEmpty())
			AllPackets.channel.sendToServer(new LinkedControllerInputPacket(currentlyPressed, false));
		currentlyPressed.clear();
	}

	protected static boolean isActuallyPressed(KeyBinding kb) {
		return InputMappings.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getHandle(),
			kb.getKey()
				.getKeyCode());
	}

	public static void tick() {
		LinkedControllerItemRenderer.tick();
		if (MODE == Mode.IDLE)
			return;
		if (packetCooldown > 0)
			packetCooldown--;

		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		ItemStack heldItem = player.getHeldItemMainhand();

		if (player.isSpectator()) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		if (!AllItems.LINKED_CONTROLLER.isIn(heldItem)) {
			heldItem = player.getHeldItemOffhand();
			if (!AllItems.LINKED_CONTROLLER.isIn(heldItem)) {
				MODE = Mode.IDLE;
				onReset();
				return;
			}
		}

		if (mc.currentScreen != null) {
			MODE = Mode.IDLE;
			onReset();
			return;
		}

		if (InputMappings.isKeyDown(mc.getWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE)) {
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
				AllPackets.channel.sendToServer(new LinkedControllerInputPacket(releasedKeys, false));
				AllSoundEvents.CONTROLLER_CLICK.playAt(player.world, player.getBlockPos(), 1f, .5f, true);
			}

			// Newly Pressed Keys
			if (!newKeys.isEmpty()) {
				AllPackets.channel.sendToServer(new LinkedControllerInputPacket(newKeys, true));
				packetCooldown = PACKET_RATE;
				AllSoundEvents.CONTROLLER_CLICK.playAt(player.world, player.getBlockPos(), 1f, .75f, true);
			}

			// Keepalive Pressed Keys
			if (packetCooldown == 0) {
				if (!pressedKeys.isEmpty()) {
					AllPackets.channel.sendToServer(new LinkedControllerInputPacket(pressedKeys, true));
					packetCooldown = PACKET_RATE;
				}
			}
		}

		if (MODE == Mode.BIND) {
			VoxelShape shape = mc.world.getBlockState(selectedLocation)
				.getShape(mc.world, selectedLocation);
			if (!shape.isEmpty())
				CreateClient.OUTLINER.showAABB("controller", shape.getBoundingBox()
					.offset(selectedLocation))
					.colored(0xB73C2D)
					.lineWidth(1 / 16f);

			for (Integer integer : newKeys) {
				LinkBehaviour linkBehaviour = TileEntityBehaviour.get(mc.world, selectedLocation, LinkBehaviour.TYPE);
				if (linkBehaviour != null) {
					AllPackets.channel.sendToServer(new LinkedControllerBindPacket(integer, selectedLocation));
					Lang.sendStatus(mc.player, "linked_controller.key_bound", controls.get(integer)
						.getBoundKeyLocalizedText()
						.getString());
				}
				MODE = Mode.IDLE;
				break;
			}
		}

		currentlyPressed = pressedKeys;
		controls.forEach(kb -> kb.setPressed(false));
	}

	public static void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay,
		float partialTicks) {
		if (MODE != Mode.BIND)
			return;
		Minecraft mc = Minecraft.getInstance();

		ms.push();
		Screen tooltipScreen = new TooltipScreen(null);
		tooltipScreen.init(mc, mc.getWindow()
			.getScaledWidth(),
			mc.getWindow()
				.getScaledHeight());

		Object[] keys = new Object[6];
		Vector<KeyBinding> controls = getControls();
		for (int i = 0; i < controls.size(); i++) {
			KeyBinding keyBinding = controls.get(i);
			keys[i] = keyBinding.getBoundKeyLocalizedText()
				.getString();
		}

		List<ITextComponent> list = new ArrayList<>();
		list.add(Lang.createTranslationTextComponent("linked_controller.bind_mode")
			.formatted(TextFormatting.GOLD));
		list.addAll(
			TooltipHelper.cutTextComponent(Lang.createTranslationTextComponent("linked_controller.press_keybind", keys),
				TextFormatting.GRAY, TextFormatting.GRAY));

		int width = 0;
		int height = list.size() * mc.fontRenderer.FONT_HEIGHT;
		for (ITextComponent iTextComponent : list)
			width = Math.max(width, mc.fontRenderer.getWidth(iTextComponent));
		int x = (tooltipScreen.width / 3) - width / 2;
		int y = tooltipScreen.height - height;

		tooltipScreen.renderTooltip(ms, list, x, y);

		ms.pop();

	}

}
