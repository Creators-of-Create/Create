package com.simibubi.create.modules.contraptions.relays.belt;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.SafeTileEntityRenderer;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.IndependentShadowRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock.Slope;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@SuppressWarnings("deprecation")
public class BeltTileEntityRenderer extends SafeTileEntityRenderer<BeltTileEntity> {

	private static SpriteShiftEntry animatedTexture;

	public BeltTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}
	
	@Override
	protected void renderSafe(BeltTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {

		BlockState blockState = te.getBlockState();
		if (!AllBlocks.BELT.typeOf(blockState))
			return;

		BlockState renderedState = getBeltState(te);
		SuperByteBuffer beltBuffer =
			CreateClient.bufferCache.renderBlockIn(KineticTileEntityRenderer.KINETIC_TILE, renderedState);

		beltBuffer.color(te.color == -1 ? 0x808080 : te.color);

		// UV shift
		float speed = te.getSpeed();
		if (animatedTexture == null)
			animatedTexture = SpriteShifter.get("block/belt", "block/belt_animated");
		if (speed != 0) {
			float time =
				AnimationTickHolder.getRenderTick() * blockState.get(HORIZONTAL_FACING).getAxisDirection().getOffset();
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
		
		IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());

		int packedLightmapCoords = WorldRenderer.getLightmapCoordinates(te.getWorld(), blockState, te.getPos());
		beltBuffer.light(packedLightmapCoords).renderInto(ms, vb);

		if (te.hasPulley()) {
			SuperByteBuffer superBuffer = AllBlockPartials.BELT_PULLEY.renderOn(blockState);
			Axis axis = blockState.get(BeltBlock.HORIZONTAL_FACING).getAxis();
			superBuffer.rotateCentered(Axis.X, (float) (Math.PI / 2));
			superBuffer.rotateCentered(Axis.Y, (float) (axis == Axis.X ? 0 : Math.PI / 2));
			KineticTileEntityRenderer.standardKineticRotationTransform(superBuffer, te)
					.renderInto(ms, vb);
		}
		
		renderItems(te, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderItems(BeltTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		if (!te.isController())
			return;
		if (te.beltLength == 0)
			return;
		
		ms.push();

		Vec3i directionVec = te.getBeltFacing().getDirectionVec();
		Vec3d beltStartOffset = new Vec3d(directionVec).scale(-.5).add(.5, 13 / 16f + .125f, .5);
		ms.translate(beltStartOffset.x, beltStartOffset.y, beltStartOffset.z);
		Slope slope = te.getBlockState().get(BeltBlock.SLOPE);
		int verticality = slope == Slope.DOWNWARD ? -1 : slope == Slope.UPWARD ? 1 : 0;
		boolean slopeAlongX = te.getBeltFacing().getAxis() == Axis.X;

		for (TransportedItemStack transported : te.getInventory().items) {
			ms.push();
			TessellatorHelper.fightZFighting(transported.angle, ms);
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
			boolean onSlope = slope != Slope.HORIZONTAL && MathHelper.clamp(offset, .5f, te.beltLength - .5f) == offset;
			boolean tiltForward =
				(slope == Slope.DOWNWARD ^ te.getBeltFacing().getAxisDirection() == AxisDirection.POSITIVE) == (te
						.getBeltFacing().getAxis() == Axis.Z);
			float slopeAngle = onSlope ? tiltForward ? -45 : 45 : 0;

			ms.translate(offsetVec.x, offsetVec.y, offsetVec.z);

			boolean alongX = te.getBeltFacing().rotateY().getAxis() == Axis.X;
			if (!alongX)
				sideOffset *= -1;
			ms.translate(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

			ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
			boolean blockItem = itemRenderer.getItemModelWithOverrides(transported.stack, te.getWorld(), null).isGui3d();
			if (Minecraft.getInstance().gameSettings.fancyGraphics) {
				Vec3d shadowPos = new Vec3d(te.getPos()).add(beltStartOffset.scale(1).add(offsetVec)
						.add(alongX ? sideOffset : 0, .39, alongX ? 0 : sideOffset));
				IndependentShadowRenderer.renderShadow(ms, buffer, shadowPos, .75f, blockItem ? .2f : .2f);
			}

			RenderHelper.enableStandardItemLighting();

			int count = (int) (MathHelper.log2((int) (transported.stack.getCount()))) / 2;
			ms.multiply(new Vector3f(slopeAlongX ? 0 : 1, 0, slopeAlongX ? 1 : 0).getDegreesQuaternion(slopeAngle));
			if (onSlope)
				ms.translate(0, 1 / 8f, 0);
			Random r = new Random(transported.angle);

			for (int i = 0; i <= count; i++) {
				ms.push();

				ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(transported.angle));
				if (!blockItem) {
					ms.translate(0, -.09375, 0);
					ms.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
				}

				if (blockItem) {
					ms.translate(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
				}

				ms.scale(.5f, .5f, .5f);
				itemRenderer.renderItem(transported.stack, TransformType.FIXED, light, overlay, ms, buffer);
				ms.pop();

				if (!blockItem)
					ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(10));
				ms.translate(0, blockItem ? 1 / 64d : 1 / 16d, 0);

			}

			ms.pop();
		}
		ms.pop();
	}

	protected BlockState getBeltState(KineticTileEntity te) {
		return te.getBlockState().with(BeltBlock.CASING, false);
	}

}
