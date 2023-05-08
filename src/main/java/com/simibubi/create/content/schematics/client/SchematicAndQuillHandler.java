package com.simibubi.create.content.schematics.client;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.ClientSchematicLoader;
import com.simibubi.create.content.schematics.item.SchematicAndQuillItem;
import com.simibubi.create.content.schematics.packet.InstantSchematicPacket;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

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
			range = (int) Mth.clamp(range + delta, 1, 100);
		if (selectedFace == null)
			return true;

		AABB bb = new AABB(firstPos, secondPos);
		Vec3i vec = selectedFace.getNormal();
		Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera()
			.getPosition();
		if (bb.contains(projectedView))
			delta *= -1;

		int x = (int) (vec.getX() * delta);
		int y = (int) (vec.getY() * delta);
		int z = (int) (vec.getZ() * delta);

		AxisDirection axisDirection = selectedFace.getAxisDirection();
		if (axisDirection == AxisDirection.NEGATIVE)
			bb = bb.move(-x, -y, -z);

		double maxX = Math.max(bb.maxX - x * axisDirection.getStep(), bb.minX);
		double maxY = Math.max(bb.maxY - y * axisDirection.getStep(), bb.minY);
		double maxZ = Math.max(bb.maxZ - z * axisDirection.getStep(), bb.minZ);
		bb = new AABB(bb.minX, bb.minY, bb.minZ, maxX, maxY, maxZ);

		firstPos = new BlockPos(bb.minX, bb.minY, bb.minZ);
		secondPos = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);
		LocalPlayer player = Minecraft.getInstance().player;
		Lang.translate("schematicAndQuill.dimensions", (int) bb.getXsize() + 1, (int) bb.getYsize() + 1,
			(int) bb.getZsize() + 1)
			.sendStatus(player);

		return true;
	}

	public boolean onMouseInput(int button, boolean pressed) {
		if (!pressed || button != 1)
			return false;
		if (!isActive())
			return false;

		LocalPlayer player = Minecraft.getInstance().player;

		if (player.isShiftKeyDown()) {
			discard();
			return true;
		}

		if (secondPos != null) {
			ScreenOpener.open(new SchematicPromptScreen());
			return true;
		}

		if (selectedPos == null) {
			Lang.translate("schematicAndQuill.noTarget")
				.sendStatus(player);
			return true;
		}

		if (firstPos != null) {
			secondPos = selectedPos;
			Lang.translate("schematicAndQuill.secondPos")
				.sendStatus(player);
			return true;
		}

		firstPos = selectedPos;
		Lang.translate("schematicAndQuill.firstPos")
			.sendStatus(player);
		return true;
	}

	public void discard() {
		LocalPlayer player = Minecraft.getInstance().player;
		firstPos = null;
		secondPos = null;
		Lang.translate("schematicAndQuill.abort")
			.sendStatus(player);
	}

	public void tick() {
		if (!isActive())
			return;

		LocalPlayer player = Minecraft.getInstance().player;
		if (AllKeys.ACTIVATE_TOOL.isPressed()) {
			float pt = AnimationTickHolder.getPartialTicks();
			Vec3 targetVec = player.getEyePosition(pt)
				.add(player.getLookAngle()
					.scale(range));
			selectedPos = new BlockPos(targetVec);

		} else {
			BlockHitResult trace = RaycastHelper.rayTraceRange(player.level, player, 75);
			if (trace != null && trace.getType() == Type.BLOCK) {

				BlockPos hit = trace.getBlockPos();
				boolean replaceable = player.level.getBlockState(hit)
					.canBeReplaced(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, trace)));
				if (trace.getDirection()
					.getAxis()
					.isVertical() && !replaceable)
					hit = hit.relative(trace.getDirection());
				selectedPos = hit;
			} else
				selectedPos = null;
		}

		selectedFace = null;
		if (secondPos != null) {
			AABB bb = new AABB(firstPos, secondPos).expandTowards(1, 1, 1)
				.inflate(.45f);
			Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera()
				.getPosition();
			boolean inside = bb.contains(projectedView);
			PredicateTraceResult result =
				RaycastHelper.rayTraceUntil(player, 70, pos -> inside ^ bb.contains(VecHelper.getCenterOf(pos)));
			selectedFace = result.missed() ? null
				: inside ? result.getFacing()
					.getOpposite() : result.getFacing();
		}

		AABB currentSelectionBox = getCurrentSelectionBox();
		if (currentSelectionBox != null)
			outliner().chaseAABB(outlineSlot, currentSelectionBox)
				.colored(0x6886c5)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.highlightFace(selectedFace);
	}

	private AABB getCurrentSelectionBox() {
		if (secondPos == null) {
			if (firstPos == null)
				return selectedPos == null ? null : new AABB(selectedPos);
			return selectedPos == null ? new AABB(firstPos) : new AABB(firstPos, selectedPos).expandTowards(1, 1, 1);
		}
		return new AABB(firstPos, secondPos).expandTowards(1, 1, 1);
	}

	private boolean isActive() {
		return isPresent() && AllItems.SCHEMATIC_AND_QUILL.isIn(Minecraft.getInstance().player.getMainHandItem());
	}

	private boolean isPresent() {
		return Minecraft.getInstance() != null && Minecraft.getInstance().level != null
			&& Minecraft.getInstance().screen == null;
	}

	public void saveSchematic(String string, boolean convertImmediately) {
		StructureTemplate t = new StructureTemplate();
		BoundingBox bb = BoundingBox.fromCorners(firstPos, secondPos);
		BlockPos origin = new BlockPos(bb.minX(), bb.minY(), bb.minZ());
		BlockPos bounds = new BlockPos(bb.getXSpan(), bb.getYSpan(), bb.getZSpan());
		Level level = Minecraft.getInstance().level;

		t.fillFromWorld(level, origin, bounds, true, Blocks.AIR);

		if (string.isEmpty())
			string = Lang.translateDirect("schematicAndQuill.fallbackName")
				.getString();

		String folderPath = "schematics";
		FilesHelper.createFolderIfMissing(folderPath);
		String filename = FilesHelper.findFirstValidFilename(string, folderPath, "nbt");
		String filepath = folderPath + "/" + filename;

		Path path = Paths.get(filepath);
		OutputStream outputStream = null;
		try {
			outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE);
			CompoundTag nbttagcompound = t.save(new CompoundTag());
			SchematicAndQuillItem.replaceStructureVoidWithAir(nbttagcompound);
			SchematicAndQuillItem.clampGlueBoxes(level, new AABB(origin, origin.offset(bounds)), nbttagcompound);
			NbtIo.writeCompressed(nbttagcompound, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null)
				IOUtils.closeQuietly(outputStream);
		}
		firstPos = null;
		secondPos = null;
		LocalPlayer player = Minecraft.getInstance().player;
		Lang.translate("schematicAndQuill.saved", filepath)
			.sendStatus(player);

		if (!convertImmediately)
			return;
		if (!Files.exists(path)) {
			Create.LOGGER.error("Missing Schematic file: " + path.toString());
			return;
		}
		try {
			if (!ClientSchematicLoader.validateSizeLimitation(Files.size(path)))
				return;
			AllPackets.getChannel().sendToServer(new InstantSchematicPacket(filename, origin, bounds));

		} catch (IOException e) {
			Create.LOGGER.error("Error finding Schematic file: " + path.toString());
			e.printStackTrace();
			return;
		}
	}

	private Outliner outliner() {
		return CreateClient.OUTLINER;
	}

}