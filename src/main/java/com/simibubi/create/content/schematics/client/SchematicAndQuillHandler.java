package com.simibubi.create.content.schematics.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.Create;
import com.simibubi.create.content.schematics.SchematicExport;
import com.simibubi.create.content.schematics.SchematicExport.SchematicExportResult;
import com.simibubi.create.content.schematics.packet.InstantSchematicPacket;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.outliner.Outliner;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class SchematicAndQuillHandler {

	private Object outlineSlot = new Object();

	public BlockPos firstPos;
	public BlockPos secondPos;
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

		firstPos = BlockPos.containing(bb.minX, bb.minY, bb.minZ);
		secondPos = BlockPos.containing(bb.maxX, bb.maxY, bb.maxZ);
		LocalPlayer player = Minecraft.getInstance().player;
		CreateLang.translate("schematicAndQuill.dimensions", (int) bb.getXsize() + 1, (int) bb.getYsize() + 1,
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
			CreateLang.translate("schematicAndQuill.noTarget")
				.sendStatus(player);
			return true;
		}

		if (firstPos != null) {
			secondPos = selectedPos;
			CreateLang.translate("schematicAndQuill.secondPos")
				.sendStatus(player);
			return true;
		}

		firstPos = selectedPos;
		CreateLang.translate("schematicAndQuill.firstPos")
			.sendStatus(player);
		return true;
	}

	public void discard() {
		LocalPlayer player = Minecraft.getInstance().player;
		firstPos = null;
		secondPos = null;
		CreateLang.translate("schematicAndQuill.abort")
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
			selectedPos = BlockPos.containing(targetVec);

		} else {
			BlockHitResult trace = RaycastHelper.rayTraceRange(player.level(), player, 75);
			if (trace != null && trace.getType() == Type.BLOCK) {

				BlockPos hit = trace.getBlockPos();
				boolean replaceable = player.level().getBlockState(hit)
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
		SchematicExportResult result = SchematicExport.saveSchematic(
				SchematicExport.SCHEMATICS, string, false,
				Minecraft.getInstance().level, firstPos, secondPos
		);
		LocalPlayer player = Minecraft.getInstance().player;
		if (result == null) {
			CreateLang.translate("schematicAndQuill.failed")
					.style(ChatFormatting.RED)
					.sendStatus(player);
			return;
		}
		Path file = result.file();
		CreateLang.translate("schematicAndQuill.saved", file.getFileName())
				.sendStatus(player);
		firstPos = null;
		secondPos = null;
		if (!convertImmediately)
			return;
		try {
			if (!ClientSchematicLoader.validateSizeLimitation(Files.size(file)))
				return;
			AllPackets.getChannel()
				.sendToServer(new InstantSchematicPacket(result.fileName(), result.origin(), result.bounds()));
		} catch (IOException e) {
			Create.LOGGER.error("Error instantly uploading Schematic file: " + file, e);
		}
	}

	private Outliner outliner() {
		return CatnipClient.OUTLINER;
	}

}
