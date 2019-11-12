package com.simibubi.create.modules.contraptions.relays.belt;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer.BlockModelSpinner;
import com.simibubi.create.modules.contraptions.relays.belt.BeltInventory.TransportedItemStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@SuppressWarnings("deprecation")
public class BeltTileEntityRenderer extends TileEntityRenderer<BeltTileEntity> {

	@Override
	public void render(BeltTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.render(te, x, y, z, partialTicks, destroyStage);
		if (te.isController()) {
			GlStateManager.pushMatrix();
			GlStateManager.translated(x + .5, y + 13 / 16f + .25, z + .5);

			for (TransportedItemStack transported : te.getInventory().items) {
				GlStateManager.pushMatrix();
				Vec3i direction = te.getBeltChainDirection();
				float offset = transported.beltPosition;
				Vec3d offsetVec = new Vec3d(direction).scale(offset);
				GlStateManager.translated(offsetVec.x, offsetVec.y, offsetVec.z);
				Minecraft.getInstance().getItemRenderer().renderItem(transported.stack, TransformType.FIXED);
				GlStateManager.popMatrix();
			}

			GlStateManager.popMatrix();
		}

		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, Tessellator.getInstance().getBuffer());
		TessellatorHelper.draw();
	}

	@Override
	public void renderTileEntityFast(BeltTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {

		if (te.hasPulley()) {
			final BlockState state = getRenderedBlockState(te);
			KineticTileEntityRenderer.cacheIfMissing(state, getWorld(), BlockModelSpinner::new);
			final BlockPos pos = te.getPos();
			Axis axis = ((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState());
			float angle = KineticTileEntityRenderer.getAngleForTe(te, pos, axis);
			KineticTileEntityRenderer.renderFromCache(buffer, state, getWorld(), (float) x, (float) y, (float) z, pos,
					axis, angle);
		}

		KineticTileEntityRenderer.cacheIfMissing(te.getBlockState(), getWorld(), BeltModelAnimator::new);
		renderBeltFromCache(te, (float) x, (float) y, (float) z, buffer);
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.BELT_PULLEY.get().getDefaultState().with(BlockStateProperties.AXIS,
				((IRotate) AllBlocks.BELT.get()).getRotationAxis(te.getBlockState()));
	}

	public void renderBeltFromCache(BeltTileEntity te, float x, float y, float z, BufferBuilder buffer) {
		buffer.putBulkData(((BeltModelAnimator) KineticTileEntityRenderer.cachedBuffers.get(te.getBlockState()))
				.getTransformed(te, x, y, z, te.color));
	}
}
