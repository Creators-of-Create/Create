package com.simibubi.create.modules.contraptions.components.crafter;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("deprecation")
public class MechanicalCrafterTileEntityRenderer extends TileEntityRenderer<MechanicalCrafterTileEntity> {

	@Override
	public void render(MechanicalCrafterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage) {
		super.render(te, x, y, z, partialTicks, destroyStage);
		if (!AllBlocks.MECHANICAL_CRAFTER.typeOf(te.getBlockState()))
			return;

		GlStateManager.pushMatrix();
		Direction facing = te.getBlockState().get(MechanicalCrafterBlock.HORIZONTAL_FACING);
		Vec3d vec = new Vec3d(facing.getDirectionVec()).scale(.58).add(.5, .5, .5);
		GlStateManager.translated(x + vec.x, y + vec.y, z + vec.z);
		GlStateManager.scalef(1 / 2f, 1 / 2f, 1 / 2f);
		float yRot = AngleHelper.horizontalAngle(facing);
		GlStateManager.rotated(yRot, 0, 1, 0);
		renderItems(te);
		GlStateManager.popMatrix();

		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, Tessellator.getInstance().getBuffer());
		TessellatorHelper.draw();
	}

	public void renderItems(MechanicalCrafterTileEntity te) {
		RenderHelper.enableStandardItemLighting();

		ItemStack stack = te.inventory.getStackInSlot(0);
		if (!stack.isEmpty())
			Minecraft.getInstance().getItemRenderer().renderItem(stack, TransformType.FIXED);

		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public void renderTileEntityFast(MechanicalCrafterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockState blockState = te.getBlockState();
		BlockState renderedState = AllBlocks.SHAFTLESS_COGWHEEL.get().getDefaultState().with(BlockStateProperties.AXIS,
				blockState.get(MechanicalCrafterBlock.HORIZONTAL_FACING).getAxis());
		KineticTileEntityRenderer.renderRotatingKineticBlock(te, getWorld(), renderedState, x, y, z, buffer);

		Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(blockState);
		BlockPos pos = te.getPos();

//		SuperByteBuffer lidBuffer = renderAndTransform(AllBlocks.MECHANICAL_CRAFTER_LID, blockState, pos);
//		lidBuffer.translate(x, y, z).renderInto(buffer);

		if (MechanicalCrafterBlock.isValidTarget(getWorld(), pos.offset(targetDirection), blockState)) {
			SuperByteBuffer beltBuffer = renderAndTransform(AllBlocks.MECHANICAL_CRAFTER_BELT, blockState, pos);
			SuperByteBuffer beltFrameBuffer = renderAndTransform(AllBlocks.MECHANICAL_CRAFTER_BELT_FRAME, blockState,
					pos);
			beltBuffer.translate(x, y, z).renderInto(buffer);
			beltFrameBuffer.translate(x, y, z).renderInto(buffer);

		} else {
			SuperByteBuffer arrowBuffer = renderAndTransform(AllBlocks.MECHANICAL_CRAFTER_ARROW, blockState, pos);
			arrowBuffer.translate(x, y, z).renderInto(buffer);
		}

	}

	private SuperByteBuffer renderAndTransform(AllBlocks renderBlock, BlockState crafterState, BlockPos pos) {
		SuperByteBuffer buffer = CreateClient.bufferCache.renderGenericBlockModel(renderBlock.getDefault());
		float xRot = crafterState.get(MechanicalCrafterBlock.POINTING).getXRotation();
		float yRot = AngleHelper.horizontalAngle(crafterState.get(MechanicalCrafterBlock.HORIZONTAL_FACING));
		buffer.rotateCentered(Axis.X, (float) ((xRot) / 180 * Math.PI));
		buffer.rotateCentered(Axis.Y, (float) ((yRot + 90) / 180 * Math.PI));
		buffer.light(crafterState.getPackedLightmapCoords(getWorld(), pos));
		return buffer;
	}

}
