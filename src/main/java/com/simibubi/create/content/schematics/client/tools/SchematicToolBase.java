package com.simibubi.create.content.schematics.client.tools;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.content.schematics.client.SchematicTransformation;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;

public abstract class SchematicToolBase implements ISchematicTool {

	protected SchematicHandler schematicHandler;

	protected BlockPos selectedPos;
	protected Vec3d chasingSelectedPos;
	protected Vec3d lastChasingSelectedPos;

	protected boolean selectIgnoreBlocks;
	protected int selectionRange;
	protected boolean schematicSelected;
	protected boolean renderSelectedFace;
	protected Direction selectedFace;

	protected final List<String> mirrors = Arrays.asList("none", "leftRight", "frontBack");
	protected final List<String> rotations = Arrays.asList("none", "cw90", "cw180", "cw270");

	@Override
	public void init() {
		schematicHandler = CreateClient.schematicHandler;
		selectedPos = null;
		selectedFace = null;
		schematicSelected = false;
		chasingSelectedPos = Vec3d.ZERO;
		lastChasingSelectedPos = Vec3d.ZERO;
	}

	@Override
	public void updateSelection() {
		updateTargetPos();

		if (selectedPos == null)
			return;
		lastChasingSelectedPos = chasingSelectedPos;
		Vec3d target = new Vec3d(selectedPos);
		if (target.distanceTo(chasingSelectedPos) < 1 / 512f) {
			chasingSelectedPos = target;
			return;
		}

		chasingSelectedPos = chasingSelectedPos.add(target.subtract(chasingSelectedPos).scale(1 / 2f));
	}

	public void updateTargetPos() {
		ClientPlayerEntity player = Minecraft.getInstance().player;

		// Select Blueprint
		if (schematicHandler.isDeployed()) {
			SchematicTransformation transformation = schematicHandler.getTransformation();
			AxisAlignedBB localBounds = schematicHandler.getBounds();

			Vec3d traceOrigin = RaycastHelper.getTraceOrigin(player);
			Vec3d start = transformation.toLocalSpace(traceOrigin);
			Vec3d end = transformation.toLocalSpace(RaycastHelper.getTraceTarget(player, 70, traceOrigin));
			PredicateTraceResult result =
				RaycastHelper.rayTraceUntil(start, end, pos -> localBounds.contains(VecHelper.getCenterOf(pos)));

			schematicSelected = !result.missed();
			selectedFace = schematicSelected ? result.getFacing() : null;
		}

		boolean snap = this.selectedPos == null;

		// Select location at distance
		if (selectIgnoreBlocks) {
			float pt = Minecraft.getInstance().getRenderPartialTicks();
			selectedPos = new BlockPos(player.getEyePosition(pt).add(player.getLookVec().scale(selectionRange)));
			if (snap)
				lastChasingSelectedPos = chasingSelectedPos = new Vec3d(selectedPos);
			return;
		}

		// Select targeted Block
		selectedPos = null;
		BlockRayTraceResult trace = RaycastHelper.rayTraceRange(player.world, player, 75);
		if (trace == null || trace.getType() != Type.BLOCK)
			return;

		BlockPos hit = new BlockPos(trace.getHitVec());
		boolean replaceable = player.world.getBlockState(hit).getMaterial().isReplaceable();
		if (trace.getFace().getAxis().isVertical() && !replaceable)
			hit = hit.offset(trace.getFace());
		selectedPos = hit;
		if (snap)
			lastChasingSelectedPos = chasingSelectedPos = new Vec3d(selectedPos);
	}

	@Override
	public void renderTool(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		if (!schematicHandler.isDeployed())
			return;

//		AABBOutline outline = schematicHandler.getOutline();
		if (renderSelectedFace) {
//			schematicHandler.getOutline().setTextures(null,
//					AllKeys.ctrlDown() ? AllSpecialTextures.HIGHLIGHT_CHECKERED : AllSpecialTextures.CHECKERED);
//			outline.highlightFace(selectedFace);
		}

		RenderHelper.disableStandardItemLighting();
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
//		outline.render(Tessellator.getInstance().getBuffer());TODO
		RenderSystem.popMatrix();
//		outline.setTextures(null, null);

	}

	@Override
	public void renderOverlay(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {}
	
	@Override
	public void renderToolLocal(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {}

}
