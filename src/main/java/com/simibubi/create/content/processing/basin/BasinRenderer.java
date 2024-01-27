package com.simibubi.create.content.processing.basin;

import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class BasinRenderer extends SmartBlockEntityRenderer<BasinBlockEntity> {

	public BasinRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(BasinBlockEntity basin, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(basin, partialTicks, ms, buffer, light, overlay);

		float fluidLevel = renderFluids(basin, partialTicks, ms, buffer, light, overlay);
		float level = Mth.clamp(fluidLevel - .3f, .125f, .6f);

		ms.pushPose();

		BlockPos pos = basin.getBlockPos();
		ms.translate(.5, .2f, .5);
		TransformStack.of(ms)
			.rotateYDegrees(basin.ingredientRotation.getValue(partialTicks));

		RandomSource r = RandomSource.create(pos.hashCode());
		Vec3 baseVector = new Vec3(.125, level, 0);

		IItemHandlerModifiable inv = basin.itemCapability.orElse(new ItemStackHandler());
		int itemCount = 0;
		for (int slot = 0; slot < inv.getSlots(); slot++)
			if (!inv.getStackInSlot(slot)
				.isEmpty())
				itemCount++;

		if (itemCount == 1)
			baseVector = new Vec3(0, level, 0);

		float anglePartition = 360f / itemCount;
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;

			ms.pushPose();

			if (fluidLevel > 0) {
				ms.translate(0,
					(Mth.sin(
						AnimationTickHolder.getRenderTime(basin.getLevel()) / 12f + anglePartition * itemCount) + 1.5f)
						* 1 / 32f,
					0);
			}

			Vec3 itemPosition = VecHelper.rotate(baseVector, anglePartition * itemCount, Axis.Y);
			ms.translate(itemPosition.x, itemPosition.y, itemPosition.z);
            TransformStack.of(ms)
				.rotateYDegrees(anglePartition * itemCount + 35)
				.rotateXDegrees(65);

			for (int i = 0; i <= stack.getCount() / 8; i++) {
				ms.pushPose();

				Vec3 vec = VecHelper.offsetRandomly(Vec3.ZERO, r, 1 / 16f);

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
		Vec3 directionVec = Vec3.atLowerCornerOf(direction.getNormal());
		Vec3 outVec = VecHelper.getCenterOf(BlockPos.ZERO)
			.add(directionVec.scale(.55)
				.subtract(0, 1 / 2f, 0));

		boolean outToBasin = basin.getLevel()
			.getBlockState(basin.getBlockPos()
				.relative(direction))
			.getBlock() instanceof BasinBlock;

		for (IntAttached<ItemStack> intAttached : basin.visualizedOutputItems) {
			float progress = 1 - (intAttached.getFirst() - partialTicks) / BasinBlockEntity.OUTPUT_ANIMATION_TIME;

			if (!outToBasin && progress > .35f)
				continue;

			ms.pushPose();
            TransformStack.of(ms)
				.translate(outVec)
				.translate(new Vec3(0, Math.max(-.55f, -(progress * progress * 2)), 0))
				.translate(directionVec.scale(progress * .5f))
				.rotateYDegrees(AngleHelper.horizontalAngle(direction))
				.rotateXDegrees(progress * 180);
			renderItem(ms, buffer, light, overlay, intAttached.getValue());
			ms.popPose();
		}
	}

	protected void renderItem(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		mc.getItemRenderer()
			.renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, ms, buffer, mc.level, 0);
	}

	protected float renderFluids(BasinBlockEntity basin, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		SmartFluidTankBehaviour inputFluids = basin.getBehaviour(SmartFluidTankBehaviour.INPUT);
		SmartFluidTankBehaviour outputFluids = basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT);
		SmartFluidTankBehaviour[] tanks = { inputFluids, outputFluids };
		float totalUnits = basin.getTotalFluidUnits(partialTicks);
		if (totalUnits < 1)
			return 0;

		float fluidLevel = Mth.clamp(totalUnits / 2000, 0, 1);

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

				float partial = Mth.clamp(units / totalUnits, 0, 1);
				xMax += partial * 12 / 16f;
				FluidRenderer.renderFluidBox(renderedFluid, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light,
					false);

				xMin = xMax;
			}
		}

		return yMax;
	}

	@Override
	public int getViewDistance() {
		return 16;
	}

}
