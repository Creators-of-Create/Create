package com.simibubi.create.content.kinetics.waterwheel;

import java.util.function.Consumer;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelRenderer.Variant;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

public class WaterWheelVisual<T extends WaterWheelBlockEntity> extends KineticBlockEntityVisual<T> {
	private static final RendererReloadCache<ModelKey, Model> MODEL_CACHE = new RendererReloadCache<>(WaterWheelVisual::createModel);

	protected final boolean large;
	protected BlockState lastMaterial;
	protected RotatingInstance rotatingModel;

	public WaterWheelVisual(VisualizationContext context, T blockEntity, boolean large, float partialTick) {
		super(context, blockEntity, partialTick);
		this.large = large;

		setupInstance();
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelVisual<T> standard(VisualizationContext context, T blockEntity, float partialTick) {
		return new WaterWheelVisual<>(context, blockEntity, false, partialTick);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelVisual<T> large(VisualizationContext context, T blockEntity, float partialTick) {
		return new WaterWheelVisual<>(context, blockEntity, true, partialTick);
	}

	private void setupInstance() {
		lastMaterial = blockEntity.material;
		rotatingModel = instancerProvider().instancer(AllInstanceTypes.ROTATING, MODEL_CACHE.get(new ModelKey(Variant.of(large, blockState), blockEntity.material)))
				.createInstance();
		rotatingModel.setup(blockEntity)
			.setPosition(getVisualPosition())
			.rotateToFace(rotationAxis())
			.setChanged();
	}

	@Override
	public void update(float pt) {
		if (lastMaterial != blockEntity.material) {
			rotatingModel.delete();
			setupInstance();
		} else {
			rotatingModel.setup(blockEntity)
				.setChanged();
		}
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

	private static Model createModel(ModelKey key) {
		BakedModel model = WaterWheelRenderer.generateModel(key.variant(), key.material());
		return new BakedModelBuilder(model)
				.build();
	}

	public record ModelKey(WaterWheelRenderer.Variant variant, BlockState material) {

	}
}
