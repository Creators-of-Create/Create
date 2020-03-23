package com.simibubi.create.modules.contraptions.processing;

import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.block.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

@SuppressWarnings("deprecation")
public class BasinTileEntityRenderer extends SafeTileEntityRenderer<BasinTileEntity> {

	@Override
	public void renderWithGL(BasinTileEntity basin, double x, double y, double z, float partialTicks, int destroyStage) {
		RenderSystem.pushMatrix();
		BlockPos pos = basin.getPos();
		RenderSystem.translated(x + .5, y + .2f, z + .5);
		Random r = new Random(pos.hashCode());

		IItemHandlerModifiable inv = basin.inventory.orElse(new ItemStackHandler());
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack = inv.getStackInSlot(slot);
			if (stack.isEmpty())
				continue;

			for (int i = 0; i <= stack.getCount() / 8; i++) {
				RenderSystem.pushMatrix();
				Vec3d vec = VecHelper.offsetRandomly(Vec3d.ZERO, r, .25f);
				Vec3d vec2 = VecHelper.offsetRandomly(Vec3d.ZERO, r, .5f);
				RenderSystem.translated(vec.x, vec.y, vec.z);
				RenderSystem.rotated(vec2.x * 180, vec2.z, vec2.y, 0);

				Minecraft.getInstance().getItemRenderer().renderItem(stack, TransformType.GROUND);
				RenderSystem.popMatrix();
			}
			RenderSystem.translated(0, 1 / 64f, 0);
		}
		RenderSystem.popMatrix();

	}

}
