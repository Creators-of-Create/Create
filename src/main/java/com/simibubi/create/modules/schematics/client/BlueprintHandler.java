package com.simibubi.create.modules.schematics.client;

import java.util.HashMap;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.gui.ToolSelectionScreen;
import com.simibubi.create.foundation.packet.NbtPacket;
import com.simibubi.create.foundation.type.Cuboid;
import com.simibubi.create.foundation.utility.KeyboardHelper;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.schematics.SchematicWorld;
import com.simibubi.create.modules.schematics.client.tools.Tools;
import com.simibubi.create.modules.schematics.item.BlueprintItem;
import com.simibubi.create.modules.schematics.packet.SchematicPlacePacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseScrollEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.FORGE)
public class BlueprintHandler {

	public static BlueprintHandler instance;

	public Template cachedSchematic;
	public String cachedSchematicName;
	public PlacementSettings cachedSettings;

	public BlockPos anchor;
	public BlockPos size;
	public boolean active;
	public boolean deployed;
	public int slot;
	public ItemStack item;

	public Tools currentTool;
	public ToolSelectionScreen selectionScreen;

	public static final int SYNC_DELAY = 20;
	public int syncCooldown;

	private BlueprintHotbarOverlay overlay;

	public BlueprintHandler() {
		instance = this;
		currentTool = Tools.Deploy;
		overlay = new BlueprintHotbarOverlay();
		selectionScreen = new ToolSelectionScreen(ImmutableList.of(Tools.Deploy), this::equip);
	}

	@SubscribeEvent
	public static void onPaperCrafted(ItemCraftedEvent event) {
		if (event.isCanceled())
			return;
		if (event.getCrafting().getItem() == Items.PAPER) {
			event.getPlayer()
					.unlockRecipes(new ResourceLocation[] { AllItems.EMPTY_BLUEPRINT.get().getRegistryName() });
		}
		if (event.getCrafting().getItem() == Items.BONE_MEAL) {
			event.getPlayer()
					.unlockRecipes(new ResourceLocation[] { AllItems.TREE_FERTILIZER.get().getRegistryName() });
		}
		if (event.getCrafting().getItem() == Items.END_ROD) {
			event.getPlayer().unlockRecipes(new ResourceLocation[] { AllItems.SYMMETRY_WAND.get().getRegistryName() });
		}
		if (AllItems.EMPTY_BLUEPRINT.typeOf(event.getCrafting())) {
			event.getPlayer()
					.unlockRecipes(new ResourceLocation[] { AllItems.BLUEPRINT_AND_QUILL.get().getRegistryName(),
							AllBlocks.SCHEMATIC_TABLE.get().getRegistryName(),
							AllBlocks.SCHEMATICANNON.get().getRegistryName() });
		}
	}

	@SubscribeEvent
	public static void onItemPickup(ItemPickupEvent event) {
		if (event.isCanceled())
			return;
		if (event.getStack().getItem() == Items.END_ROD) {
			event.getPlayer().unlockRecipes(new ResourceLocation[] { AllItems.SYMMETRY_WAND.get().getRegistryName() });
		}
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		ClientPlayerEntity player = Minecraft.getInstance().player;

		if (player == null)
			return;

		ItemStack stack = findBlueprintInHand(player);
		if (stack == null) {
			instance.active = false;
			instance.syncCooldown = 0;
			if (instance.item != null && itemLost(player)) {
				instance.slot = 0;
				instance.item = null;
				SchematicHologram.reset();
			}
			return;
		}

		// Newly equipped
		if (!instance.active || !stack.getTag().getString("File").equals(instance.cachedSchematicName)) {
			instance.loadSettings(stack);
			instance.cachedSchematicName = stack.getTag().getString("File");
			instance.active = true;
			if (instance.deployed) {
				Tools toolBefore = instance.currentTool;
				instance.selectionScreen = new ToolSelectionScreen(Tools.getTools(player.isCreative()),
						instance::equip);
				if (toolBefore != null) {
					instance.selectionScreen.setSelectedElement(toolBefore);
					instance.equip(toolBefore);
				}
			} else
				instance.selectionScreen = new ToolSelectionScreen(ImmutableList.of(Tools.Deploy), instance::equip);
			instance.sync();
		}

		if (!instance.active)
			return;

		if (instance.syncCooldown > 0)
			instance.syncCooldown--;
		if (instance.syncCooldown == 1)
			instance.sync();

		instance.selectionScreen.update();
		instance.currentTool.getTool().updateSelection();
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		if (!instance.active)
			return;
		if (Minecraft.getInstance().player.isSneaking())
			return;

		TessellatorHelper.prepareForDrawing();
		instance.currentTool.getTool().renderTool();
		TessellatorHelper.cleanUpAfterDrawing();
	}

	@SubscribeEvent
	public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		if (!instance.active)
			return;
		if (event.getType() != ElementType.HOTBAR)
			return;
		if (instance.item != null)
			instance.overlay.renderOn(instance.slot);

		instance.currentTool.getTool().renderOverlay();
		instance.selectionScreen.renderPassive(event.getPartialTicks());
	}

	@SubscribeEvent
	public static void onClick(MouseInputEvent event) {
		if (Minecraft.getInstance().currentScreen != null)
			return;
		if (event.getAction() != KeyboardHelper.PRESS)
			return;
		if (event.getButton() != 1)
			return;
		if (!instance.active)
			return;
		if (Minecraft.getInstance().player.isSneaking())
			return;

		instance.currentTool.getTool().handleRightClick();
	}

	@SubscribeEvent
	public static void onKeyTyped(KeyInputEvent event) {
		if (Minecraft.getInstance().currentScreen != null)
			return;
		if (event.getKey() != Create.TOOL_MENU.getKey().getKeyCode())
			return;
		if (!instance.active)
			return;

		boolean released = event.getAction() == KeyboardHelper.RELEASE;

		ToolSelectionScreen toolSelection = instance.selectionScreen;
		if (released && toolSelection.focused) {
			toolSelection.focused = false;
			toolSelection.onClose();
		}

		if (!released && !toolSelection.focused)
			toolSelection.focused = true;
	}

	@SubscribeEvent
	// TODO: This is a fabricated event call by ScrollFixer until a proper event
	// exists
	public static void onMouseScrolled(MouseScrollEvent.Post event) {
		if (event.getGui() != null)
			return;
		if (instance.onScroll(event.getScrollDelta()))
			event.setCanceled(true);
	}

	public boolean onScroll(double delta) {
		if (!active)
			return false;
		if (Minecraft.getInstance().player.isSneaking())
			return false;
		if (selectionScreen.focused) {
			selectionScreen.cycle((int) delta);
			return true;
		}
		if (KeyboardHelper.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			return currentTool.getTool().handleMouseWheel(delta);
		}

		return false;
	}

	private static ItemStack findBlueprintInHand(PlayerEntity player) {
		ItemStack stack = player.getHeldItemMainhand();
		if (!AllItems.BLUEPRINT.typeOf(stack))
			return null;
		if (!stack.hasTag())
			return null;

		instance.item = stack;
		instance.slot = player.inventory.currentItem;
		return stack;
	}

	private static boolean itemLost(PlayerEntity player) {
		for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
			if (!player.inventory.getStackInSlot(i).isItemEqual(instance.item))
				continue;
			if (!ItemStack.areItemStackTagsEqual(player.inventory.getStackInSlot(i), instance.item))
				continue;
			return false;
		}
		return true;
	}

	public void markDirty() {
		syncCooldown = SYNC_DELAY;
		SchematicHologram.reset();
	}

	public void sync() {
		Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent("Syncing..."), true);
		AllPackets.channel.sendToServer(new NbtPacket(item, slot));

		if (deployed) {
			Template schematic = BlueprintItem.getSchematic(item);

			if (schematic.getSize().equals(BlockPos.ZERO))
				return;

			SchematicWorld w = new SchematicWorld(new HashMap<>(), new Cuboid(), anchor);
			PlacementSettings settings = cachedSettings.copy();
			settings.setBoundingBox(null);
			schematic.addBlocksToWorld(w, anchor, settings);
			new SchematicHologram().startHologram(w);
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
			Minecraft.getInstance().player.sendStatusMessage(
					new StringTextComponent("Mirror: " + cachedSettings.getMirror().toString()), true);

		} else if (m == mirror) {
			cachedSettings.setMirror(Mirror.NONE);
			anchor = anchor.subtract(anchorOffset);
			Minecraft.getInstance().player.sendStatusMessage(
					new StringTextComponent("Mirror: " + cachedSettings.getMirror().toString()), true);

		} else if (m != mirror) {
			cachedSettings.setMirror(Mirror.NONE);
			anchor = anchor.add(anchorOffset);
			cachedSettings.setRotation(r.add(Rotation.CLOCKWISE_180));
			Minecraft.getInstance().player.sendStatusMessage(
					new StringTextComponent("Mirror: None, Rotation: " + cachedSettings.getRotation().toString()),
					true);
		}

		item.getTag().put("Anchor", NBTUtil.writeBlockPos(anchor));
		item.getTag().putString("Mirror", cachedSettings.getMirror().name());
		item.getTag().putString("Rotation", r.name());

		markDirty();
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

		Minecraft.getInstance().player.sendStatusMessage(
				new StringTextComponent("Rotation: " + cachedSettings.getRotation().toString()), true);

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
			instance.selectionScreen = new ToolSelectionScreen(
					Tools.getTools(Minecraft.getInstance().player.isCreative()), instance::equip);

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
		SchematicHologram.reset();
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
