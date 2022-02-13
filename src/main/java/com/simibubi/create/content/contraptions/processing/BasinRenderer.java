package com.simibubi.create.content.contraptions.processing;

import java.util.Random;

import com.jozufozu.flywheel.util.transform.MatrixTransformStack;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class BasinRenderer extends SmartTileEntityRenderer<BasinTileEntity> {

	public BasinRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(BasinTileEntity basin, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(basin, partialTicks, ms, buffer, light, overlay);

		float fluidLevel = renderFluids(basin, partialTicks, ms, buffer, light, overlay);
		float level = MathHelper.clamp(fluidLevel - .3f, .125f, .6f);

		ms.pushPose();

		BlockPos pos = basin.getBlockPos();
		ms.translate(.5, .2f, .5);
		MatrixTransformStack.of(ms)
			.rotateY(basin.ingredientRotation.getValue(partialTicks));

		Random r = new Random(pos.hashCode());
		Vector3d baseVector = new Vector3d(.125, level, 0);

		IItemHandlerModifiable inv = basin.itemCapability.orElse(new ItemStackHandler());
		int itemCount = 0;
		for (int slot = 0; slot < inv.getSlots(); slot++)
			if (!inv.getStackInSlot(slot)
				.isEmpty())
				itemCount++;

		if (itemCount == 1)
			baseVector = new Vector3d(0, level, 0);

		float anglePartition = 360f / itemCount;
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;

			ms.pushPose();

			if (fluidLevel > 0) {
				ms.translate(0,
					(MathHelper.sin(
						AnimationTickHolder.getRenderTime(basin.getLevel()) / 12f + anglePartition * itemCount) + 1.5f)
						* 1 / 32f,
					0);
			}

			Vector3d itemPosition = VecHelper.rotate(baseVector, anglePartition * itemCount, Axis.Y);
			ms.translate(itemPosition.x, itemPosition.y, itemPosition.z);
			MatrixTransformStack.of(ms)
				.rotateY(anglePartition * itemCount + 35)
				.rotateX(65);

			for (int i = 0; i <= stack.getCount() / 8; i++) {
				ms.pushPose();

				Vector3d vec = VecHelper.offsetRandomly(Vector3d.ZERO, r, 1 / 16f);

				ms.translate(vec.x, vec.y, vec.z);
				renderItem(ms, buffer, light, overlay, stack);
				ms.popPose();
			}
			ms.popPose();

			itemCount--;
		}
		ms.popPose();

		BlockState blockState = basin.getBlockState();
		if (!(blockState.getBlock() instanceof BasinBlock))
			return;
		Direction direction = blockState.getValue(BasinBlock.FACING);
		if (direction == Direction.DOWN)
			return;
		Vector3d directionVec = Vector3d.atLowerCornerOf(direction.getNormal());
		Vector3d outVec = VecHelper.getCenterOf(BlockPos.ZERO)
			.add(directionVec.scale(.55)
				.subtract(0, 1 / 2f, 0));

		boolean outToBasin = basin.getLevel()
			.getBlockState(basin.getBlockPos()
				.relative(direction))
			.getBlock() instanceof BasinBlock;

		for (IntAttached<ItemStack> intAttached : basin.visualizedOutputItems) {
			float progress = 1 - (intAttached.getFirst() - partialTicks) / BasinTileEntity.OUTPUT_ANIMATION_TIME;

			if (!outToBasin && progress > .35f)
				continue;

			ms.pushPose();
			MatrixTransformStack.of(ms)
				.translate(outVec)
				.translate(new Vector3d(0, Math.max(-.55f, -(progress * progress * 2)), 0))
				.translate(directionVec.scale(progress * .5f))
				.rotateY(AngleHelper.horizontalAngle(direction))
				.rotateX(progress * 180);
			renderItem(ms, buffer, light, overlay, intAttached.getValue());
			ms.popPose();
		}
	}

	protected void renderItem(MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay, ItemStack stack) {
		Minecraft.getInstance()
			.getItemRenderer()
			.renderStatic(stack, TransformType.GROUND, light, overlay, ms, buffer);
	}

	protected float renderFluids(BasinTileEntity basin, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		SmartFluidTankBehaviour inputFluids = basin.getBehaviour(SmartFluidTankBehaviour.INPUT);
		SmartFluidTankBehaviour outputFluids = basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT);
		SmartFluidTankBehaviour[] tanks = { inputFluids, outputFluids };
		float totalUnits = basin.getTotalFluidUnits(partialTicks);
		if (totalUnits < 1)
			return 0;

		float fluidLevel = MathHelper.clamp(totalUnits / 2000, 0, 1);
		
		fluidLevel = 1 - ((1 - fluidLevel) * (1 - fluidLevel));

		float xMin = 2 / 16f;
		float xMax = 2 / 16f;
		final float yMin = 2 / 16f;
		final float yMax = yMin + 12 / 16f * fluidLevel;
		final float zMin = 2 / 16f;
		final float zMax = 14 / 16f;

		for (SmartFluidTankBehaviour behaviour : tanks) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				FluidStack renderedFluid = tankSegment.getRenderedFluid();
				if (renderedFluid.isEmpty())
					continue;
				float units = tankSegment.getTotalUnits(partialTicks);
				if (units < 1)
					continue;

				float partial = MathHelper.clamp(units / totalUnits, 0, 1);
				xMax += partial * 12 / 16f;
				FluidRenderer.renderTiledFluidBB(renderedFluid, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light,
					false);

				xMin = xMax;
			}
		}

		return yMax;
	}

}
