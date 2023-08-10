package com.simibubi.create.content.schematics.client.tools;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.schematics.client.SchematicTransformation;

import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.outliner.AABBOutline;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DeployTool extends PlacementToolBase {

	@Override
	public void init() {
		super.init();
		selectionRange = -1;
	}

	@Override
	public void updateSelection() {
		if (schematicHandler.isActive() && selectionRange == -1) {
			selectionRange = (int) (schematicHandler.getBounds()
				.getCenter()
				.length() / 2);
			selectionRange = Mth.clamp(selectionRange, 1, 100);
		}
		selectIgnoreBlocks = AllKeys.ACTIVATE_TOOL.isPressed();
		super.updateSelection();
	}

	@Override
	public void renderTool(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera) {
		super.renderTool(ms, buffer, camera);

		if (selectedPos == null)
			return;

		ms.pushPose();
		float pt = AnimationTickHolder.getPartialTicks();
		double x = Mth.lerp(pt, lastChasingSelectedPos.x, chasingSelectedPos.x);
		double y = Mth.lerp(pt, lastChasingSelectedPos.y, chasingSelectedPos.y);
		double z = Mth.lerp(pt, lastChasingSelectedPos.z, chasingSelectedPos.z);

		SchematicTransformation transformation = schematicHandler.getTransformation();
		AABB bounds = schematicHandler.getBounds();
		Vec3 center = bounds.getCenter();
		Vec3 rotationOffset = transformation.getRotationOffset(true);
		int centerX = (int) center.x;
		int centerZ = (int) center.z;
		double xOrigin = bounds.getXsize() / 2f;
		double zOrigin = bounds.getZsize() / 2f;
		Vec3 origin = new Vec3(xOrigin, 0, zOrigin);

		ms.translate(x - centerX - camera.x, y - camera.y, z - centerZ - camera.z);
		TransformStack.cast(ms)
			.translate(origin)
			.translate(rotationOffset)
			.rotateY(transformation.getCurrentRotation())
			.translateBack(rotationOffset)
			.translateBack(origin);

		AABBOutline outline = schematicHandler.getOutline();
		outline.render(ms, buffer, Vec3.ZERO, pt);
		outline.getParams()
			.clearTextures();
		ms.popPose();
	}

	@Override
	public boolean handleMouseWheel(double delta) {
		if (!selectIgnoreBlocks)
			return super.handleMouseWheel(delta);
		selectionRange += delta;
		selectionRange = Mth.clamp(selectionRange, 1, 100);
		return true;
	}

	@Override
	public boolean handleRightClick() {
		if (selectedPos == null)
			return super.handleRightClick();
		Vec3 center = schematicHandler.getBounds()
			.getCenter();
		BlockPos target = selectedPos.offset(-((int) center.x), 0, -((int) center.z));

		ItemStack item = schematicHandler.getActiveSchematicItem();
		if (item != null) {
			item.getTag()
				.putBoolean("Deployed", true);
			item.getTag()
				.put("Anchor", NbtUtils.writeBlockPos(target));
			schematicHandler.getTransformation()
				.startAt(target);
		}

		schematicHandler.getTransformation()
			.moveTo(target);
		schematicHandler.markDirty();
		schematicHandler.deploy();
		return true;
	}

}
