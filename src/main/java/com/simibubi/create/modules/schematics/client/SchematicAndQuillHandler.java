package com.simibubi.create.modules.schematics.client;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextInputPromptScreen;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.Lang;
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
import net.minecraft.world.gen.feature.template.Template;

public class SchematicAndQuillHandler {

	private BlockPos firstPos;
	private BlockPos secondPos;
	private BlockPos selectedPos;
	private Direction selectedFace;
	private int range = 10;

	private boolean isActive() {
		return isPresent() && AllItems.BLUEPRINT_AND_QUILL.typeOf(Minecraft.getInstance().player.getHeldItemMainhand());
	}

	private boolean isPresent() {
		return Minecraft.getInstance() != null && Minecraft.getInstance().world != null
				&& Minecraft.getInstance().currentScreen == null;
	}

	public boolean mouseScrolled(double delta) {
		if (!isActive())
			return false;
		if (!AllKeys.ctrlDown())
			return false;
		if (secondPos == null)
			range = (int) MathHelper.clamp(range + delta, 1, 100);
		if (selectedFace != null) {
			MutableBoundingBox bb = new MutableBoundingBox(firstPos, secondPos);
			Vec3i vec = selectedFace.getDirectionVec();

			int x = (int) (vec.getX() * delta);
			int y = (int) (vec.getY() * delta);
			int z = (int) (vec.getZ() * delta);

			AxisDirection axisDirection = selectedFace.getAxisDirection();
			if (axisDirection == AxisDirection.NEGATIVE)
				bb.offset(-x, -y, -z);

			bb.maxX = Math.max(bb.maxX - x * axisDirection.getOffset(), bb.minX);
			bb.maxY = Math.max(bb.maxY - y * axisDirection.getOffset(), bb.minY);
			bb.maxZ = Math.max(bb.maxZ - z * axisDirection.getOffset(), bb.minZ);

			firstPos = new BlockPos(bb.minX, bb.minY, bb.minZ);
			secondPos = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
			Lang.sendStatus(Minecraft.getInstance().player, "schematicAndQuill.dimensions", bb.getXSize(),
					bb.getYSize(), bb.getZSize());
		}

		return true;
	}

	public void onMouseInput(int button, boolean pressed) {
		if (!pressed || button != 1)
			return;
		if (!isActive())
			return;

		ClientPlayerEntity player = Minecraft.getInstance().player;

		if (player.isSneaking()) {
			firstPos = null;
			secondPos = null;
			Lang.sendStatus(player, "schematicAndQuill.abort");
			return;
		}

		if (secondPos != null) {
			TextInputPromptScreen guiScreenIn = new TextInputPromptScreen(this::saveSchematic, s -> {
			});
			guiScreenIn.setTitle(Lang.translate("schematicAndQuill.prompt"));
			guiScreenIn.setButtonTextConfirm(Lang.translate("action.saveToFile"));
			guiScreenIn.setButtonTextAbort(Lang.translate("action.discard"));
			ScreenOpener.open(guiScreenIn);
			return;
		}

		if (selectedPos == null) {
			Lang.sendStatus(player, "schematicAndQuill.noTarget");
			return;
		}

		if (firstPos != null) {
			secondPos = selectedPos;
			Lang.sendStatus(player, "schematicAndQuill.secondPos");
			return;
		}

		firstPos = selectedPos;
		Lang.sendStatus(player, "schematicAndQuill.firstPos");
	}

	public void saveSchematic(String string) {
		Template t = new Template();
		MutableBoundingBox bb = new MutableBoundingBox(firstPos, secondPos);
		t.takeBlocksFromWorld(Minecraft.getInstance().world, new BlockPos(bb.minX, bb.minY, bb.minZ),
				new BlockPos(bb.getXSize(), bb.getYSize(), bb.getZSize()), false, Blocks.AIR);

		if (string.isEmpty())
			string = Lang.translate("schematicAndQuill.fallbackName");

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
		Lang.sendStatus(Minecraft.getInstance().player, "schematicAndQuill.saved", filepath);
	}

	public void render() {
		if (!isActive())
			return;

		TessellatorHelper.prepareForDrawing();
		RenderSystem.lineWidth(2);
		RenderSystem.color4f(1, 1, 1, 1);
		RenderSystem.disableTexture();

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

				RenderSystem.enableTexture();
				TessellatorHelper.begin();
				AllSpecialTextures.SELECTION.bind();
				TessellatorHelper.doubleFace(Tessellator.getInstance().getBuffer(), new BlockPos(faceMin),
						new BlockPos(faceMax.subtract(faceMin)), 1 / 16f * selectedFace.getAxisDirection().getOffset(),
						false, false, false);
				TessellatorHelper.draw();
				RenderSystem.disableTexture();

			}

		}

		RenderSystem.lineWidth(1);
		RenderSystem.enableTexture();
		TessellatorHelper.cleanUpAfterDrawing();
	}

	protected static void drawBox(BlockPos min, BlockPos max, boolean blue) {
		float red = blue ? .8f : 1;
		float green = blue ? .9f : 1;
		WorldRenderer.drawBoundingBox(min.getX() - 1 / 16d, min.getY() + 1 / 16d, min.getZ() - 1 / 16d,
				max.getX() + 1 / 16d, max.getY() + 1 / 16d, max.getZ() + 1 / 16d, red, green, 1, 1);
	}

	public void tick() {
		if (!isActive())
			return;
		ClientPlayerEntity player = Minecraft.getInstance().player;

		selectedPos = null;
		if (AllKeys.ACTIVATE_TOOL.isPressed()) {
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