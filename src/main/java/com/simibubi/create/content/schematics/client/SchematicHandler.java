package com.simibubi.create.content.schematics.client;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.client.tools.Tools;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.foundation.gui.ToolSelectionScreen;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.networking.NbtPacket;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

public class SchematicHandler {

	private String displayedSchematic;
	private SchematicTransformation transformation;
	private AxisAlignedBB bounds;
	private boolean deployed;
	private boolean active;
	private Tools currentTool;

	private static final int SYNC_DELAY = 10;
	private int syncCooldown;
	private int activeHotbarSlot;
	private ItemStack activeSchematicItem;
	private AABBOutline outline;

	private SchematicRenderer renderer;
	private SchematicHotbarSlotOverlay overlay;
	private ToolSelectionScreen selectionScreen;

	public SchematicHandler() {
		overlay = new SchematicHotbarSlotOverlay();
		renderer = new SchematicRenderer();
		currentTool = Tools.Deploy;
		selectionScreen = new ToolSelectionScreen(ImmutableList.of(Tools.Deploy), this::equip);
		transformation = new SchematicTransformation();
	}

	public void tick() {
		ClientPlayerEntity player = Minecraft.getInstance().player;

		if (activeSchematicItem != null && transformation != null)
			transformation.tick();

		ItemStack stack = findBlueprintInHand(player);
		if (stack == null) {
			active = false;
			syncCooldown = 0;
			if (activeSchematicItem != null && itemLost(player)) {
				activeHotbarSlot = 0;
				activeSchematicItem = null;
				renderer.setActive(false);
			}
			return;
		}

		if (!active || !stack.getTag()
			.getString("File")
			.equals(displayedSchematic))
			init(player, stack);
		if (!active)
			return;

		renderer.tick();
		if (syncCooldown > 0)
			syncCooldown--;
		if (syncCooldown == 1)
			sync();

		selectionScreen.update();
		currentTool.getTool()
			.updateSelection();
	}

	private void init(ClientPlayerEntity player, ItemStack stack) {
		loadSettings(stack);
		displayedSchematic = stack.getTag()
			.getString("File");
		active = true;
		if (deployed) {
			setupRenderer();
			Tools toolBefore = currentTool;
			selectionScreen = new ToolSelectionScreen(Tools.getTools(player.isCreative()), this::equip);
			if (toolBefore != null) {
				selectionScreen.setSelectedElement(toolBefore);
				equip(toolBefore);
			}
		} else
			selectionScreen = new ToolSelectionScreen(ImmutableList.of(Tools.Deploy), this::equip);
	}

	private void setupRenderer() {
		Template schematic = SchematicItem.loadSchematic(activeSchematicItem);
		if (schematic.getSize()
			.equals(BlockPos.ZERO))
			return;

		SchematicWorld w = new SchematicWorld(BlockPos.ZERO, Minecraft.getInstance().world);
		schematic.addBlocksToWorld(w, BlockPos.ZERO, new PlacementSettings());
		renderer.display(w);
	}

	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		boolean present = activeSchematicItem != null;
		if (!active && !present)
			return;

		if (active) {
			ms.push();
			currentTool.getTool()
				.renderTool(ms, buffer);
			ms.pop();
		}

		ms.push();
		transformation.applyGLTransformations(ms);
		renderer.render(ms, buffer);
		if (active)
			currentTool.getTool()
				.renderOnSchematic(ms, buffer);
		ms.pop();

	}

	public void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		if (!active)
			return;
		if (activeSchematicItem != null)
			this.overlay.renderOn(activeHotbarSlot);

		currentTool.getTool()
			.renderOverlay(ms, buffer);
		selectionScreen.renderPassive(Minecraft.getInstance()
			.getRenderPartialTicks());
	}

	public void onMouseInput(int button, boolean pressed) {
		if (!active)
			return;
		if (!pressed || button != 1)
			return;
		if (Minecraft.getInstance().player.isSneaking())
			return;

		currentTool.getTool()
			.handleRightClick();
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
		if (!active || Minecraft.getInstance().player.isSneaking())
			return false;

		if (selectionScreen.focused) {
			selectionScreen.cycle((int) delta);
			return true;
		}
		if (AllKeys.ACTIVATE_TOOL.isPressed())
			return currentTool.getTool()
				.handleMouseWheel(delta);
		return false;
	}

	private ItemStack findBlueprintInHand(PlayerEntity player) {
		ItemStack stack = player.getHeldItemMainhand();
		if (!AllItems.typeOf(AllItems.SCHEMATIC, stack))
			return null;
		if (!stack.hasTag())
			return null;

		activeSchematicItem = stack;
		activeHotbarSlot = player.inventory.currentItem;
		return stack;
	}

	private boolean itemLost(PlayerEntity player) {
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (!player.inventory.getStackInSlot(i)
				.isItemEqual(activeSchematicItem))
				continue;
			if (!ItemStack.areItemStackTagsEqual(player.inventory.getStackInSlot(i), activeSchematicItem))
				continue;
			return false;
		}
		return true;
	}

	public void markDirty() {
		syncCooldown = SYNC_DELAY;
	}

	public void sync() {
		if (activeSchematicItem == null)
			return;

		PlacementSettings settings = transformation.toSettings();
		CompoundNBT tag = activeSchematicItem.getTag();
		tag.putBoolean("Deployed", deployed);
		tag.put("Anchor", NBTUtil.writeBlockPos(transformation.getAnchor()));
		tag.putString("Rotation", settings.getRotation()
			.name());
		tag.putString("Mirror", settings.getMirror()
			.name());

		AllPackets.channel.sendToServer(new NbtPacket(activeSchematicItem, activeHotbarSlot));
	}

	public void equip(Tools tool) {
		this.currentTool = tool;
		currentTool.getTool()
			.init();
	}

	public void loadSettings(ItemStack blueprint) {
		CompoundNBT tag = blueprint.getTag();
		BlockPos anchor = BlockPos.ZERO;
		PlacementSettings settings = SchematicItem.getSettings(blueprint);
		transformation = new SchematicTransformation();

		deployed = tag.getBoolean("Deployed");
		if (deployed)
			anchor = NBTUtil.readBlockPos(tag.getCompound("Anchor"));
		BlockPos size = NBTUtil.readBlockPos(tag.getCompound("Bounds"));

		bounds = new AxisAlignedBB(BlockPos.ZERO, size);
		outline = new AABBOutline(bounds);
		outline.getParams()
			.lineWidth(1 / 16f)
			.disableNormals();
		transformation.init(anchor, settings, bounds);
	}

	public void deploy() {
		if (!deployed) {
			List<Tools> tools = Tools.getTools(Minecraft.getInstance().player.isCreative());
			selectionScreen = new ToolSelectionScreen(tools, this::equip);
		}
		deployed = true;
		setupRenderer();
	}

	public String getCurrentSchematicName() {
		return displayedSchematic != null ? displayedSchematic : "-";
	}

	public void printInstantly() {
		AllPackets.channel.sendToServer(new SchematicPlacePacket(activeSchematicItem.copy()));
		CompoundNBT nbt = activeSchematicItem.getTag();
		nbt.putBoolean("Deployed", false);
		activeSchematicItem.setTag(nbt);
		renderer.setActive(false);
		active = false;
		markDirty();
	}

	public boolean isActive() {
		return active;
	}

	public AxisAlignedBB getBounds() {
		return bounds;
	}

	public SchematicTransformation getTransformation() {
		return transformation;
	}

	public boolean isDeployed() {
		return deployed;
	}

	public ItemStack getActiveSchematicItem() {
		return activeSchematicItem;
	}

	public AABBOutline getOutline() {
		return outline;
	}

}
