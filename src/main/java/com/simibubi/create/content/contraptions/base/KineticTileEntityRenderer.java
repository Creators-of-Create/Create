package com.simibubi.create.content.contraptions.base;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.render.InstancedBuffer;
import com.simibubi.create.foundation.utility.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.render.SuperByteBufferCache.Compartment;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class KineticTileEntityRenderer extends SafeTileEntityRenderer<KineticTileEntity> {

	public static final Compartment<BlockState> KINETIC_TILE = new Compartment<>();
	public static boolean rainbowMode = false;

	public KineticTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public boolean isGlobalRenderer(KineticTileEntity te) {
		return true;
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		for (RenderType type : RenderType.getBlockLayers())
			if (RenderTypeLookup.canRenderInLayer(te.getBlockState(), type))
				renderRotatingBuffer(te, getRotatedModel(te), light);
	}

	public static void renderRotatingKineticBlock(KineticTileEntity te, BlockState renderedState, int light) {
		InstancedBuffer instancedRenderer = CreateClient.kineticRenderer.renderBlockInstanced(KINETIC_TILE, renderedState);
		renderRotatingBuffer(te, instancedRenderer, light);
	}

	public static void renderRotatingBuffer(KineticTileEntity te, InstancedBuffer instancer, int light) {
		instancer.setupInstance(data -> {
			final BlockPos pos = te.getPos();
			Axis axis = ((IRotate) te.getBlockState()
									 .getBlock()).getRotationAxis(te.getBlockState());

			data.setPackedLight(light)
				.setRotationalSpeed(te.getSpeed())
				.setRotationOffset(getRotationOffsetForPosition(te, pos, axis))
				.setRotationAxis(Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis).getUnitVector())
				.setPosition(pos);
		});
	}

	public static float getAngleForTe(KineticTileEntity te, final BlockPos pos, Axis axis) {
		float time = AnimationTickHolder.getRenderTick();
		float offset = getRotationOffsetForPosition(te, pos, axis);
		float angle = ((time * te.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
		return angle;
	}

	public static SuperByteBuffer standardKineticRotationTransform(SuperByteBuffer buffer, KineticTileEntity te,
		int light) {
		final BlockPos pos = te.getPos();
		Axis axis = ((IRotate) te.getBlockState()
			.getBlock()).getRotationAxis(te.getBlockState());
		return kineticRotationTransform(buffer, te, axis, getAngleForTe(te, pos, axis), light);
	}

	public static SuperByteBuffer kineticRotationTransform(SuperByteBuffer buffer, KineticTileEntity te, Axis axis,
		float angle, int light) {
		buffer.light(light);
		buffer.rotateCentered(Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis), angle);

		int white = 0xFFFFFF;
		if (KineticDebugger.isActive()) {
			rainbowMode = true;
			buffer.color(te.hasNetwork() ? ColorHelper.colorFromLong(te.network) : white);
		} else {
			float overStressedEffect = te.effects.overStressedEffect;
			if (overStressedEffect != 0)
				if (overStressedEffect > 0)
					buffer.color(ColorHelper.mixColors(white, 0xFF0000, overStressedEffect));
				else
					buffer.color(ColorHelper.mixColors(white, 0x00FFBB, -overStressedEffect));
			else
				buffer.color(white);
		}

		return buffer;
	}

	protected static float getRotationOffsetForPosition(KineticTileEntity te, final BlockPos pos, final Axis axis) {
		float offset = CogWheelBlock.isLargeCog(te.getBlockState()) ? 11.25f : 0;
		double d = (((axis == Axis.X) ? 0 : pos.getX()) + ((axis == Axis.Y) ? 0 : pos.getY())
			+ ((axis == Axis.Z) ? 0 : pos.getZ())) % 2;
		if (d == 0) {
			offset = 22.5f;
		}
		return offset;
	}

	public static BlockState shaft(Axis axis) {
		return AllBlocks.SHAFT.getDefaultState()
			.with(BlockStateProperties.AXIS, axis);
	}

	public static Axis getRotationAxisOf(KineticTileEntity te) {
		return ((IRotate) te.getBlockState()
			.getBlock()).getRotationAxis(te.getBlockState());
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return te.getBlockState();
	}

	protected InstancedBuffer getRotatedModel(KineticTileEntity te) {
		return CreateClient.kineticRenderer.renderBlockInstanced(KINETIC_TILE, getRenderedBlockState(te));
	}

}
