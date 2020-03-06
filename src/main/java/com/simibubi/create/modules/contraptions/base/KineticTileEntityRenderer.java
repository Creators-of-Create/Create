package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.SafeTileEntityRendererFast;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.SuperByteBufferCache.Compartment;
import com.simibubi.create.modules.contraptions.KineticDebugger;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class KineticTileEntityRenderer extends SafeTileEntityRendererFast<KineticTileEntity> {

	public static final Compartment<BlockState> KINETIC_TILE = new Compartment<>();
	public static boolean rainbowMode = false;

	@Override
	public void renderFast(KineticTileEntity te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer) {
		renderRotatingBuffer(te, getWorld(), getRotatedModel(te), x, y, z, buffer);
	}

	public static void renderRotatingKineticBlock(KineticTileEntity te, World world, BlockState renderedState, double x,
			double y, double z, BufferBuilder buffer) {
		SuperByteBuffer superByteBuffer = CreateClient.bufferCache.renderBlockIn(KINETIC_TILE, renderedState);
		renderRotatingBuffer(te, world, superByteBuffer, x, y, z, buffer);
	}

	public static void renderRotatingBuffer(KineticTileEntity te, World world, SuperByteBuffer superBuffer, double x,
			double y, double z, BufferBuilder buffer) {
		buffer.putBulkData(standardKineticRotationTransform(superBuffer, te, world).translate(x, y, z).build());
	}

	public static float getAngleForTe(KineticTileEntity te, final BlockPos pos, Axis axis) {
		float time = AnimationTickHolder.getRenderTick();
		float offset = getRotationOffsetForPosition(te, pos, axis);
		float angle = ((time * te.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;
		return angle;
	}

	public static SuperByteBuffer standardKineticRotationTransform(SuperByteBuffer buffer, KineticTileEntity te,
			World world) {
		final BlockPos pos = te.getPos();
		Axis axis = ((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState());
		return kineticRotationTransform(buffer, te, axis, getAngleForTe(te, pos, axis), world);
	}

	public static SuperByteBuffer kineticRotationTransform(SuperByteBuffer buffer, KineticTileEntity te, Axis axis,
			float angle, World world) {
		int packedLightmapCoords = te.getBlockState().getPackedLightmapCoords(world, te.getPos());
		buffer.light(packedLightmapCoords);
		buffer.rotateCentered(axis, angle);

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
		float offset = AllBlocks.LARGE_COGWHEEL.typeOf(te.getBlockState()) ? 11.25f : 0;
		double d = (((axis == Axis.X) ? 0 : pos.getX()) + ((axis == Axis.Y) ? 0 : pos.getY())
				+ ((axis == Axis.Z) ? 0 : pos.getZ())) % 2;
		if (d == 0) {
			offset = 22.5f;
		}
		return offset;
	}

	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return te.getBlockState();
	}

	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return CreateClient.bufferCache.renderBlockIn(KINETIC_TILE, getRenderedBlockState(te));
	}

}
