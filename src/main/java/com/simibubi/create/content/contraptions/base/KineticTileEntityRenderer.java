package com.simibubi.create.content.contraptions.base;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.foundation.render.Compartment;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.instancing.InstanceContext;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
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
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
//		for (RenderType type : RenderType.getBlockLayers())
//			if (RenderTypeLookup.canRenderInLayer(te.getBlockState(), type))
//				renderRotatingBuffer(te, getRotatedModel(te));

//		addInstanceData(new InstanceContext.World<>(te));
	}

	public void addInstanceData(InstanceContext<KineticTileEntity> ctx) {
		renderRotatingBuffer(ctx, getRotatedModel(ctx));
	}

	public void markForRebuild(InstanceContext<KineticTileEntity> ctx) {
		getRotatedModel(ctx).clearInstanceData();
	}

	public static <T extends KineticTileEntity> void renderRotatingKineticBlock(InstanceContext<T> ctx, BlockState renderedState) {
		InstancedModel<RotatingData> instancedRenderer = ctx.getRotating().getModel(KINETIC_TILE, renderedState);
		renderRotatingBuffer(ctx, instancedRenderer);
	}

	public static <T extends KineticTileEntity> void markForRebuild(InstanceContext<T> ctx, BlockState renderedState) {
		ctx.getRotating().getModel(KINETIC_TILE, renderedState).clearInstanceData();
	}

	public static <T extends KineticTileEntity> void renderRotatingBuffer(InstanceContext<T> ctx, InstancedModel<RotatingData> instancer) {
		instancer.setupInstance(data -> {
			T te = ctx.te;
			final BlockPos pos = te.getPos();
			Axis axis = ((IRotate) te.getBlockState()
									 .getBlock()).getRotationAxis(te.getBlockState());

			data.setRotationalSpeed(te.getSpeed())
				.setRotationOffset(getRotationOffsetForPosition(te, pos, axis))
				.setRotationAxis(Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis).getUnitVector())
				.setTileEntity(te);

			if (ctx.checkWorldLight()) {
				data.setBlockLight(te.getWorld().getLightLevel(LightType.BLOCK, te.getPos()))
					.setSkyLight(te.getWorld().getLightLevel(LightType.SKY, te.getPos()));
			}
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

	protected InstancedModel<RotatingData> getRotatedModel(InstanceContext<? extends KineticTileEntity> ctx) {
		return ctx.getRotating().getModel(KINETIC_TILE, getRenderedBlockState(ctx.te));
	}

}
