package com.simibubi.create.content.schematics.client;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.gui.TextInputPromptScreen;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.gen.feature.template.Template;

public class SchematicAndQuillHandler {

	private Object outlineSlot = new Object();

	private BlockPos firstPos;
	private BlockPos secondPos;
	private BlockPos selectedPos;
	private Direction selectedFace;
	private int range = 10;

	public boolean mouseScrolled(double delta) {
		if (!isActive())
			return false;
		if (!AllKeys.ctrlDown())
			return false;
		if (secondPos == null)
			range = (int) MathHelper.clamp(range + delta, 1, 100);
		if (selectedFace == null)
			return true;

		AxisAlignedBB bb = new AxisAlignedBB(firstPos, secondPos);
		Vector3i vec = selectedFace.getDirectionVec();
		Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo()
			.getProjectedView();
		if (bb.contains(projectedView))
			delta *= -1;

		int x = (int) (vec.getX() * delta);
		int y = (int) (vec.getY() * delta);
		int z = (int) (vec.getZ() * delta);

		AxisDirection axisDirection = selectedFace.getAxisDirection();
		if (axisDirection == AxisDirection.NEGATIVE)
			bb = bb.offset(-x, -y, -z);

		double maxX = Math.max(bb.maxX - x * axisDirection.getOffset(), bb.minX);
		double maxY = Math.max(bb.maxY - y * axisDirection.getOffset(), bb.minY);
		double maxZ = Math.max(bb.maxZ - z * axisDirection.getOffset(), bb.minZ);
		bb = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, maxX, maxY, maxZ);

		firstPos = new BlockPos(bb.minX, bb.minY, bb.minZ);
		secondPos = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
		Lang.sendStatus(Minecraft.getInstance().player, "schematicAndQuill.dimensions", (int) bb.getXSize() + 1,
			(int) bb.getYSize() + 1, (int) bb.getZSize() + 1);

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

	public void tick() {
		if (!isActive())
			return;

		ClientPlayerEntity player = Minecraft.getInstance().player;
		if (AllKeys.ACTIVATE_TOOL.isPressed()) {
			float pt = Minecraft.getInstance()
				.getRenderPartialTicks();
			Vector3d targetVec = player.getEyePosition(pt)
				.add(player.getLookVec()
					.scale(range));
			selectedPos = new BlockPos(targetVec);

		} else {
			BlockRayTraceResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
			if (trace != null && trace.getType() == Type.BLOCK) {

				BlockPos hit = trace.getPos();
				boolean replaceable = player.world.getBlockState(hit)
					.isReplaceable(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, trace)));
				if (trace.getFace()
					.getAxis()
					.isVertical() && !replaceable)
					hit = hit.offset(trace.getFace());
				selectedPos = hit;
			} else
				selectedPos = null;
		}

		selectedFace = null;
		if (secondPos != null) {
			AxisAlignedBB bb = new AxisAlignedBB(firstPos, secondPos).expand(1, 1, 1)
				.grow(.45f);
			Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo()
				.getProjectedView();
			boolean inside = bb.contains(projectedView);
			PredicateTraceResult result =
				RaycastHelper.rayTraceUntil(player, 70, pos -> inside ^ bb.contains(VecHelper.getCenterOf(pos)));
			selectedFace = result.missed() ? null
				: inside ? result.getFacing()
					.getOpposite() : result.getFacing();
		}

		AxisAlignedBB currentSelectionBox = getCurrentSelectionBox();
		if (currentSelectionBox != null)
			outliner().chaseAABB(outlineSlot, currentSelectionBox)
				.colored(0x6886c5)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.highlightFace(selectedFace);
	}

	private AxisAlignedBB getCurrentSelectionBox() {
		if (secondPos == null) {
			if (firstPos == null)
				return selectedPos == null ? null : new AxisAlignedBB(selectedPos);
			return selectedPos == null ? new AxisAlignedBB(firstPos)
				: new AxisAlignedBB(firstPos, selectedPos).expand(1, 1, 1);
		}
		return new AxisAlignedBB(firstPos, secondPos).expand(1, 1, 1);
	}

	private boolean isActive() {
		return isPresent() && AllItems.SCHEMATIC_AND_QUILL.isIn(Minecraft.getInstance().player.getHeldItemMainhand());
	}

	private boolean isPresent() {
		return Minecraft.getInstance() != null && Minecraft.getInstance().world != null
			&& Minecraft.getInstance().currentScreen == null;
	}

	public void saveSchematic(String string) {
		Template t = new Template();
		MutableBoundingBox bb = new MutableBoundingBox(firstPos, secondPos);
		t.takeBlocksFromWorld(Minecraft.getInstance().world, new BlockPos(bb.minX, bb.minY, bb.minZ),
			new BlockPos(bb.getXSize(), bb.getYSize(), bb.getZSize()), true, Blocks.AIR);

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

	private Outliner outliner() {
		return CreateClient.outliner;
	}

}