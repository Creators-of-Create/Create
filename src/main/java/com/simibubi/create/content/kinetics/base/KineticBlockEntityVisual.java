package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

public abstract class KineticBlockEntityVisual<T extends KineticBlockEntity> extends AbstractBlockEntityVisual<T> {

	public KineticBlockEntityVisual(VisualizationContext context, T blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
	}

	protected Direction.Axis rotationAxis() {
		return rotationAxis(blockState);
	}

	public static float rotationOffset(BlockState state, Axis axis, Vec3i pos) {
		if (shouldOffset(axis, pos)) {
			return 22.5f;
		} else {
			return ICogWheel.isLargeCog(state) ? 11.25f : 0;
		}
	}

	public static boolean shouldOffset(Axis axis, Vec3i pos) {
		// Sum the components of the other 2 axes.
		int x = (axis == Axis.X) ? 0 : pos.getX();
		int y = (axis == Axis.Y) ? 0 : pos.getY();
		int z = (axis == Axis.Z) ? 0 : pos.getZ();
		return ((x + y + z) % 2) == 0;
	}

	public static Axis rotationAxis(BlockState blockState) {
		return (blockState.getBlock() instanceof IRotate irotate) ? irotate.getRotationAxis(blockState) : Axis.Y;
	}

	public static void applyOverstressEffect(KineticBlockEntity be, RotatingInstance... instances) {
		float overStressedEffect = be.effects.overStressedEffect;
		if (overStressedEffect != 0) {
			boolean overstressed = overStressedEffect > 0;
			Color color = overstressed ? Color.RED : Color.SPRING_GREEN;
			float weight = overstressed ? overStressedEffect : -overStressedEffect;

			for (RotatingInstance instance : instances)
				instance.setColor(Color.WHITE.mixWith(color, weight));
		} else {
			for (RotatingInstance instance : instances)
				instance.setColor(Color.WHITE);
		}

		for (RotatingInstance instance : instances)
			instance.setChanged();
	}
}
