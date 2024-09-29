package com.simibubi.create.content.contraptions.pulley;

import java.util.function.Consumer;

import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.ShaftVisual;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.math.MoreMath;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.util.SmartRecycler;
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
	private final TransformedInstance magnet;
	private final SmartRecycler<Boolean, TransformedInstance> rope;

	protected final Direction rotatingAbout;
	protected final Axis rotationAxis;

	private final LightCache lightCache = new LightCache();

	private float offset;

	public AbstractPulleyVisual(VisualizationContext dispatcher, T blockEntity, float partialTick) {
		super(dispatcher, blockEntity, partialTick);

		rotatingAbout = Direction.get(Direction.AxisDirection.POSITIVE, axis);
		rotationAxis = Axis.of(rotatingAbout.step());

		coil = getCoilModel().createInstance()
				.position(getVisualPosition());
		coil.setChanged();

		magnet = magnetInstancer().createInstance();

		rope = new SmartRecycler<>(b -> b ? getHalfRopeModel().createInstance() : getRopeModel().createInstance());

		updateOffset(partialTick);
		updateLight(partialTick);
	}

	@Override
	public void setSectionCollector(SectionCollector sectionCollector) {
		super.setSectionCollector(sectionCollector);
		lightCache.updateSections();
	}

	protected abstract Instancer<TransformedInstance> getRopeModel();

	protected abstract Instancer<TransformedInstance> getMagnetModel();

	protected abstract Instancer<TransformedInstance> getHalfMagnetModel();

	protected abstract Instancer<OrientedInstance> getCoilModel();

	protected abstract Instancer<TransformedInstance> getHalfRopeModel();

	protected abstract float getOffset(float pt);

	protected abstract boolean isRunning();

	private Instancer<TransformedInstance> magnetInstancer() {
		return offset > .25f ? getMagnetModel() : getHalfMagnetModel();
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		updateOffset(ctx.partialTick());
		coil.rotation(rotationAxis.rotationDegrees(offset * 180))
				.setChanged();

		magnet.setVisible(isRunning() || offset == 0);

		magnetInstancer().stealInstance(magnet);

		magnet.setIdentityTransform()
				.translate(getVisualPosition())
				.translate(0, -offset, 0)
				.light(lightCache.getPackedLight(Math.max(0, Mth.floor(offset))))
				.setChanged();

		rope.resetCount();

		if (shouldRenderHalfRope()) {
			float f = offset % 1;
			float halfRopeNudge = f > .75f ? f - 1 : f;

			rope.get(true).setIdentityTransform()
					.translate(getVisualPosition())
					.translate(0, -halfRopeNudge, 0)
					.light(lightCache.getPackedLight(0))
					.setChanged();
		}

		if (isRunning()) {
			int neededRopeCount = getNeededRopeCount();

			for (int i = 0; i < neededRopeCount; i++) {

				rope.get(false)
						.setIdentityTransform()
						.translate(getVisualPosition())
						.translate(0, -offset + i + 1, 0)
						.light(lightCache.getPackedLight(neededRopeCount - 1 - i))
						.setChanged();
			}
		}

		rope.discardExtra();
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

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		consumer.accept(coil);
		consumer.accept(magnet);
	}

	@Override
    protected void _delete() {
		super._delete();
		coil.delete();
		magnet.delete();
		rope.delete();
	}

	private class LightCache {
		private final ByteList data =  new ByteArrayList();
		private final LongSet sections = new LongOpenHashSet();
		private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		private int sectionCount;

		public void setSize(int size) {
			if (size != data.size()) {
				data.size(size);
				update();

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
					// Will be null during initialization
					if (lightSections != null) {
						updateSections();
					}
				}
			}
		}

		public void updateSections() {
			lightSections.sections(sections);
		}

		public void update() {
			mutablePos.set(pos);

			for (int i = 0; i < data.size(); i++) {
				int blockLight = level.getBrightness(LightLayer.BLOCK, mutablePos);
				int skyLight = level.getBrightness(LightLayer.SKY, mutablePos);
				int light = ((skyLight & 0xF) << 4) | (blockLight & 0xF);
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
