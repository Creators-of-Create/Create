package com.simibubi.create.content.contraptions.processing;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

		ms.push();
		BlockPos pos = basin.getPos();
		ms.translate(.5, .2f, .5);
		Random r = new Random(pos.hashCode());

		IItemHandlerModifiable inv = basin.inventory.orElse(new ItemStackHandler());
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;

			for (int i = 0; i <= stack.getCount() / 8; i++) {
				ms.push();
				Vec3d vec = VecHelper.offsetRandomly(Vec3d.ZERO, r, .25f);
				Vec3d vec2 = VecHelper.offsetRandomly(Vec3d.ZERO, r, .5f);
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

}
