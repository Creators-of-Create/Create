package com.simibubi.create.content.schematics.client;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.schematics.SchematicInstances;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.client.tools.ToolType;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import com.simibubi.create.content.schematics.packet.SchematicSyncPacket;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.outliner.AABBOutline;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SchematicHandler implements LayeredDraw.Layer {

	private String displayedSchematic;
	private SchematicTransformation transformation;
	private AABB bounds;
	private boolean deployed;
	private boolean active;
	private ToolType currentTool;

	private static final int SYNC_DELAY = 10;
	private int syncCooldown;
	private int activeHotbarSlot;
	private ItemStack activeSchematicItem;
	private AABBOutline outline;

	private final SchematicRenderer[] renderers = new SchematicRenderer[3];
	private final SchematicHotbarSlotOverlay overlay;
	private ToolSelectionScreen selectionScreen;

	public SchematicHandler() {
		overlay = new SchematicHotbarSlotOverlay();
		currentTool = ToolType.DEPLOY;
		selectionScreen = new ToolSelectionScreen(ImmutableList.of(ToolType.DEPLOY), this::equip);
		transformation = new SchematicTransformation();
	}

	public void tick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			if (active) {
				active = false;
				syncCooldown = 0;
				activeHotbarSlot = 0;
				activeSchematicItem = null;
			}
			return;
		}

		if (activeSchematicItem != null && transformation != null)
			transformation.tick();

		LocalPlayer player = mc.player;
		ItemStack stack = findBlueprintInHand(player);
		if (stack == null) {
			active = false;
			syncCooldown = 0;
			if (activeSchematicItem != null && itemLost(player)) {
				activeHotbarSlot = 0;
				activeSchematicItem = null;
			}
			return;
		}

		if (!active || !stack.get(AllDataComponents.SCHEMATIC_FILE)
			.equals(displayedSchematic)) {
			init(player, stack);
		}
		if (!active)
			return;

		if (syncCooldown > 0)
			syncCooldown--;
		if (syncCooldown == 1)
			sync();

		selectionScreen.update();
		currentTool.getTool()
			.updateSelection();
	}

	private void init(LocalPlayer player, ItemStack stack) {
		loadSettings(stack);
		displayedSchematic = stack.get(AllDataComponents.SCHEMATIC_FILE);
		active = true;
		if (deployed) {
			setupRenderer();
			ToolType toolBefore = currentTool;
			selectionScreen = new ToolSelectionScreen(ToolType.getTools(player.isCreative()), this::equip);
			if (toolBefore != null) {
				selectionScreen.setSelectedElement(toolBefore);
				equip(toolBefore);
			}
		} else
			selectionScreen = new ToolSelectionScreen(ImmutableList.of(ToolType.DEPLOY), this::equip);
	}

	private void setupRenderer() {
		Level clientWorld = Minecraft.getInstance().level;
		StructureTemplate schematic =
			SchematicItem.loadSchematic(clientWorld, activeSchematicItem);
		Vec3i size = schematic.getSize();
		if (size.equals(Vec3i.ZERO))
			return;

		SchematicLevel w = new SchematicLevel(clientWorld);
		SchematicLevel wMirroredFB = new SchematicLevel(clientWorld);
		SchematicLevel wMirroredLR = new SchematicLevel(clientWorld);
		StructurePlaceSettings placementSettings = new StructurePlaceSettings();
		StructureTransform transform;
		BlockPos pos;

		pos = BlockPos.ZERO;

		try {
			schematic.placeInWorld(w, pos, pos, placementSettings, w.getRandom(), Block.UPDATE_CLIENTS);
			for (BlockEntity blockEntity : w.getBlockEntities())
				blockEntity.setLevel(w);
			fixControllerBlockEntities(w);
		} catch (Exception e) {
			Minecraft.getInstance().player.displayClientMessage(CreateLang.translate("schematic.error")
				.component(), false);
			Create.LOGGER.error("Failed to load Schematic for Previewing", e);
			return;
		}

		placementSettings.setMirror(Mirror.FRONT_BACK);
		pos = BlockPos.ZERO.east(size.getX() - 1);
		schematic.placeInWorld(wMirroredFB, pos, pos, placementSettings, wMirroredFB.getRandom(), Block.UPDATE_CLIENTS);
		transform = new StructureTransform(placementSettings.getRotationPivot(), Axis.Y, Rotation.NONE,
			placementSettings.getMirror());
		for (BlockEntity be : wMirroredFB.getRenderedBlockEntities())
			transform.apply(be);
		fixControllerBlockEntities(wMirroredFB);

		placementSettings.setMirror(Mirror.LEFT_RIGHT);
		pos = BlockPos.ZERO.south(size.getZ() - 1);
		schematic.placeInWorld(wMirroredLR, pos, pos, placementSettings, wMirroredFB.getRandom(), Block.UPDATE_CLIENTS);
		transform = new StructureTransform(placementSettings.getRotationPivot(), Axis.Y, Rotation.NONE,
			placementSettings.getMirror());
		for (BlockEntity be : wMirroredLR.getRenderedBlockEntities())
			transform.apply(be);
		fixControllerBlockEntities(wMirroredLR);

		renderers[0] = new SchematicRenderer(w);
		renderers[1] = new SchematicRenderer(wMirroredFB);
		renderers[2] = new SchematicRenderer(wMirroredLR);
	}

	private void fixControllerBlockEntities(SchematicLevel level) {
		for (BlockEntity blockEntity : level.getBlockEntities()) {
			if (!(blockEntity instanceof IMultiBlockEntityContainer multiBlockEntity))
				continue;
			BlockPos lastKnown = multiBlockEntity.getLastKnownPos();
			BlockPos current = blockEntity.getBlockPos();
			if (lastKnown == null || current == null)
				continue;
			if (multiBlockEntity.isController())
				continue;
			if (!lastKnown.equals(current)) {
				BlockPos newControllerPos = multiBlockEntity.getController()
					.offset(current.subtract(lastKnown));
				if (multiBlockEntity instanceof SmartBlockEntity sbe)
					sbe.markVirtual();
				multiBlockEntity.setController(newControllerPos);
			}
		}
	}

	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
		if (!active) {
			return;
		}
		boolean present = activeSchematicItem != null;
		if (!present) {
			return;
		}

		ms.pushPose();
		currentTool.getTool()
			.renderTool(ms, buffer, camera);
		ms.popPose();

		ms.pushPose();
		transformation.applyTransformations(ms, camera);

		if (deployed) {
			float pt = AnimationTickHolder.getPartialTicks();
			boolean lr = transformation.getScaleLR()
				.getValue(pt) < 0;
			boolean fb = transformation.getScaleFB()
				.getValue(pt) < 0;
			if (lr && !fb && renderers[2] != null) {
				renderers[2].render(ms, buffer);
			} else if (fb && !lr && renderers[1] != null) {
				renderers[1].render(ms, buffer);
			} else if (renderers[0] != null) {
				renderers[0].render(ms, buffer);
			}
		}

		currentTool.getTool()
			.renderOnSchematic(ms, buffer);

		ms.popPose();
	}

	public void updateRenderers() {
		for (SchematicRenderer renderer : renderers) {
			if (renderer != null) {
				renderer.update();
			}
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || !active)
			return;
		if (activeSchematicItem != null)
			this.overlay.renderOn(guiGraphics, activeHotbarSlot);
		currentTool.getTool()
			.renderOverlay(mc.gui, guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(false), guiGraphics.guiWidth(), guiGraphics.guiHeight());
		selectionScreen.renderPassive(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(false));
	}

	public boolean onMouseInput(int button, boolean pressed) {
		if (!active)
			return false;
		if (!pressed || button != 1)
			return false;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player.isShiftKeyDown())
			return false;
		if (mc.hitResult instanceof BlockHitResult blockRayTraceResult) {
			BlockState clickedBlock = mc.level.getBlockState(blockRayTraceResult.getBlockPos());
			if (AllBlocks.SCHEMATICANNON.has(clickedBlock))
				return false;
			if (AllBlocks.DEPLOYER.has(clickedBlock))
				return false;
		}
		return currentTool.getTool()
			.handleRightClick();
	}

	public void onKeyInput(int key, boolean pressed) {
		if (!active)
			return;
		if (!AllKeys.TOOL_MENU.doesModifierAndCodeMatch(key))
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
			selectionScreen.cycle((int) Math.signum(delta));
			return true;
		}
		if (AllKeys.ctrlDown())
			return currentTool.getTool()
				.handleMouseWheel(delta);
		return false;
	}

	private ItemStack findBlueprintInHand(Player player) {
		ItemStack stack = player.getMainHandItem();
		if (!AllItems.SCHEMATIC.isIn(stack))
			return null;
		if (!stack.has(AllDataComponents.SCHEMATIC_FILE))
			return null;

		activeSchematicItem = stack;
		activeHotbarSlot = player.getInventory().selected;
		return stack;
	}

	private boolean itemLost(Player player) {
		for (int i = 0; i < Inventory.getSelectionSize(); i++) {
			if (player.getInventory()
				.getItem(i)
				.is(activeSchematicItem.getItem()))
				continue;
			if (!ItemStack.matches(player.getInventory()
				.getItem(i), activeSchematicItem))
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
		CatnipServices.NETWORK.sendToServer(new SchematicSyncPacket(activeHotbarSlot, transformation.toSettings(),
			transformation.getAnchor(), deployed));
	}

	public void equip(ToolType tool) {
		this.currentTool = tool;
		currentTool.getTool()
			.init();
	}

	public void loadSettings(ItemStack blueprint) {
		StructurePlaceSettings settings = SchematicItem.getSettings(blueprint);
		transformation = new SchematicTransformation();

		deployed = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false);
		BlockPos anchor = blueprint.getOrDefault(AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ZERO);
		Vec3i size = blueprint.get(AllDataComponents.SCHEMATIC_BOUNDS);
		if (size == null)
			return;

		bounds = new AABB(0, 0, 0, size.getX(), size.getY(), size.getZ());
		outline = new AABBOutline(bounds);
		outline.getParams()
			.colored(0x6886c5)
			.lineWidth(1 / 16f);
		transformation.init(anchor, settings, bounds);
	}

	public void deploy() {
		if (!deployed) {
			List<ToolType> tools = ToolType.getTools(Minecraft.getInstance().player.isCreative());
			selectionScreen = new ToolSelectionScreen(tools, this::equip);
		}
		deployed = true;
		setupRenderer();
	}

	public String getCurrentSchematicName() {
		return displayedSchematic != null ? displayedSchematic : "-";
	}

	public void printInstantly() {
		CatnipServices.NETWORK.sendToServer(new SchematicPlacePacket(activeSchematicItem.copy()));
		activeSchematicItem.set(AllDataComponents.SCHEMATIC_DEPLOYED, false);
		SchematicInstances.clearHash(activeSchematicItem);
		active = false;
		markDirty();
	}

	public boolean isActive() {
		return active;
	}

	public AABB getBounds() {
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
