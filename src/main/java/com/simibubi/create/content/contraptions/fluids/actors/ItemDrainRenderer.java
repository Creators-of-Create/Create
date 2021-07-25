package com.simibubi.create.content.contraptions.fluids.actors;

import java.util.Random;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fluids.FluidStack;

public class ItemDrainRenderer extends SmartTileEntityRenderer<ItemDrainTileEntity> {

	public ItemDrainRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(ItemDrainTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		renderFluid(te, partialTicks, ms, buffer, light);
		renderItem(te, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderItem(ItemDrainTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		TransportedItemStack transported = te.heldItem;
		if (transported == null)
			return;

		MatrixTransformStack msr = MatrixTransformStack.of(ms);
		Vector3d itemPosition = VecHelper.getCenterOf(te.getBlockPos());

		Direction insertedFrom = transported.insertedFrom;
		if (!insertedFrom.getAxis()
			.isHorizontal())
			return;

		ms.pushPose();
		ms.translate(.5f, 15 / 16f, .5f);
		msr.nudge(0);
		float offset = MathHelper.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);
		float sideOffset = MathHelper.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);

		Vector3d offsetVec = Vector3d.atLowerCornerOf(insertedFrom.getOpposite()
			.getNormal())
			.scale(.5f - offset);
		ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);
		boolean alongX = insertedFrom.getClockWise()
			.getAxis() == Axis.X;
		if (!alongX)
			sideOffset *= -1;
		ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

		ItemStack itemStack = transported.stack;
		Random r = new Random(0);
		ItemRenderer itemRenderer = Minecraft.getInstance()
			.getItemRenderer();
		int count = (int) (MathHelper.log2((int) (itemStack.getCount()))) / 2;
		boolean renderUpright = BeltHelper.isItemUpright(itemStack);
		boolean blockItem = itemRenderer.getModel(itemStack, null, null)
			.isGui3d();

		if (renderUpright)
			ms.translate(0, 3 / 32d, 0);

		int positive = insertedFrom.getAxisDirection()
			.getStep();
		float verticalAngle = positive * offset * 360;
		if (insertedFrom.getAxis() != Axis.X)
			msr.rotateX(verticalAngle);
		if (insertedFrom.getAxis() != Axis.Z)
			msr.rotateZ(-verticalAngle);

		if (renderUpright) {
			Entity renderViewEntity = Minecraft.getInstance().cameraEntity;
			if (renderViewEntity != null) {
				Vector3d positionVec = renderViewEntity.position();
				Vector3d vectorForOffset = itemPosition.add(offsetVec);
				Vector3d diff = vectorForOffset.subtract(positionVec);

				if (insertedFrom.getAxis() != Axis.X)
					diff = VecHelper.rotate(diff, verticalAngle, Axis.X);
				if (insertedFrom.getAxis() != Axis.Z)
					diff = VecHelper.rotate(diff, -verticalAngle, Axis.Z);

				float yRot = (float) MathHelper.atan2(diff.z, -diff.x);
				ms.mulPose(Vector3f.YP.rotation((float) (yRot - Math.PI / 2)));
			}
			ms.translate(0, 0, -1 / 16f);
		}

		for (int i = 0; i <= count; i++) {
			ms.pushPose();
			if (blockItem)
				ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
			ms.scale(.5f, .5f, .5f);
			if (!blockItem && !renderUpright)
				msr.rotateX(90);
			itemRenderer.renderStatic(itemStack, TransformType.FIXED, light, overlay, ms, buffer);
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

	protected void renderFluid(ItemDrainTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light) {
		SmartFluidTankBehaviour tank = te.internalTank;
		if (tank == null)
			return;

		TankSegment primaryTank = tank.getPrimaryTank();
		FluidStack fluidStack = primaryTank.getRenderedFluid();
		float level = primaryTank.getFluidLevel()
			.getValue(partialTicks);

		if (!fluidStack.isEmpty() && level != 0) {
			float yMin = 5f / 16f;
			float min = 2f / 16f;
			float max = min + (12 / 16f);
			float yOffset = (7 / 16f) * level;
			ms.pushPose();
			ms.translate(0, yOffset, 0);
			FluidRenderer.renderTiledFluidBB(fluidStack, min, yMin - yOffset, min, max, yMin, max, buffer, ms, light,
				false);
			ms.popPose();
		}

		ItemStack heldItemStack = te.getHeldItemStack();
		if (heldItemStack.isEmpty())
			return;
		FluidStack fluidStack2 = EmptyingByBasin.emptyItem(te.getLevel(), heldItemStack, true)
			.getFirst();
		if (fluidStack2.isEmpty()) {
			if (fluidStack.isEmpty())
				return;
			fluidStack2 = fluidStack;
		}

		int processingTicks = te.processingTicks;
		float processingPT = te.processingTicks - partialTicks;
		float processingProgress = 1 - (processingPT - 5) / 10;
		processingProgress = MathHelper.clamp(processingProgress, 0, 1);
		float radius = 0;

		if (processingTicks != -1) {
			radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
			AxisAlignedBB bb = new AxisAlignedBB(0.5, 1.0, 0.5, 0.5, 0.25, 0.5).inflate(radius / 32f);
			FluidRenderer.renderTiledFluidBB(fluidStack2, (float) bb.minX, (float) bb.minY, (float) bb.minZ,
				(float) bb.maxX, (float) bb.maxY, (float) bb.maxZ, buffer, ms, light, true);
		}

	}

}
