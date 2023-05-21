package com.simibubi.create.content.schematics.client.tools;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.content.schematics.client.SchematicTransformation;
import com.simibubi.create.foundation.outliner.AABBOutline;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.ForgeIngameGui;

public abstract class SchematicToolBase implements ISchematicTool {

	protected SchematicHandler schematicHandler;

	protected BlockPos selectedPos;
	protected Vec3 chasingSelectedPos;
	protected Vec3 lastChasingSelectedPos;

	protected boolean selectIgnoreBlocks;
	protected int selectionRange;
	protected boolean schematicSelected;
	protected boolean renderSelectedFace;
	protected Direction selectedFace;

	protected final List<String> mirrors = Arrays.asList("none", "leftRight", "frontBack");
	protected final List<String> rotations = Arrays.asList("none", "cw90", "cw180", "cw270");

	@Override
	public void init() {
		schematicHandler = CreateClient.SCHEMATIC_HANDLER;
		selectedPos = null;
		selectedFace = null;
		schematicSelected = false;
		chasingSelectedPos = Vec3.ZERO;
		lastChasingSelectedPos = Vec3.ZERO;
	}

	@Override
	public void updateSelection() {
		updateTargetPos();

		if (selectedPos == null)
			return;
		lastChasingSelectedPos = chasingSelectedPos;
		Vec3 target = Vec3.atLowerCornerOf(selectedPos);
		if (target.distanceTo(chasingSelectedPos) < 1 / 512f) {
			chasingSelectedPos = target;
			return;
		}

		chasingSelectedPos = chasingSelectedPos.add(target.subtract(chasingSelectedPos)
			.scale(1 / 2f));
	}

	public void updateTargetPos() {
		LocalPlayer player = Minecraft.getInstance().player;

		// Select Blueprint
		if (schematicHandler.isDeployed()) {
			SchematicTransformation transformation = schematicHandler.getTransformation();
			AABB localBounds = schematicHandler.getBounds();

			Vec3 traceOrigin = RaycastHelper.getTraceOrigin(player);
			Vec3 start = transformation.toLocalSpace(traceOrigin);
			Vec3 end = transformation.toLocalSpace(RaycastHelper.getTraceTarget(player, 70, traceOrigin));
			PredicateTraceResult result =
				RaycastHelper.rayTraceUntil(start, end, pos -> localBounds.contains(VecHelper.getCenterOf(pos)));

			schematicSelected = !result.missed();
			selectedFace = schematicSelected ? result.getFacing() : null;
		}

		boolean snap = this.selectedPos == null;

		// Select location at distance
		if (selectIgnoreBlocks) {
			float pt = AnimationTickHolder.getPartialTicks();
			selectedPos = new BlockPos(player.getEyePosition(pt)
				.add(player.getLookAngle()
					.scale(selectionRange)));
			if (snap)
				lastChasingSelectedPos = chasingSelectedPos = Vec3.atLowerCornerOf(selectedPos);
			return;
		}

		// Select targeted Block
		selectedPos = null;
		BlockHitResult trace = RaycastHelper.rayTraceRange(player.level, player, 75);
		if (trace == null || trace.getType() != Type.BLOCK)
			return;

		BlockPos hit = new BlockPos(trace.getLocation());
		boolean replaceable = player.level.getBlockState(hit)
			.getMaterial()
			.isReplaceable();
		if (trace.getDirection()
			.getAxis()
			.isVertical() && !replaceable)
			hit = hit.relative(trace.getDirection());
		selectedPos = hit;
		if (snap)
			lastChasingSelectedPos = chasingSelectedPos = Vec3.atLowerCornerOf(selectedPos);
	}

	@Override
	public void renderTool(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {}

	@Override
	public void renderOverlay(ForgeIngameGui gui, PoseStack poseStack, float partialTicks, int width, int height) {}

	@Override
	public void renderOnSchematic(PoseStack ms, SuperRenderTypeBuffer buffer) {
		if (!schematicHandler.isDeployed())
			return;

		ms.pushPose();
		AABBOutline outline = schematicHandler.getOutline();
		if (renderSelectedFace) {
			outline.getParams()
				.highlightFace(selectedFace)
				.withFaceTextures(AllSpecialTextures.CHECKERED,
					AllKeys.ctrlDown() ? AllSpecialTextures.HIGHLIGHT_CHECKERED : AllSpecialTextures.CHECKERED);
		}
		outline.getParams()
			.colored(0x6886c5)
			.withFaceTexture(AllSpecialTextures.CHECKERED)
			.lineWidth(1 / 16f);
		outline.render(ms, buffer, Vec3.ZERO, AnimationTickHolder.getPartialTicks());
		outline.getParams()
			.clearTextures();
		ms.popPose();
	}

}
