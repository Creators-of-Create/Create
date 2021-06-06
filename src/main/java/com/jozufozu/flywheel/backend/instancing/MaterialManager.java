package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.jozufozu.flywheel.core.shader.IProgramCallback;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.WeakHashSet;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3i;

public class MaterialManager<P extends WorldProgram> {

	public static int MAX_ORIGIN_DISTANCE = 100;

	protected final ArrayList<MaterialRenderer<P>> renderers;
	protected final Map<ResourceLocation, InstanceMaterial<?>> materials;

	private BlockPos originCoordinate = BlockPos.ZERO;

	private final WeakHashSet<OriginShiftListener> listeners;

	public MaterialManager(WorldContext<P> context) {
		this.materials = new HashMap<>();
		this.renderers = new ArrayList<>(Backend.getInstance().allMaterials().size());
		this.listeners = new WeakHashSet<>();

		for (MaterialSpec<?> spec : Backend.getInstance().allMaterials()) {
			InstanceMaterial<?> material = new InstanceMaterial<>(this::getOriginCoordinate, spec);
			materials.put(spec.name, material);
			MaterialRenderer<P> renderer = new MaterialRenderer<>(context.getProgram(spec.getProgramName()), material);
			renderers.add(renderer);
		}

	}

	/**
	 * Render every model for every material.
	 *
	 * @param layer          Which vanilla {@link RenderType} is being drawn?
	 * @param viewProjection How do we get from camera space to clip space?
	 */
	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ) {
		render(layer, viewProjection, camX, camY, camZ, null);
	}

	/**
	 * Render every model for every material.
	 *
	 * @param layer          Which vanilla {@link RenderType} is being drawn?
	 * @param viewProjection How do we get from camera space to clip space?
	 * @param callback       Provide additional uniforms or state here.
	 */
	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, IProgramCallback<P> callback) {
		camX -= originCoordinate.getX();
		camY -= originCoordinate.getY();
		camZ -= originCoordinate.getZ();

		Matrix4f translate = Matrix4f.translate((float) -camX, (float) -camY, (float) -camZ);

		translate.multiplyBackward(viewProjection);

		for (MaterialRenderer<P> material : renderers) {
			material.render(layer, translate, camX, camY, camZ, callback);
		}
	}

	public void delete() {
		for (InstanceMaterial<?> material : materials.values()) {
			material.delete();
		}
	}

	@SuppressWarnings("unchecked")
	public <D extends InstanceData> InstanceMaterial<D> getMaterial(MaterialSpec<D> materialType) {
		return (InstanceMaterial<D>) materials.get(materialType.name);
	}

	public InstanceMaterial<ModelData> getTransformMaterial() {
		return getMaterial(Materials.TRANSFORMED);
	}

	public InstanceMaterial<OrientedData> getOrientedMaterial() {
		return getMaterial(Materials.ORIENTED);
	}

	public Vector3i getOriginCoordinate() {
		return originCoordinate;
	}

	public void onOriginShift(OriginShiftListener listener) {
		listeners.add(listener);
	}

	public void checkAndShiftOrigin(ActiveRenderInfo info) {
		int cX = MathHelper.floor(info.getProjectedView().x);
		int cY = MathHelper.floor(info.getProjectedView().y);
		int cZ = MathHelper.floor(info.getProjectedView().z);

		int dX = cX - originCoordinate.getX();
		int dY = cY - originCoordinate.getY();
		int dZ = cZ - originCoordinate.getZ();

		if (Math.abs(dX) > MAX_ORIGIN_DISTANCE || Math.abs(dY) > MAX_ORIGIN_DISTANCE || Math.abs(dZ) > MAX_ORIGIN_DISTANCE) {

			originCoordinate = new BlockPos(cX, cY, cZ);

			materials.values().forEach(InstanceMaterial::clear);
			listeners.forEach(OriginShiftListener::onOriginShift);
		}
	}

	@FunctionalInterface
	public interface OriginShiftListener {
		void onOriginShift();
	}
}
