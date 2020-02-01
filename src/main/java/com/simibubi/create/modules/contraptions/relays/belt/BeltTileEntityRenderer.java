package com.simibubi.create.modules.contraptions.relays.belt;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.IndependentShadowRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@SuppressWarnings("deprecation")
public class BeltTileEntityRenderer extends TileEntityRenderer<BeltTileEntity> {

	private static SpriteShiftEntry animatedTexture;

	@Override
	public void render(BeltTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.render(te, x, y, z, partialTicks, destroyStage);

		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, Tessellator.getInstance().getBuffer());
		TessellatorHelper.draw();

		renderItems(te, x, y, z, partialTicks);
	}

	@Override
	public void renderTileEntityFast(BeltTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		if (te.hasPulley())
			KineticTileEntityRenderer.renderRotatingKineticBlock(te, getWorld(), getPulleyState(te), x, y, z, buffer);

		BlockState renderedState = getBeltState(te);
		SuperByteBuffer beltBuffer = CreateClient.bufferCache.renderBlockState(KineticTileEntityRenderer.KINETIC_TILE,
				renderedState);

		beltBuffer.color(te.color == -1 ? 0x808080 : te.color);

		// UV shift
		float speed = te.getSpeed();
		if (animatedTexture == null)
			animatedTexture = SpriteShifter.get("block/belt", "block/belt_animated");
		if (speed != 0) {
			float time = AnimationTickHolder.getRenderTick()
					* te.getBlockState().get(HORIZONTAL_FACING).getAxisDirection().getOffset();
			if (renderedState.get(BeltBlock.HORIZONTAL_FACING).getAxis() == Axis.X)
				speed = -speed;
			int textureIndex = (int) ((speed * time / 36) % 16);
			if (textureIndex < 0)
				textureIndex += 16;

			beltBuffer.shiftUVtoSheet(animatedTexture.getOriginal(), animatedTexture.getTarget(),
					(textureIndex % 4) * 16, (textureIndex / 4) * 16);
		} else {
			beltBuffer.shiftUVtoSheet(animatedTexture.getOriginal(), animatedTexture.getTarget(), 0, 0);
		}

		int packedLightmapCoords = te.getBlockState().getPackedLightmapCoords(getWorld(), te.getPos());
		beltBuffer.light(packedLightmapCoords).translate(x, y, z).renderInto(buffer);
	}

	protected void renderItems(BeltTileEntity te, double x, double y, double z, float partialTicks) {
		if (te.isController()) {
			GlStateManager.pushMatrix();

			Vec3i directionVec = te.getBeltFacing().getDirectionVec();
			Vec3d beltStartOffset = new Vec3d(directionVec).scale(-.5).add(.5, 13 / 16f + .125f, .5);
			GlStateManager.translated(x + beltStartOffset.x, y + beltStartOffset.y, z + beltStartOffset.z);
			Slope slope = te.getBlockState().get(BeltBlock.SLOPE);
			int verticality = slope == Slope.DOWNWARD ? -1 : slope == Slope.UPWARD ? 1 : 0;
			boolean slopeAlongX = te.getBeltFacing().getAxis() == Axis.X;

			for (TransportedItemStack transported : te.getInventory().items) {
				GlStateManager.pushMatrix();
				TessellatorHelper.fightZFighting(transported.angle);
				float offset = MathHelper.lerp(partialTicks, transported.prevBeltPosition, transported.beltPosition);
				float sideOffset = MathHelper.lerp(partialTicks, transported.prevSideOffset, transported.sideOffset);
				float verticalMovement = verticality;

				if (te.getSpeed() == 0) {
					offset = transported.beltPosition;
					sideOffset = transported.sideOffset;
				}

				if (offset < .5)
					verticalMovement = 0;
				verticalMovement = verticalMovement * (Math.min(offset, te.beltLength - .5f) - .5f);
				Vec3d offsetVec = new Vec3d(directionVec).scale(offset).add(0, verticalMovement, 0);
				boolean onSlope = slope != Slope.HORIZONTAL
						&& MathHelper.clamp(offset, .5f, te.beltLength - .5f) == offset;
				float slopeAngle = onSlope
						? slope == Slope.DOWNWARD ^ te.getDirectionAwareBeltMovementSpeed() > 0
								^ te.getBeltMovementSpeed() < 0 ? -45 : 45
						: 0;

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
				GlStateManager.rotated(slopeAngle, slopeAlongX ? 0 : 1, 0, slopeAlongX ? 1 : 0);
				if (onSlope)
					GlStateManager.translated(0, 1 / 8f, 0);
				Random r = new Random(transported.angle);

				for (int i = 0; i <= count; i++) {
					GlStateManager.pushMatrix();

					GlStateManager.rotated(transported.angle, 0, 1, 0);
					if (!blockItem) {
						GlStateManager.translated(0, -.09375, 0);
						GlStateManager.rotated(90, 1, 0, 0);
					}

					if (blockItem) {
						GlStateManager.translated(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
					}

					GlStateManager.scaled(.5, .5, .5);
					itemRenderer.renderItem(transported.stack, TransformType.FIXED);
					GlStateManager.popMatrix();

					if (!blockItem)
						GlStateManager.rotated(10, 0, 1, 0);
					GlStateManager.translated(0, blockItem ? 1 / 64d : 1 / 16d, 0);

				}

				RenderHelper.disableStandardItemLighting();
				GlStateManager.popMatrix();
			}

			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}

	protected BlockState getPulleyState(KineticTileEntity te) {
		return AllBlocks.BELT_PULLEY.get().getDefaultState().with(BlockStateProperties.AXIS,
				((IRotate) AllBlocks.BELT.get()).getRotationAxis(te.getBlockState()));
	}

	protected BlockState getBeltState(KineticTileEntity te) {
		return te.getBlockState().with(BeltBlock.CASING, false);
	}

}
