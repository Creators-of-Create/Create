package com.simibubi.create.modules.schematics.client;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextInputPromptScreen;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.KeyboardHelper;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent.MouseScrollEvent;
import net.minecraftforge.client.event.InputEvent.MouseInputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.FORGE)
public class BlueprintAndQuillHandler {

	static BlockPos firstPos;
	static BlockPos secondPos;
	static BlockPos selectedPos;
	static Direction selectedFace;
	static int range = 10;

	private static boolean active() {
		return present() && AllItems.BLUEPRINT_AND_QUILL.typeOf(Minecraft.getInstance().player.getHeldItemMainhand());
	}

	private static boolean present() {
		return Minecraft.getInstance() != null && Minecraft.getInstance().world != null
				&& Minecraft.getInstance().currentScreen == null;
	}

	@SubscribeEvent
	// TODO: This is a fabricated event call by ScrollFixer until a proper event
	// exists
	public static void onMouseScrolled(MouseScrollEvent.Post event) {
		if (event.getGui() != null)
			return;
		if (!active())
			return;
		if (!KeyboardHelper.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL))
			return;
		int delta = (int) event.getScrollDelta();
		if (secondPos == null)
			range = (int) MathHelper.clamp(range + delta, 1, 100);
		if (selectedFace != null) {
			MutableBoundingBox bb = new MutableBoundingBox(firstPos, secondPos);
			Vec3i vec = selectedFace.getDirectionVec();

			int x = vec.getX() * delta;
			int y = vec.getY() * delta;
			int z = vec.getZ() * delta;

			AxisDirection axisDirection = selectedFace.getAxisDirection();
			if (axisDirection == AxisDirection.NEGATIVE)
				bb.offset(-x, -y, -z);

			bb.maxX = Math.max(bb.maxX - x * axisDirection.getOffset(), bb.minX);
			bb.maxY = Math.max(bb.maxY - y * axisDirection.getOffset(), bb.minY);
			bb.maxZ = Math.max(bb.maxZ - z * axisDirection.getOffset(), bb.minZ);

			firstPos = new BlockPos(bb.minX, bb.minY, bb.minZ);
			secondPos = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
			Minecraft.getInstance().player.sendStatusMessage(
					new StringTextComponent(
							"Schematic size: " + (bb.getXSize()) + "x" + (bb.getYSize()) + "x" + (bb.getZSize())),
					true);
		}

		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClick(MouseInputEvent event) {
		if (event.getAction() != KeyboardHelper.PRESS)
			return;
		if (event.getButton() != 1)
			return;
		if (!active())
			return;
		ClientPlayerEntity player = Minecraft.getInstance().player;

		if (player.isSneaking()) {
			firstPos = null;
			secondPos = null;
			player.sendStatusMessage(new StringTextComponent("Removed selection."), true);
			return;
		}

		if (secondPos != null) {
			TextInputPromptScreen guiScreenIn = new TextInputPromptScreen(BlueprintAndQuillHandler::saveSchematic, s -> {
			});
			guiScreenIn.setTitle("Enter a name for the Schematic:");
			guiScreenIn.setButtonTextConfirm("Save");
			guiScreenIn.setButtonTextAbort("Cancel");
			ScreenOpener.open(guiScreenIn);
			return;
		}

		if (selectedPos == null) {
			player.sendStatusMessage(new StringTextComponent("Hold [CTRL] to select Air blocks."), true);
			return;
		}

		if (firstPos != null) {
			secondPos = selectedPos;
			player.sendStatusMessage(new StringTextComponent(TextFormatting.GREEN + "Second position set."), true);
			return;
		}

		firstPos = selectedPos;
		player.sendStatusMessage(new StringTextComponent(TextFormatting.GREEN + "First position set."), true);
	}

	public static void saveSchematic(String string) {
		Template t = new Template();
		MutableBoundingBox bb = new MutableBoundingBox(firstPos, secondPos);
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
		secondPos = null;
		Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent("Saved as " + filepath), true);
	}

	@SubscribeEvent
	public static void onRenderWorld(RenderWorldLastEvent event) {
		if (!active())
			return;

		TessellatorHelper.prepareForDrawing();
		GlStateManager.lineWidth(2);
		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.disableTexture();

		if (secondPos == null) {
			// 1st Step
			if (firstPos != null && selectedPos == null) {
				MutableBoundingBox bb = new MutableBoundingBox(firstPos, firstPos.add(1, 1, 1));
				BlockPos min = new BlockPos(bb.minX, bb.minY, bb.minZ);
				BlockPos max = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
				drawBox(min, max, true);
			}

			if (firstPos != null && selectedPos != null) {
				MutableBoundingBox bb = new MutableBoundingBox(firstPos, selectedPos);
				BlockPos min = new BlockPos(bb.minX, bb.minY, bb.minZ);
				BlockPos max = new BlockPos(bb.maxX + 1, bb.maxY + 1, bb.maxZ + 1);
				drawBox(min, max, true);
			}

			if (firstPos == null && selectedPos != null) {
				MutableBoundingBox bb = new MutableBoundingBox(selectedPos, selectedPos.add(1, 1, 1));
				BlockPos min = new BlockPos(bb.minX, bb.minY, bb.minZ);
				BlockPos max = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
				drawBox(min, max, true);
			}
		} else {
			// 2nd Step
			MutableBoundingBox bb = new MutableBoundingBox(firstPos, secondPos);
			BlockPos min = new BlockPos(bb.minX, bb.minY, bb.minZ);
			BlockPos max = new BlockPos(bb.maxX + 1, bb.maxY + 1, bb.maxZ + 1);
			drawBox(min, max, false);

			if (selectedFace != null) {
				Vec3d vec = new Vec3d(selectedFace.getDirectionVec());
				Vec3d center = new Vec3d(min.add(max)).scale(1 / 2f);
				Vec3d radii = new Vec3d(max.subtract(min)).scale(1 / 2f);

				Vec3d onFaceOffset = new Vec3d(1 - Math.abs(vec.x), 1 - Math.abs(vec.y), 1 - Math.abs(vec.z))
						.mul(radii);
				Vec3d faceMin = center.add(vec.mul(radii).add(onFaceOffset));
				Vec3d faceMax = center.add(vec.mul(radii).subtract(onFaceOffset));

				GlStateManager.enableTexture();
				TessellatorHelper.begin();
				AllSpecialTextures.SELECTION.bind();
				TessellatorHelper.doubleFace(Tessellator.getInstance().getBuffer(), new BlockPos(faceMin),
						new BlockPos(faceMax.subtract(faceMin)), 1 / 16f * selectedFace.getAxisDirection().getOffset(),
						false, false, false);
				TessellatorHelper.draw();
				GlStateManager.disableTexture();

			}

		}

		GlStateManager.lineWidth(1);
		GlStateManager.enableTexture();
		TessellatorHelper.cleanUpAfterDrawing();
	}

	protected static void drawBox(BlockPos min, BlockPos max, boolean blue) {
		float red = blue ? .8f : 1;
		float green = blue ? .9f : 1;
		WorldRenderer.drawBoundingBox(min.getX() - 1 / 16d, min.getY() + 1 / 16d, min.getZ() - 1 / 16d,
				max.getX() + 1 / 16d, max.getY() + 1 / 16d, max.getZ() + 1 / 16d, red, green, 1, 1);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;
		if (!active())
			return;
		ClientPlayerEntity player = Minecraft.getInstance().player;

		selectedPos = null;
		if (KeyboardHelper.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
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

		if (secondPos == null) {
			selectedFace = null;
			return;
		}

		MutableBoundingBox bb = new MutableBoundingBox(firstPos, secondPos);
		bb.maxX++;
		bb.maxY++;
		bb.maxZ++;

		PredicateTraceResult result = RaycastHelper.rayTraceUntil(player, 70, pos -> bb.isVecInside(pos));
		selectedFace = result.missed() ? null : result.getFacing();
	}
}