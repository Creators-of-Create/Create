package com.simibubi.create.modules.contraptions.relays.belt;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.IndependentShadowRenderer;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer.BlockModelSpinner;
import com.simibubi.create.modules.contraptions.relays.belt.BeltInventory.TransportedItemStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@SuppressWarnings("deprecation")
public class BeltTileEntityRenderer extends TileEntityRenderer<BeltTileEntity> {

	@Override
	public void render(BeltTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.render(te, x, y, z, partialTicks, destroyStage);

		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, Tessellator.getInstance().getBuffer());
		TessellatorHelper.draw();

		if (te.isController()) {
			GlStateManager.pushMatrix();

			Vec3i directionVec = te.getBeltFacing().getDirectionVec();
			Vec3d beltStartOffset = new Vec3d(directionVec).scale(-.5).add(.5, 13 / 16f + .125f, .5);
			GlStateManager.translated(x + beltStartOffset.x, y + beltStartOffset.y, z + beltStartOffset.z);

			for (TransportedItemStack transported : te.getInventory().items) {
				GlStateManager.pushMatrix();
				float offset = MathHelper.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);

				float sideOffset = MathHelper.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);
				Vec3d offsetVec = new Vec3d(directionVec).scale(offset);
				GlStateManager.translated(offsetVec.x, offsetVec.y, offsetVec.z);

				boolean alongX = te.getBeltFacing().rotateY().getAxis() == Axis.X;
				if (!alongX)
					sideOffset *= -1;
				GlStateManager.translated(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

				ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
				boolean blockItem = itemRenderer.getModelWithOverrides(transported.stack).isGui3d();
				if (Minecraft.getInstance().gameSettings.fancyGraphics) {
					Vec3d shadowPos = new Vec3d(te.getPos()).add(beltStartOffset.scale(1).add(offsetVec)
							.add(alongX ? sideOffset : 0, .39, alongX ? 0 : sideOffset));
					IndependentShadowRenderer.renderShadow(shadowPos.x, shadowPos.y, shadowPos.z, .75f,
							blockItem ? .2f : .2f);
				}

				RenderHelper.enableStandardItemLighting();

				int count = (int) (MathHelper.log2((int) (transported.stack.getCount()))) / 2;
				for (int i = 0; i <= count; i++) {
					GlStateManager.pushMatrix();

					GlStateManager.rotated(transported.angle, 0, 1, 0);
					if (!blockItem) {
						GlStateManager.translated(0, -.09375, 0);
						GlStateManager.rotated(90, 1, 0, 0);
					}

					GlStateManager.scaled(.5, .5, .5);
					itemRenderer.renderItem(transported.stack, TransformType.FIXED);
					GlStateManager.popMatrix();
					GlStateManager.rotated(10, 0, 1, 0);
					GlStateManager.translated(0, 1/16d, 0);
				}

				RenderHelper.disableStandardItemLighting();
				GlStateManager.popMatrix();
			}

			GlStateManager.popMatrix();
		}
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
