package com.simibubi.create.content.contraptions.pulley;

import java.util.function.Consumer;

import com.simibubi.create.foundation.utility.flywheel.box.MutableBox;
import com.simibubi.create.foundation.utility.flywheel.light.LightVolume;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.render.ConditionalInstance;
import com.simibubi.create.foundation.render.GroupInstance;
import com.simibubi.create.foundation.render.SelectInstance;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public abstract class AbstractPulleyVisual<T extends KineticBlockEntity> extends ShaftVisual<T> implements SimpleDynamicVisual {

	final OrientedInstance coil;
	final SelectInstance<OrientedInstance> magnet;
	final GroupInstance<OrientedInstance> rope;
	final ConditionalInstance<OrientedInstance> halfRope;

	protected float offset;
	protected final Direction rotatingAbout;
	protected final Axis rotationAxis;

	private final MutableBox volume = new MutableBox();
	private final LightVolume light;

	public AbstractPulleyVisual(VisualizationContext dispatcher, T blockEntity, float partialTick) {
		super(dispatcher, blockEntity, partialTick);

		rotatingAbout = Direction.get(Direction.AxisDirection.POSITIVE, axis);
		rotationAxis = Axis.of(rotatingAbout.step());

		coil = getCoilModel().createInstance()
				.setPosition(getVisualPosition());
		coil.setChanged();

		magnet = new SelectInstance<>(this::getMagnetModelIndex);
		magnet.addModel(getMagnetModel())
				.addModel(getHalfMagnetModel());

		rope = new GroupInstance<>(getRopeModel());
		halfRope = new ConditionalInstance<>(getHalfRopeModel()).withCondition(this::shouldRenderHalfRope);

		updateOffset(0);
		updateVolume();

		light = new LightVolume(level, volume);
		light.initialize();
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		updateOffset(ctx.partialTick());
		coil.setRotation(rotationAxis.rotationDegrees(offset * 180))
				.setChanged();

		int neededRopeCount = getNeededRopeCount();
		rope.resize(neededRopeCount);

		magnet.update()
				.get()
				.ifPresent(data -> {
					int i = Math.max(0, Mth.floor(offset));
					short packed = light.getPackedLight(pos.getX(), pos.getY() - i, pos.getZ());
					data.setPosition(getVisualPosition())
							.nudgePosition(0, -offset, 0)
							.light(packed)
							.setChanged();
				});

		halfRope.update()
				.get()
				.ifPresent(rope1 -> {
					float f = offset % 1;
					float halfRopeNudge = f > .75f ? f - 1 : f;

					short packed = light.getPackedLight(pos.getX(), pos.getY(), pos.getZ());
					rope1.setPosition(getVisualPosition())
							.nudgePosition(0, -halfRopeNudge, 0)
							.light(packed)
							.setChanged();
				});

		if (isRunning()) {
			int size = rope.size();
			int bottomY = pos.getY() - size;
			for (int i = 0; i < size; i++) {
				short packed = light.getPackedLight(pos.getX(), bottomY + i, pos.getZ());

				rope.get(i)
						.setPosition(getVisualPosition())
						.nudgePosition(0, -offset + i + 1, 0)
						.light(packed)
						.setChanged();
			}
		} else {
			rope.clear();
		}
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);
		light.copyLight(volume);
		relight(pos, coil);
	}

	@Override
    protected void _delete() {
		super._delete();
		coil.delete();
		magnet.delete();
		rope.clear();
		halfRope.delete();
		light.delete();
	}

	protected abstract Instancer<OrientedInstance> getRopeModel();

	protected abstract Instancer<OrientedInstance> getMagnetModel();

	protected abstract Instancer<OrientedInstance> getHalfMagnetModel();

	protected abstract Instancer<OrientedInstance> getCoilModel();

	protected abstract Instancer<OrientedInstance> getHalfRopeModel();

	protected abstract float getOffset(float pt);

	protected abstract boolean isRunning();

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
			volume.assign(pos.below(length), pos);
			volume.fixMinMax();
			return true;
		}
		return false;
	}

	private void updateOffset(float pt) {
		offset = getOffset(pt);
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
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		consumer.accept(coil);
		magnet.forEach(consumer);
		rope.forEach(consumer);
		halfRope.forEach(consumer);
	}
}
