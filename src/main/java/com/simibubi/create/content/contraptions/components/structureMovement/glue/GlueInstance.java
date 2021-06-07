package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.jozufozu.flywheel.backend.instancing.ITickableInstance;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.backend.model.IndexedModel;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.instancing.ConditionalInstance;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.simibubi.create.AllItems;
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
		Instancer<OrientedData> instancer = renderer.getMaterial(Materials.ORIENTED)
				.get(entity.getType(), GlueInstance::supplyModel);

		Direction face = entity.getFacingDirection();
		rotation = new Quaternion(AngleHelper.verticalAngle(face), AngleHelper.horizontalAngleNew(face), 0, true);

		model = new ConditionalInstance<>(instancer)
				.withCondition(this::shouldShow)
				.withSetupFunc(this::positionModel)
				.update();
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

		ByteBuffer buffer = ByteBuffer.allocate(Formats.UNLIT_MODEL.getStride() * 8);
		buffer.order(ByteOrder.nativeOrder());

		//         x,            y,            z,nx, ny,nz, u, v
		// inside quad
		buffer.putFloat((float) a1.x).putFloat((float) a1.y).putFloat((float) a1.z).put((byte) 0).put((byte) 127).put((byte) 0).putFloat(1f).putFloat(0f);
		buffer.putFloat((float) a2.x).putFloat((float) a2.y).putFloat((float) a2.z).put((byte) 0).put((byte) 127).put((byte) 0).putFloat(1f).putFloat(1f);
		buffer.putFloat((float) a3.x).putFloat((float) a3.y).putFloat((float) a3.z).put((byte) 0).put((byte) 127).put((byte) 0).putFloat(0f).putFloat(1f);
		buffer.putFloat((float) a4.x).putFloat((float) a4.y).putFloat((float) a4.z).put((byte) 0).put((byte) 127).put((byte) 0).putFloat(0f).putFloat(0f);
		// outside quad
		buffer.putFloat((float) b4.x).putFloat((float) b4.y).putFloat((float) b4.z).put((byte) 0).put((byte) -127).put((byte) 0).putFloat(0f).putFloat(0f);
		buffer.putFloat((float) b3.x).putFloat((float) b3.y).putFloat((float) b3.z).put((byte) 0).put((byte) -127).put((byte) 0).putFloat(0f).putFloat(1f);
		buffer.putFloat((float) b2.x).putFloat((float) b2.y).putFloat((float) b2.z).put((byte) 0).put((byte) -127).put((byte) 0).putFloat(1f).putFloat(1f);
		buffer.putFloat((float) b1.x).putFloat((float) b1.y).putFloat((float) b1.z).put((byte) 0).put((byte) -127).put((byte) 0).putFloat(1f).putFloat(0f);

		buffer.rewind();


		return IndexedModel.fromSequentialQuads(Formats.UNLIT_MODEL, buffer, 8);
	}
}
