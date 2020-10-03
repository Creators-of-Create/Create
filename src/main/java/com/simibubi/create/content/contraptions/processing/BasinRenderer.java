package com.simibubi.create.content.contraptions.processing;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.MathHelper;
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

		float fluidSurface = renderFluids(basin, partialTicks, ms, buffer, light, overlay);

		ms.push();
		BlockPos pos = basin.getPos();
		ms.translate(.5, .2f, .5);
		Random r = new Random(pos.hashCode());

		IItemHandlerModifiable inv = basin.itemCapability.orElse(new ItemStackHandler());
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;

			for (int i = 0; i <= stack.getCount() / 8; i++) {
				ms.push();
				Vector3d vec = VecHelper.offsetRandomly(Vector3d.ZERO, r, .25f);
				Vector3d vec2 = VecHelper.offsetRandomly(Vector3d.ZERO, r, .5f);
				ms.translate(vec.x, vec.y, vec.z);
				ms.multiply(new Vector3f((float) vec2.z, (float) vec2.y, 0).getDegreesQuaternion((float) vec2.x * 180));

				Minecraft.getInstance()
					.getItemRenderer()
					.renderItem(stack, TransformType.GROUND, light, overlay, ms, buffer);
				ms.pop();
			}
			ms.translate(0, 1 / 64f, 0);
		}
		ms.pop();

	}

	protected float renderFluids(BasinTileEntity basin, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		SmartFluidTankBehaviour inputFluids = basin.getBehaviour(SmartFluidTankBehaviour.INPUT);
		SmartFluidTankBehaviour outputFluids = basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT);
		SmartFluidTankBehaviour[] tanks = { inputFluids, outputFluids };
		int renderedFluids = 0;
		float totalUnits = 0;

		for (SmartFluidTankBehaviour behaviour : tanks) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				if (tankSegment.getRenderedFluid()
					.isEmpty())
					continue;
				totalUnits += tankSegment.getTotalUnits(partialTicks);
				renderedFluids++;
			}
		}

		if (renderedFluids == 0)
			return 0;
		if (totalUnits == 0)
			return 0;
		
		float fluidLevel = MathHelper.clamp(totalUnits / 2000, 0, 1);

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

				float partial = tankSegment.getTotalUnits(partialTicks) / totalUnits;
				xMax += partial * 12 / 16f;
				FluidRenderer.renderTiledFluidBB(renderedFluid, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light,
					false);

				xMin = xMax;
			}
		}
		
		return fluidLevel;
	}

}
