package com.simibubi.create.schematic.tools;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.schematic.BlueprintHandler;
import com.simibubi.create.utility.Keyboard;
import com.simibubi.create.utility.RaycastHelper;
import com.simibubi.create.utility.RaycastHelper.PredicateTraceResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;

public abstract class SchematicToolBase implements ISchematicTool {

	protected BlueprintHandler blueprint;

	public BlockPos selectedPos;
	public boolean selectIgnoreBlocks;
	public int selectionRange;

	public boolean schematicSelected;
	public boolean renderSelectedFace;
	public Direction selectedFace;

	public SchematicToolBase() {
		blueprint = BlueprintHandler.instance;
	}

	@Override
	public void init() {
		selectedPos = null;
		selectedFace = null;
		schematicSelected = false;
	}

	@Override
	public void updateSelection() {
		ClientPlayerEntity player = Minecraft.getInstance().player;

		// Select Blueprint
		if (blueprint.deployed) {
			BlockPos min = blueprint.getTransformedAnchor();
			MutableBoundingBox bb = new MutableBoundingBox(min, min.add(blueprint.getTransformedSize()));
			PredicateTraceResult result = RaycastHelper.rayTraceUntil(player, 70,
					pos -> bb.isVecInside(pos));
			schematicSelected = !result.missed();
			selectedFace = schematicSelected ? result.getFacing() : null;
		}

		// Select location at distance
		if (selectIgnoreBlocks) {
			selectedPos = new BlockPos(player.getEyePosition(Minecraft.getInstance().getRenderPartialTicks())
					.add(player.getLookVec().scale(selectionRange)));
			return;
		}

		// Select targeted Block
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

	@Override
	public void renderTool() {

		if (blueprint.deployed) {
			GlStateManager.lineWidth(2);
			GlStateManager.color4f(1, 1, 1, 1);
			GlStateManager.disableTexture();

			BlockPos min = blueprint.getTransformedAnchor();
			MutableBoundingBox bb = new MutableBoundingBox(min, min.add(blueprint.getTransformedSize()));
			min = new BlockPos(bb.minX, bb.minY, bb.minZ);
			BlockPos max = new BlockPos(bb.maxX, bb.maxY, bb.maxZ);

			WorldRenderer.drawBoundingBox(min.getX() - 1 / 8d, min.getY() + 1 / 16d, min.getZ() - 1 / 8d,
					max.getX() + 1 / 8d, max.getY() + 1 / 8d, max.getZ() + 1 / 8d, 1, 1, 1, 1);

			if (schematicSelected && renderSelectedFace && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
				Vec3d vec = new Vec3d(selectedFace.getDirectionVec());
				Vec3d center = new Vec3d(min.add(max)).scale(1 / 2f);
				Vec3d radii = new Vec3d(max.subtract(min)).scale(1 / 2f);

				Vec3d onFaceOffset = new Vec3d(1 - Math.abs(vec.x), 1 - Math.abs(vec.y), 1 - Math.abs(vec.z))
						.mul(radii);
				Vec3d faceMin = center.add(vec.mul(radii).add(onFaceOffset)).add(vec.scale(1/8f));
				Vec3d faceMax = center.add(vec.mul(radii).subtract(onFaceOffset)).add(vec.scale(1/8f));

				GlStateManager.lineWidth(6);
				WorldRenderer.drawBoundingBox(faceMin.getX(), faceMin.getY() + 1 / 16d, faceMin.getZ(), faceMax.getX(),
						faceMax.getY() + 1 / 8d, faceMax.getZ(), .6f, .7f, 1, 1);
			}

			GlStateManager.lineWidth(1);
			GlStateManager.enableTexture();

		}

	}

	@Override
	public void renderOverlay() {

	}

}
