package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.instancing.ConditionalInstance;
import com.jozufozu.flywheel.core.instancing.GroupInstance;
import com.jozufozu.flywheel.core.instancing.SelectInstance;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.jozufozu.flywheel.light.LightPacking;
import com.jozufozu.flywheel.light.LightVolume;
import com.jozufozu.flywheel.light.TickingLightListener;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;
import com.mojang.math.Vector3f;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;

public abstract class AbstractPulleyInstance<T extends KineticBlockEntity> extends ShaftInstance<T> implements DynamicInstance, TickingLightListener {

	final OrientedData coil;
	final SelectInstance<OrientedData> magnet;
	final GroupInstance<OrientedData> rope;
	final ConditionalInstance<OrientedData> halfRope;

	protected float offset;
	protected final Direction rotatingAbout;
	protected final Vector3f rotationAxis;

	private final GridAlignedBB volume = new GridAlignedBB();
	private final LightVolume light;

	public AbstractPulleyInstance(MaterialManager dispatcher, T blockEntity) {
		super(dispatcher, blockEntity);

		rotatingAbout = Direction.get(Direction.AxisDirection.POSITIVE, axis);
		rotationAxis = rotatingAbout.step();

		coil = getCoilModel().createInstance()
				.setPosition(getInstancePosition());

		magnet = new SelectInstance<>(this::getMagnetModelIndex);
		magnet.addModel(getMagnetModel())
				.addModel(getHalfMagnetModel());

		rope = new GroupInstance<>(getRopeModel());
		halfRope = new ConditionalInstance<>(getHalfRopeModel()).withCondition(this::shouldRenderHalfRope);

		updateOffset();
		updateVolume();

		light = new LightVolume(world, volume);
		light.initialize();
	}

	@Override
	public void beginFrame() {
		updateOffset();
		coil.setRotation(rotationAxis.rotationDegrees(offset * 180));

		int neededRopeCount = getNeededRopeCount();
		rope.resize(neededRopeCount);

		magnet.update()
				.get()
				.ifPresent(data -> {
					int i = Math.max(0, Mth.floor(offset));
					short packed = light.getPackedLight(pos.getX(), pos.getY() - i, pos.getZ());
					data.setPosition(getInstancePosition())
							.nudge(0, -offset, 0)
							.setBlockLight(LightPacking.getBlock(packed))
							.setSkyLight(LightPacking.getSky(packed));
				});

		halfRope.update()
				.get()
				.ifPresent(rope1 -> {
					float f = offset % 1;
					float halfRopeNudge = f > .75f ? f - 1 : f;

					short packed = light.getPackedLight(pos.getX(), pos.getY(), pos.getZ());
					rope1.setPosition(getInstancePosition())
							.nudge(0, -halfRopeNudge, 0)
							.setBlockLight(LightPacking.getBlock(packed))
							.setSkyLight(LightPacking.getSky(packed));
				});

		if (isRunning()) {
			int size = rope.size();
			int bottomY = pos.getY() - size;
			for (int i = 0; i < size; i++) {
				short packed = light.getPackedLight(pos.getX(), bottomY + i, pos.getZ());

				rope.get(i)
						.setPosition(getInstancePosition())
						.nudge(0, -offset + i + 1, 0)
						.setBlockLight(LightPacking.getBlock(packed))
						.setSkyLight(LightPacking.getSky(packed));
			}
		} else {
			rope.clear();
		}
	}

	@Override
	public void updateLight() {
		super.updateLight();
		relight(pos, coil);
	}

	@Override
	public void remove() {
		super.remove();
		coil.delete();
		magnet.delete();
		rope.clear();
		halfRope.delete();
		light.delete();
	}

	protected abstract Instancer<OrientedData> getRopeModel();

	protected abstract Instancer<OrientedData> getMagnetModel();

	protected abstract Instancer<OrientedData> getHalfMagnetModel();

	protected abstract Instancer<OrientedData> getCoilModel();

	protected abstract Instancer<OrientedData> getHalfRopeModel();

	protected abstract float getOffset();

	protected abstract boolean isRunning();

	@Override
	public boolean tickLightListener() {
		if (updateVolume()) {
			light.move(volume);
			return true;
		}
		return false;
	}

	private boolean updateVolume() {
		int length = Mth.ceil(offset) + 2;

		if (volume.sizeY() < length) {
			volume.assign(pos.below(length), pos)
					.fixMinMax();
			return true;
		}
		return false;
	}

	private void updateOffset() {
		offset = getOffset();
	}

	private int getNeededRopeCount() {
		return Math.max(0, Mth.ceil(offset - 1.25f));
	}

	private boolean shouldRenderHalfRope() {
		float f = offset % 1;
		return offset > .75f && (f < .25f || f > .75f);
	}

	private int getMagnetModelIndex() {
		if (isRunning() || offset == 0) {
			return offset > .25f ? 0 : 1;
		} else {
			return -1;
		}
	}

	@Override
	public boolean decreaseFramerateWithDistance() {
		return false;
	}

	@Override
	public ImmutableBox getVolume() {
		return volume;
	}

	@Override
	public void onLightUpdate(LightLayer type, ImmutableBox changed) {
		super.onLightUpdate(type, changed);
		light.onLightUpdate(type, changed);
	}
}
