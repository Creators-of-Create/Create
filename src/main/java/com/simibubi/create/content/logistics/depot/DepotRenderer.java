package com.simibubi.create.content.logistics.depot;

import java.util.Random;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DepotRenderer extends SafeBlockEntityRenderer<DepotBlockEntity> {

	public DepotRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(DepotBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		renderItemsOf(be, partialTicks, ms, buffer, light, overlay, be.depotBehaviour);
	}

	public static void renderItemsOf(SmartBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay, DepotBehaviour depotBehaviour) {

		TransportedItemStack transported = depotBehaviour.heldItem;
		TransformStack msr = TransformStack.cast(ms);
		Vec3 itemPosition = VecHelper.getCenterOf(be.getBlockPos());

		ms.pushPose();
		ms.translate(.5f, 15 / 16f, .5f);

		if (transported != null)
			depotBehaviour.incoming.add(transported);

		// Render main items
		for (TransportedItemStack tis : depotBehaviour.incoming) {
			ms.pushPose();
			msr.nudge(0);
			float offset = Mth.lerp(partialTicks, tis.prevBeltPosition, tis.beltPosition);
			float sideOffset = Mth.lerp(partialTicks, tis.prevSideOffset, tis.sideOffset);

			if (tis.insertedFrom.getAxis()
				.isHorizontal()) {
				Vec3 offsetVec = Vec3.atLowerCornerOf(tis.insertedFrom.getOpposite()
					.getNormal()).scale(.5f - offset);
				ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);
				boolean alongX = tis.insertedFrom.getClockWise()
					.getAxis() == Direction.Axis.X;
				if (!alongX)
					sideOffset *= -1;
				ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
			}

			ItemStack itemStack = tis.stack;
			int angle = tis.angle;
			Random r = new Random(0);
			renderItem(be.getLevel(), ms, buffer, light, overlay, itemStack, angle, r, itemPosition);
			ms.popPose();
		}

		if (transported != null)
			depotBehaviour.incoming.remove(transported);

		// Render output items
		for (int i = 0; i < depotBehaviour.processingOutputBuffer.getSlots(); i++) {
			ItemStack stack = depotBehaviour.processingOutputBuffer.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			ms.pushPose();
			msr.nudge(i);

			boolean renderUpright = BeltHelper.isItemUpright(stack);
			msr.rotateY(360 / 8f * i);
			ms.translate(.35f, 0, 0);
			if (renderUpright)
				msr.rotateY(-(360 / 8f * i));
			Random r = new Random(i + 1);
			int angle = (int) (360 * r.nextFloat());
			renderItem(be.getLevel(), ms, buffer, light, overlay, stack, renderUpright ? angle + 90 : angle, r, itemPosition);
			ms.popPose();
		}

		ms.popPose();
	}

	public static void renderItem(Level level, PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack itemStack,
		int angle, Random r, Vec3 itemPosition) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		TransformStack msr = TransformStack.cast(ms);
		int count = (int) (Mth.log2((int) (itemStack.getCount()))) / 2;
		boolean renderUpright = BeltHelper.isItemUpright(itemStack);
		BakedModel bakedModel = itemRenderer.getModel(itemStack, null, null, 0);
		boolean blockItem = bakedModel.isGui3d();

		ms.pushPose();
		msr.rotateY(angle);

		if (renderUpright) {
			Entity renderViewEntity = Minecraft.getInstance().cameraEntity;
			if (renderViewEntity != null) {
				Vec3 positionVec = renderViewEntity.position();
				Vec3 vectorForOffset = itemPosition;
				Vec3 diff = vectorForOffset.subtract(positionVec);
				float yRot = (float) (Mth.atan2(diff.x, diff.z) + Math.PI);
				ms.mulPose(Axis.YP.rotation(yRot));
			}
			ms.translate(0, 3 / 32d, -1 / 16f);
		}

		for (int i = 0; i <= count; i++) {
			ms.pushPose();
			if (blockItem)
				ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
			ms.scale(.5f, .5f, .5f);
			if (!blockItem && !renderUpright) {
				ms.translate(0, -3 / 16f, 0);
				msr.rotateX(90);
			}
			itemRenderer.render(itemStack, ItemDisplayContext.FIXED, false, ms, buffer, light, overlay, bakedModel);
			ms.popPose();

			if (!renderUpright) {
				if (!blockItem)
					msr.rotateY(10);
				ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);
			} else
				ms.translate(0, 0, -1 / 16f);
		}

		ms.popPose();
	}

}
