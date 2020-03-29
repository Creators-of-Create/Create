package com.simibubi.create.modules.schematics.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllPackets;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.gui.ToolSelectionScreen;
import com.simibubi.create.foundation.packet.NbtPacket;
import com.simibubi.create.foundation.type.Cuboid;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.schematics.SchematicWorld;
import com.simibubi.create.modules.schematics.client.tools.Tools;
import com.simibubi.create.modules.schematics.item.SchematicItem;
import com.simibubi.create.modules.schematics.packet.SchematicPlacePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

public class SchematicHandler {

	public Template cachedSchematic;
	public String cachedSchematicName;
	public PlacementSettings cachedSettings;

	public BlockPos anchor;
	public BlockPos size;
	public boolean active;
	public boolean deployed;
	public int slot;
	public ItemStack item;

	private final List<String> mirrors = Arrays.asList("none", "leftRight", "frontBack");
	private final List<String> rotations = Arrays.asList("none", "cw90", "cw180", "cw270");

	public Tools currentTool;
	public ToolSelectionScreen selectionScreen;

	public static final int SYNC_DELAY = 20;
	public int syncCooldown;

	private BlueprintHotbarOverlay overlay;

	public SchematicHandler() {
		currentTool = Tools.Deploy;
		overlay = new BlueprintHotbarOverlay();
		selectionScreen = new ToolSelectionScreen(ImmutableList.of(Tools.Deploy), this::equip);
	}

	public void tick() {
		ClientPlayerEntity player = Minecraft.getInstance().player;

		ItemStack stack = findBlueprintInHand(player);
		if (stack == null) {
			active = false;
			syncCooldown = 0;
			if (item != null && itemLost(player)) {
				slot = 0;
				item = null;
				CreateClient.schematicHologram.setActive(false);
			}
			return;
		}

		// Newly equipped
		if (!active || !stack.getTag().getString("File").equals(cachedSchematicName)) {
			loadSettings(stack);
			cachedSchematicName = stack.getTag().getString("File");
			active = true;
			if (deployed) {
				Tools toolBefore = currentTool;
				selectionScreen = new ToolSelectionScreen(Tools.getTools(player.isCreative()), this::equip);
				if (toolBefore != null) {
					selectionScreen.setSelectedElement(toolBefore);
					equip(toolBefore);
				}
			} else
				selectionScreen = new ToolSelectionScreen(ImmutableList.of(Tools.Deploy), this::equip);
			sync();
		}

		if (!active)
			return;

		if (syncCooldown > 0)
			syncCooldown--;
		if (syncCooldown == 1)
			sync();

		selectionScreen.update();
		currentTool.getTool().updateSelection();
	}

	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		// TODO 1.15 buffered render
		if (!active)
			return;
		if (Minecraft.getInstance().player.isSneaking())
			return;

		TessellatorHelper.prepareForDrawing();
		currentTool.getTool().renderTool();
		TessellatorHelper.cleanUpAfterDrawing();
	}

	public void renderOverlay() {
		if (!active)
			return;
		if (item != null)
			overlay.renderOn(slot);

		currentTool.getTool().renderOverlay();
		selectionScreen.renderPassive(Minecraft.getInstance().getRenderPartialTicks());
	}

	public void onMouseInput(int button, boolean pressed) {
		if (!active)
			return;
		if (!pressed || button != 1)
			return;
		if (Minecraft.getInstance().player.isSneaking())
			return;

		currentTool.getTool().handleRightClick();
	}

	public void onKeyInput(int key, boolean pressed) {
		if (!active)
			return;
		if (key != AllKeys.TOOL_MENU.getBoundCode())
			return;

		if (pressed && !selectionScreen.focused)
			selectionScreen.focused = true;

		if (!pressed && selectionScreen.focused) {
			selectionScreen.focused = false;
			selectionScreen.onClose();
		}
	}

	public boolean mouseScrolled(double delta) {
		if (!active)
			return false;
		if (Minecraft.getInstance().player.isSneaking())
			return false;
		if (selectionScreen.focused) {
			selectionScreen.cycle((int) delta);
			return true;
		}
		if (AllKeys.ACTIVATE_TOOL.isPressed()) {
			return currentTool.getTool().handleMouseWheel(delta);
		}

		return false;
	}

	private ItemStack findBlueprintInHand(PlayerEntity player) {
		ItemStack stack = player.getHeldItemMainhand();
		if (!AllItems.BLUEPRINT.typeOf(stack))
			return null;
		if (!stack.hasTag())
			return null;

		item = stack;
		slot = player.inventory.currentItem;
		return stack;
	}

	private boolean itemLost(PlayerEntity player) {
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (!player.inventory.getStackInSlot(i).isItemEqual(item))
				continue;
			if (!ItemStack.areItemStackTagsEqual(player.inventory.getStackInSlot(i), item))
				continue;
			return false;
		}
		return true;
	}

	public void markDirty() {
		syncCooldown = SYNC_DELAY;
		CreateClient.schematicHologram.setActive(false);
	}

	public void sync() {
		message(Lang.translate("schematics.synchronizing"));
		AllPackets.channel.sendToServer(new NbtPacket(item, slot));

		if (deployed) {
			Template schematic = SchematicItem.getSchematic(item);

			if (schematic.getSize().equals(BlockPos.ZERO))
				return;

			SchematicWorld w = new SchematicWorld(new HashMap<>(), new Cuboid(), anchor, Minecraft.getInstance().world);
			PlacementSettings settings = cachedSettings.copy();
			settings.setBoundingBox(null);
			schematic.addBlocksToWorld(w, anchor, settings);
			CreateClient.schematicHologram.startHologram(w);
		}
	}

	public void equip(Tools tool) {
		this.currentTool = tool;
		currentTool.getTool().init();
	}

	public void loadSettings(ItemStack blueprint) {
		CompoundNBT tag = blueprint.getTag();
		cachedSettings = new PlacementSettings();
		cachedSettings.setRotation(Rotation.valueOf(tag.getString("Rotation")));
		cachedSettings.setMirror(Mirror.valueOf(tag.getString("Mirror")));

		deployed = tag.getBoolean("Deployed");
		if (deployed)
			anchor = NBTUtil.readBlockPos(tag.getCompound("Anchor"));

		size = NBTUtil.readBlockPos(tag.getCompound("Bounds"));
	}

	public void flip(Axis axis) {

		Rotation r = cachedSettings.getRotation();
		boolean rotationAt90s = r == Rotation.CLOCKWISE_90 || r == Rotation.COUNTERCLOCKWISE_90;
		Mirror mirror = axis == Axis.Z ^ rotationAt90s ? Mirror.FRONT_BACK : Mirror.LEFT_RIGHT;

		BlockPos coordModifier = new BlockPos((r == Rotation.NONE || r == Rotation.COUNTERCLOCKWISE_90) ? 1 : -1, 0,
				(r == Rotation.NONE || r == Rotation.CLOCKWISE_90) ? 1 : -1);
		BlockPos anchorOffset = axis == Axis.Z
				? new BlockPos(((rotationAt90s ? size.getZ() : size.getX()) - 1) * coordModifier.getX(), 0, 0)
				: new BlockPos(0, 0, ((!rotationAt90s ? size.getZ() : size.getX()) - 1) * coordModifier.getZ());

		Mirror m = cachedSettings.getMirror();

		if (m == Mirror.NONE) {
			cachedSettings.setMirror(mirror);
			anchor = anchor.add(anchorOffset);
			message(Lang.translate("schematic.mirror") + ": "
					+ Lang.translate("schematic.mirror." + mirrors.get(cachedSettings.getMirror().ordinal())));

		} else if (m == mirror) {
			cachedSettings.setMirror(Mirror.NONE);
			anchor = anchor.subtract(anchorOffset);
			message(Lang.translate("schematic.mirror") + ": "
					+ Lang.translate("schematic.mirror." + mirrors.get(cachedSettings.getMirror().ordinal())));

		} else if (m != mirror) {
			cachedSettings.setMirror(Mirror.NONE);
			anchor = anchor.add(anchorOffset);
			cachedSettings.setRotation(r.add(Rotation.CLOCKWISE_180));
			message(Lang.translate("schematic.mirror") + ": "
					+ Lang.translate("schematic.mirror." + mirrors.get(cachedSettings.getMirror().ordinal())) + ", "
					+ Lang.translate("schematic.rotation") + ": "
					+ Lang.translate("schematic.rotation." + rotations.get(cachedSettings.getRotation().ordinal())));
		}

		item.getTag().put("Anchor", NBTUtil.writeBlockPos(anchor));
		item.getTag().putString("Mirror", cachedSettings.getMirror().name());
		item.getTag().putString("Rotation", r.name());

		markDirty();
	}

	public void message(String msg) {
		Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(msg), true);
	}

	public void rotate(Rotation rotation) {
		Rotation r = cachedSettings.getRotation();
		BlockPos center = centerOfSchematic();
		cachedSettings.setRotation(r.add(rotation));
		BlockPos diff = center.subtract(anchor);
		BlockPos move = diff.subtract(diff.rotate(rotation));
		anchor = anchor.add(move);

		item.getTag().put("Anchor", NBTUtil.writeBlockPos(anchor));
		item.getTag().putString("Rotation", cachedSettings.getRotation().name());

		message(Lang.translate("schematic.rotation") + ": "
				+ Lang.translate("schematic.rotation." + rotations.get(cachedSettings.getRotation().ordinal())));

		markDirty();
	}

	public void setMirror(Mirror mirror) {
		cachedSettings.setMirror(mirror);
		item.getTag().putString("Mirror", cachedSettings.getMirror().name());
		markDirty();
	}

	public void setRotation(Rotation rotation) {
		cachedSettings.setRotation(rotation);
		item.getTag().putString("Rotation", cachedSettings.getRotation().name());
		markDirty();
	}

	public void moveTo(BlockPos anchor) {
		if (!deployed)
			selectionScreen = new ToolSelectionScreen(Tools.getTools(Minecraft.getInstance().player.isCreative()),
					this::equip);

		deployed = true;
		this.anchor = anchor;
		item.getTag().putBoolean("Deployed", true);
		item.getTag().put("Anchor", NBTUtil.writeBlockPos(anchor));
		markDirty();
	}

	public void printInstantly() {
		AllPackets.channel.sendToServer(new SchematicPlacePacket(item.copy()));
		CompoundNBT nbt = item.getTag();
		nbt.putBoolean("Deployed", false);
		item.setTag(nbt);
		CreateClient.schematicHologram.setActive(false);
		active = false;
	}

	public BlockPos getTransformedSize() {
		BlockPos flipped = size;
		if (cachedSettings.getMirror() == Mirror.FRONT_BACK)
			flipped = new BlockPos(-flipped.getX(), flipped.getY(), flipped.getZ());
		if (cachedSettings.getMirror() == Mirror.LEFT_RIGHT)
			flipped = new BlockPos(flipped.getX(), flipped.getY(), -flipped.getZ());

		BlockPos rotate = flipped.rotate(cachedSettings.getRotation());
		return rotate;
	}

	public BlockPos getTransformedAnchor() {
		BlockPos anchor = this.anchor;
		Rotation r = cachedSettings.getRotation();

		BlockPos flipOffset = BlockPos.ZERO;
		if (cachedSettings.getMirror() == Mirror.FRONT_BACK)
			flipOffset = new BlockPos(1, 0, 0);
		if (cachedSettings.getMirror() == Mirror.LEFT_RIGHT)
			flipOffset = new BlockPos(0, 0, 1);

		flipOffset = flipOffset.rotate(r);
		anchor = anchor.add(flipOffset);

		if (r == Rotation.CLOCKWISE_90 || r == Rotation.CLOCKWISE_180)
			anchor = anchor.add(1, 0, 0);
		if (r == Rotation.COUNTERCLOCKWISE_90 || r == Rotation.CLOCKWISE_180)
			anchor = anchor.add(0, 0, 1);
		return anchor;
	}

	public BlockPos centerOfSchematic() {
		BlockPos size = getTransformedSize();
		BlockPos center = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
		return anchor.add(center);
	}

}
