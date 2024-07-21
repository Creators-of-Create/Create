package com.simibubi.create.content.contraptions.pulley;

import java.util.function.Consumer;

import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.render.ConditionalInstance;
import com.simibubi.create.foundation.render.GroupInstance;
import com.simibubi.create.foundation.render.SelectInstance;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.math.MoreMath;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;

public abstract class AbstractPulleyVisual<T extends KineticBlockEntity> extends ShaftVisual<T> implements SimpleDynamicVisual {
	private final OrientedInstance coil;
	private final SelectInstance<OrientedInstance> magnet;
	private final GroupInstance<OrientedInstance> rope;
	private final ConditionalInstance<OrientedInstance> halfRope;

	protected final Direction rotatingAbout;
	protected final Axis rotationAxis;

	private final LightCache lightCache = new LightCache(1);

	private float offset;

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

		updateOffset(partialTick);
	}

	protected abstract Instancer<OrientedInstance> getRopeModel();

	protected abstract Instancer<OrientedInstance> getMagnetModel();

	protected abstract Instancer<OrientedInstance> getHalfMagnetModel();

	protected abstract Instancer<OrientedInstance> getCoilModel();

	protected abstract Instancer<OrientedInstance> getHalfRopeModel();

	protected abstract float getOffset(float pt);

	protected abstract boolean isRunning();

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
					int light = lightCache.getPackedLight(i);
					data.setPosition(getVisualPosition())
							.nudgePosition(0, -offset, 0)
							.light(light)
							.setChanged();
				});

		halfRope.update()
				.get()
				.ifPresent(rope1 -> {
					float f = offset % 1;
					float halfRopeNudge = f > .75f ? f - 1 : f;

					int light = lightCache.getPackedLight(0);
					rope1.setPosition(getVisualPosition())
							.nudgePosition(0, -halfRopeNudge, 0)
							.light(light)
							.setChanged();
				});

		if (isRunning()) {
			int size = rope.size();
			for (int i = 0; i < size; i++) {
				int light = lightCache.getPackedLight(size - 1 - i);

				rope.get(i)
						.setPosition(getVisualPosition())
						.nudgePosition(0, -offset + i + 1, 0)
						.light(light)
						.setChanged();
			}
		} else {
			rope.clear();
		}
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);
		relight(coil);

		lightCache.update();
	}

	private void updateOffset(float pt) {
		offset = getOffset(pt);
		lightCache.setSize(Mth.ceil(offset) + 2);
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

	@Override
    protected void _delete() {
		super._delete();
		coil.delete();
		magnet.delete();
		rope.clear();
		halfRope.delete();
	}

	private class LightCache {
		private final ByteList data;
		private final LongSet sections = new LongOpenHashSet();
		private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		private int sectionCount;

		public LightCache(int initialSize) {
			data = new ByteArrayList(initialSize);
			setSize(initialSize);
		}

		public void setSize(int size) {
			if (size != data.size()) {
				data.size(size);

				int sectionCount = MoreMath.ceilingDiv(size + 15 - pos.getY() + pos.getY() / 4 * 4, SectionPos.SECTION_SIZE);
				if (sectionCount != this.sectionCount) {
					this.sectionCount = sectionCount;
					sections.clear();
					int sectionX = SectionPos.blockToSectionCoord(pos.getX());
					int sectionY = SectionPos.blockToSectionCoord(pos.getY());
					int sectionZ = SectionPos.blockToSectionCoord(pos.getZ());
					for (int i = 0; i < sectionCount; i++) {
						sections.add(SectionPos.asLong(sectionX, sectionY - i, sectionZ));
					}
					lightSections.sections(sections);
				}
			}
		}

		public void update() {
			mutablePos.set(pos);

			for (int i = 0; i < data.size(); i++) {
				int blockLight = level.getBrightness(LightLayer.BLOCK, mutablePos);
				int skyLight = level.getBrightness(LightLayer.SKY, mutablePos);
				int light = ((skyLight << 4) & 0xF) | (blockLight & 0xF);
				data.set(i, (byte) light);
				mutablePos.move(Direction.DOWN);
			}
		}

		public int getPackedLight(int offset) {
			if (offset < 0 || offset >= data.size()) {
				return 0;
			}

			int light = Byte.toUnsignedInt(data.getByte(offset));
			int blockLight = light & 0xF;
			int skyLight = (light >>> 4) & 0xF;
			return LightTexture.pack(blockLight, skyLight);
		}
	}
}
