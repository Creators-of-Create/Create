package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.backend.instancing.ITickableInstance;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.backend.model.IndexedModel;
import com.jozufozu.flywheel.core.instancing.ConditionalInstance;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.render.AllMaterialSpecs;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

public class GlueInstance extends EntityInstance<SuperGlueEntity> implements ITickableInstance {

	private final Quaternion rotation;
	protected ConditionalInstance<OrientedData> model;

	public GlueInstance(MaterialManager<?> renderer, SuperGlueEntity entity) {
		super(renderer, entity);

		Instancer<OrientedData> instancer = renderer.getMaterial(AllMaterialSpecs.ORIENTED)
				.get(entity.getType(), GlueInstance::supplyModel);
		model = new ConditionalInstance<>(instancer)
				.withCondition(this::shouldShow)
				.withSetupFunc(this::positionModel)
				.update();

		Direction face = entity.getFacingDirection();

		rotation = new Quaternion(AngleHelper.verticalAngle(face), AngleHelper.horizontalAngleNew(face), 0, true);
	}

	@Override
	public void tick() {
		model.update();
	}

	@Override
	public void remove() {
		model.delete();
	}

	private void positionModel(OrientedData model) {

		model.setPosition(getInstancePosition())
				.setRotation(rotation)
				.setSkyLight(15)
				.setBlockLight(15);
	}

	private boolean shouldShow() {
		PlayerEntity player = Minecraft.getInstance().player;

		return entity.isVisible()
				|| AllItems.SUPER_GLUE.isIn(player.getHeldItemMainhand())
				|| AllItems.SUPER_GLUE.isIn(player.getHeldItemOffhand());
	}

	public static BufferedModel supplyModel() {
		Vector3d diff = Vector3d.of(Direction.SOUTH.getDirectionVec());
		Vector3d extension = diff.normalize()
				.scale(1 / 32f - 1 / 128f);

		Vector3d plane = VecHelper.axisAlingedPlaneOf(diff);
		Direction.Axis axis = Direction.getFacingFromVector(diff.x, diff.y, diff.z)
				.getAxis();

		Vector3d start = Vector3d.ZERO.subtract(extension);
		Vector3d end = Vector3d.ZERO.add(extension);

		plane = plane.scale(1 / 2f);
		Vector3d a1 = plane.add(start);
		Vector3d b1 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vector3d a2 = plane.add(start);
		Vector3d b2 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vector3d a3 = plane.add(start);
		Vector3d b3 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vector3d a4 = plane.add(start);
		Vector3d b4 = plane.add(end);

		float[] quads = new float[] {
				//         x,            y,            z,nx, ny,nz, u, v
				// inside quad
				(float) a1.x, (float) a1.y, (float) a1.z, 0, -1, 0, 1, 0,
				(float) a2.x, (float) a2.y, (float) a2.z, 0, -1, 0, 1, 1,
				(float) a3.x, (float) a3.y, (float) a3.z, 0, -1, 0, 0, 1,
				(float) a4.x, (float) a4.y, (float) a4.z, 0, -1, 0, 0, 0,
				// outside quad
				(float) b4.x, (float) b4.y, (float) b4.z, 0, 1, 0, 0, 0,
				(float) b3.x, (float) b3.y, (float) b3.z, 0, 1, 0, 0, 1,
				(float) b2.x, (float) b2.y, (float) b2.z, 0, 1, 0, 1, 1,
				(float) b1.x, (float) b1.y, (float) b1.z, 0, 1, 0, 1, 0,
		};

		ByteBuffer buffer = ByteBuffer.allocate(quads.length * 4);
		buffer.asFloatBuffer().put(quads);

		return IndexedModel.fromSequentialQuads(AllMaterialSpecs.UNLIT_MODEL, buffer, 8);
	}
}
