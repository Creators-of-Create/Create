package com.simibubi.create.foundation.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Restores some features which where "broken" in 1.17
 */
@Mixin(value = Shapes.class, remap = false)
public abstract class ShapesMixin {

	@Shadow
	protected static int findBits(double pMinBits, double pMaxBits) {
		return 0;
	}

	@Shadow
	public static VoxelShape block() {
		return null;
	}

	@Shadow
	@Final
	private static VoxelShape BLOCK;
	private static final Logger LOGGER = LogManager.getLogger();

	@Inject(method = "box", at = @At("HEAD"), cancellable = true)
	private static void allowSpecialShapes(double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ, CallbackInfoReturnable<VoxelShape> cir) {
		cir.setReturnValue(Shapes.create(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ));
	}

	@Inject(method = "create(DDDDDD)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
	private static void allowLargerBoxes(double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ, CallbackInfoReturnable<VoxelShape> cir) {
		int i = findBits(pMinX, pMaxX);
		int j = findBits(pMinY, pMaxY);
		int k = findBits(pMinZ, pMaxZ);
		if (i >= 0 && j >= 0 && k >= 0) {
			if (i == 0 && j == 0 && k == 0) {
				cir.setReturnValue(block());
			} else {
				int l = 1 << i;
				int i1 = 1 << j;
				int j1 = 1 << k;
				BitSetDiscreteVoxelShape bitsetdiscretevoxelshape = BitSetDiscreteVoxelShape.withFilledBounds(l, i1, j1, (int)Math.round(pMinX * (double)l), (int)Math.round(pMinY * (double)i1), (int)Math.round(pMinZ * (double)j1), (int)Math.round(pMaxX * (double)l), (int)Math.round(pMaxY * (double)i1), (int)Math.round(pMaxZ * (double)j1));
				cir.setReturnValue(new CubeVoxelShape(bitsetdiscretevoxelshape));
			}
		} else {
			cir.setReturnValue(new ArrayVoxelShape(BLOCK.shape, DoubleArrayList.wrap(new double[]{pMinX, pMaxX}), DoubleArrayList.wrap(new double[]{pMinY, pMaxY}), DoubleArrayList.wrap(new double[]{pMinZ, pMaxZ})));
		}
	}

	@Inject(method = "empty", at = @At("RETURN"), cancellable = true)
	private static void nullSafety(CallbackInfoReturnable<VoxelShape> cir) {
		if(cir.getReturnValue() == null) {
			LOGGER.warn("Shapes.EMPTY was null. Creating new empty shape");
			cir.setReturnValue(new ArrayVoxelShape(new BitSetDiscreteVoxelShape(0, 0, 0), new DoubleArrayList(new double[]{0.0D}), new DoubleArrayList(new double[]{0.0D}), new DoubleArrayList(new double[]{0.0D})));
		}
	}
}
