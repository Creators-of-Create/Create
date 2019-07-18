package com.simibubi.create.schematic;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.gui.GuiOpener;
import com.simibubi.create.gui.GuiTextPrompt;
import com.simibubi.create.gui.Keyboard;
import com.simibubi.create.utility.FilesHelper;
import com.simibubi.create.utility.RaycastHelper;
import com.simibubi.create.utility.TessellatorHelper;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent.MouseScrollEvent;
import net.minecraftforge.client.event.InputEvent.MouseInputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.FORGE)
public class BlueprintAndQuillHandler {

	static BlockPos firstPos;
	static BlockPos selectedPos;
	static int range = 10;

	private static boolean active() {
		return present() && AllItems.BLUEPRINT_AND_QUILL.typeOf(Minecraft.getInstance().player.getHeldItemMainhand());
	}

	private static boolean present() {
		return Minecraft.getInstance() != null && Minecraft.getInstance().world != null
				&& Minecraft.getInstance().currentScreen == null && !Minecraft.getInstance().player.isSneaking();
	}

	@SubscribeEvent
	// TODO: This is a fabricated event call by ScrollFixer until a proper event exists
	public static void onMouseScrolled(MouseScrollEvent.Post event) {
		if (event.getGui() != null)
			return;
		if (!active())
			return;
		if (!Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
			return;
		range = (int) MathHelper.clamp(range + event.getScrollDelta(), 1, 100);
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClick(MouseInputEvent event) {
		if (event.getAction() != Keyboard.PRESS)
			return;
		if (event.getButton() != 1)
			return;
		if (!active() && !Minecraft.getInstance().player.isSneaking())
			return;
		if (selectedPos == null)
			return;
		if (Minecraft.getInstance().player.isSneaking()) {
			firstPos = null;
			return;
		}

		if (firstPos != null) {
			GuiTextPrompt guiScreenIn = new GuiTextPrompt(BlueprintAndQuillHandler::saveSchematic, s -> {
				firstPos = null;
			});
			guiScreenIn.setTitle("Enter a name for the Schematic:");
			guiScreenIn.setButtonTextConfirm("Save");
			guiScreenIn.setButtonTextAbort("Cancel");
			GuiOpener.open(guiScreenIn);
			return;
		}

		firstPos = selectedPos;
	}

	public static void saveSchematic(String string) {
		Template t = new Template();
		MutableBoundingBox bb = new MutableBoundingBox(firstPos, selectedPos);
		t.takeBlocksFromWorld(Minecraft.getInstance().world, new BlockPos(bb.minX, bb.minY, bb.minZ),
				new BlockPos(bb.getXSize(), bb.getYSize(), bb.getZSize()), false, Blocks.AIR);

		if (string.isEmpty())
			string = "My Schematic";

		String folderPath = "schematics";
		FilesHelper.createFolderIfMissing(folderPath);
		String filename = FilesHelper.findFirstValidFilename(string, folderPath, "nbt");
		String filepath = folderPath + "/" + filename;

		OutputStream outputStream = null;
		try {
			outputStream = Files.newOutputStream(Paths.get(filepath), StandardOpenOption.CREATE);
			CompoundNBT nbttagcompound = t.writeToNBT(new CompoundNBT());
			CompressedStreamTools.writeCompressed(nbttagcompound, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null)
				IOUtils.closeQuietly(outputStream);
		}
		firstPos = null;
		Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent("Saved as " + filepath), true);
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		if (!active())
			return;

		TessellatorHelper.prepareForDrawing();
		GlStateManager.lineWidth(3);
		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.disableTexture();

		if (firstPos != null) {
			MutableBoundingBox bb = new MutableBoundingBox(firstPos, firstPos.add(1, 1, 1));
			BlockPos min = new BlockPos(bb.minX, bb.minY, bb.minZ);
			BlockPos max = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
			drawBox(min, max);
		}

		if (selectedPos != null) {
			MutableBoundingBox bb = new MutableBoundingBox(selectedPos, selectedPos.add(1, 1, 1));
			BlockPos min = new BlockPos(bb.minX, bb.minY, bb.minZ);
			BlockPos max = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
			drawBox(min, max);

			if (firstPos != null) {
				bb = new MutableBoundingBox(firstPos, selectedPos);
				min = new BlockPos(bb.minX, bb.minY, bb.minZ);
				max = new BlockPos(bb.maxX + 1, bb.maxY + 1, bb.maxZ + 1);
				drawBox(min, max);
			}
		}

		GlStateManager.lineWidth(1);
		GlStateManager.enableTexture();
		TessellatorHelper.cleanUpAfterDrawing();
	}

	protected static void drawBox(BlockPos min, BlockPos max) {
		WorldRenderer.drawBoundingBox(min.getX() - 1 / 16d, min.getY() - 1 / 16d, min.getZ() - 1 / 16d,
				max.getX() + 1 / 16d, max.getY() + 1 / 16d, max.getZ() + 1 / 16d, .3f, .4f, 1, 1);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (!active())
			return;
		ClientPlayerEntity player = Minecraft.getInstance().player;

		selectedPos = null;
		if (Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			selectedPos = new BlockPos(player.getEyePosition(Minecraft.getInstance().getRenderPartialTicks())
					.add(player.getLookVec().scale(range)));
		} else {
			BlockRayTraceResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
			if (trace != null && trace.getType() == Type.BLOCK) {

				BlockPos hit = new BlockPos(trace.getHitVec());
				boolean replaceable = player.world.getBlockState(hit)
						.isReplaceable(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, trace)));
				if (trace.getFace().getAxis().isVertical() && !replaceable)
					hit = hit.offset(trace.getFace());

				selectedPos = hit;
			} else {
				selectedPos = null;
			}
		}

	}
}