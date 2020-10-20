package com.simibubi.create.content.schematics.client;

import java.util.List;
import java.util.Vector;

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
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

	private Vector<SchematicRenderer> renderers;
	private SchematicHotbarSlotOverlay overlay;
	private ToolSelectionScreen selectionScreen;

	public SchematicHandler() {
		renderers = new Vector<>(3);
		for (int i = 0; i < renderers.capacity(); i++)
			renderers.add(new SchematicRenderer());

		overlay = new SchematicHotbarSlotOverlay();
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
				renderers.forEach(r -> r.setActive(false));
			}
			return;
		}

		if (!active || !stack.getTag()
			.getString("File")
			.equals(displayedSchematic))
			init(player, stack);
		if (!active)
			return;

		renderers.forEach(SchematicRenderer::tick);
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
		BlockPos size = schematic.getSize();
		if (size.equals(BlockPos.ZERO))
			return;

		World clientWorld = Minecraft.getInstance().world;
		SchematicWorld w = new SchematicWorld(clientWorld);
		SchematicWorld wMirroredFB = new SchematicWorld(clientWorld);
		SchematicWorld wMirroredLR = new SchematicWorld(clientWorld);
		PlacementSettings placementSettings = new PlacementSettings();

		schematic.place(w, BlockPos.ZERO, placementSettings, w.getRandom());
		placementSettings.setMirror(Mirror.FRONT_BACK);
		schematic.place(wMirroredFB, BlockPos.ZERO.east(size.getX() - 1), placementSettings, wMirroredFB.getRandom());
		placementSettings.setMirror(Mirror.LEFT_RIGHT);
		schematic.place(wMirroredLR, BlockPos.ZERO.south(size.getZ() - 1), placementSettings, wMirroredFB.getRandom());

		renderers.get(0)
			.display(w);
		renderers.get(1)
			.display(wMirroredFB);
		renderers.get(2)
			.display(wMirroredLR);
	}

	public void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
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

		if (!renderers.isEmpty()) {
			float pt = Minecraft.getInstance()
				.getRenderPartialTicks();
			boolean lr = transformation.getScaleLR()
				.get(pt) < 0;
			boolean fb = transformation.getScaleFB()
				.get(pt) < 0;
			if (lr && !fb)
				renderers.get(2)
					.render(ms, buffer);
			else if (fb && !lr)
				renderers.get(1)
					.render(ms, buffer);
			else
				renderers.get(0)
					.render(ms, buffer);
		}
		
		if (active)
			currentTool.getTool()
			.renderOnSchematic(ms, buffer);
		
		ms.pop();

	}

	public void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay, float partialTicks) {
		if (!active)
			return;
		if (activeSchematicItem != null)
			this.overlay.renderOn(ms, activeHotbarSlot);

		currentTool.getTool()
			.renderOverlay(ms, buffer);
		selectionScreen.renderPassive(ms, partialTicks);
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
		if (!active)
			return false;

		if (selectionScreen.focused) {
			selectionScreen.cycle((int) delta);
			return true;
		}
		if (!AllKeys.ctrlDown())
			return currentTool.getTool()
				.handleMouseWheel(delta);
		return false;
	}

	private ItemStack findBlueprintInHand(PlayerEntity player) {
		ItemStack stack = player.getHeldItemMainhand();
		if (!AllItems.SCHEMATIC.isIn(stack))
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
			.colored(0x6886c5)
			.lineWidth(1 / 16f);
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
		renderers.forEach(r -> r.setActive(false));
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
