package com.simibubi.create.content.kinetics.base;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.equipment.armor.BacktankRenderer;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.Direction;

public class SingleAxisRotatingVisual<T extends KineticBlockEntity> extends KineticBlockEntityVisual<T> implements SimpleTickableVisual {
	public static boolean rainbowMode = false;

	protected final RotatingInstance rotatingModel;

	public SingleAxisRotatingVisual(VisualizationContext context, T blockEntity, float partialTick, Model model) {
		this(context, blockEntity, partialTick, Direction.UP, model);
	}

	/**
	 * @param from  The source model orientation to rotate away from.
	 * @param model The model to spin.
	 */
	public SingleAxisRotatingVisual(VisualizationContext context, T blockEntity, float partialTick, Direction from, Model model) {
		super(context, blockEntity, partialTick);
		rotatingModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, model)
			.createInstance()
			.rotateToFace(from, rotationAxis())
			.setup(blockEntity)
			.setPosition(getVisualPosition());

		rotatingModel.setChanged();
	}

	public static <T extends KineticBlockEntity> SimpleBlockEntityVisualizer.Factory<T> of(PartialModel partial) {
		return (context, blockEntity, partialTick) -> {
			return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, Models.partial(partial));
		};
	}

	/**
	 * For partial models whose source model is aligned with the Z axis instead of Y
	 */
	public static <T extends KineticBlockEntity> SimpleBlockEntityVisualizer.Factory<T> ofZ(PartialModel partial) {
		return (context, blockEntity, partialTick) -> {
			return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, Direction.SOUTH, Models.partial(partial));
		};
	}

	public static <T extends KineticBlockEntity> SingleAxisRotatingVisual<T> shaft(VisualizationContext context, T blockEntity, float partialTick) {
		return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, Models.partial(AllPartialModels.SHAFT));
	}

	public static <T extends KineticBlockEntity> SingleAxisRotatingVisual<T> backtank(VisualizationContext context, T blockEntity, float partialTick) {
		var model = Models.partial(BacktankRenderer.getShaftModel(blockEntity.getBlockState()));
		return new SingleAxisRotatingVisual<>(context, blockEntity, partialTick, model);
	}

	@Override
	public void update(float pt) {
		rotatingModel.setup(blockEntity)
			.setChanged();
	}

	@Override
	public void tick(Context context) {
		float overStressedEffect = blockEntity.effects.overStressedEffect;
		if (overStressedEffect != 0) {
			boolean overstressed = overStressedEffect > 0;
			Color color = overstressed ? Color.RED : Color.SPRING_GREEN;
			float weight = overstressed ? overStressedEffect : -overStressedEffect;

			rotatingModel.setColor(Color.WHITE.mixWith(color, weight));
		} else {
			rotatingModel.setColor(Color.WHITE);
		}

		rotatingModel.setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(rotatingModel);
	}

	@Override
	protected void _delete() {
		rotatingModel.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(rotatingModel);
	}
}
