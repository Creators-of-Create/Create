package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import java.util.Arrays;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.render.backend.core.OrientedData;
import com.simibubi.create.foundation.render.backend.instancing.IDynamicInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.util.ConditionalInstance;
import com.simibubi.create.foundation.render.backend.instancing.util.InstanceGroup;
import com.simibubi.create.foundation.render.backend.instancing.util.SelectInstance;
import com.simibubi.create.foundation.render.backend.light.GridAlignedBB;
import com.simibubi.create.foundation.render.backend.light.LightUpdateListener;
import com.simibubi.create.foundation.render.backend.light.LightUpdater;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;

public abstract class AbstractPulleyInstance extends ShaftInstance implements IDynamicInstance, LightUpdateListener {

	final OrientedData coil;
	final SelectInstance<OrientedData> magnet;
	final InstanceGroup<OrientedData> rope;
	final ConditionalInstance<OrientedData> halfRope;

	protected float offset;
	protected final Direction rotatingAbout;
	protected final Vector3f rotationAxis;

	private byte[] bLight = new byte[1];
	private byte[] sLight = new byte[1];
	private GridAlignedBB volume;

	public AbstractPulleyInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
		super(dispatcher, tile);

		rotatingAbout = Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis);
		rotationAxis = rotatingAbout.getUnitVector();

		coil = getCoilModel()
				.createInstance()
				.setPosition(getInstancePosition());

		magnet = new SelectInstance<>(this::getMagnetModelIndex);
		magnet.addModel(getMagnetModel())
				.addModel(getHalfMagnetModel());

		rope = new InstanceGroup<>(getRopeModel());
		halfRope = new ConditionalInstance<>(getHalfRopeModel(), this::shouldRenderHalfRope);
	}

	@Override
	public void beginFrame() {
		updateOffset();

		transformModels();
	}

	private void transformModels() {
		resizeRope();

		coil.setRotation(rotationAxis.getDegreesQuaternion(offset * 180));
		magnet.update().get().ifPresent(data ->
				{
					int index = Math.max(0, MathHelper.floor(offset));
					data.setPosition(getInstancePosition())
							.nudge(0, -offset, 0)
							.setBlockLight(bLight[index])
							.setSkyLight(sLight[index]);
				}
		);

		halfRope.update().get().ifPresent(rope -> {
			float f = offset % 1;
			float halfRopeNudge = f > .75f ? f - 1 : f;

			rope.setPosition(getInstancePosition())
					.nudge(0, -halfRopeNudge, 0)
					.setBlockLight(bLight[0])
					.setSkyLight(sLight[0]);
		});

		if (isRunning()) {
			int size = rope.size();
			for (int i = 0; i < size; i++) {
				rope.get(i)
						.setPosition(getInstancePosition())
						.nudge(0, -offset + i + 1, 0)
						.setBlockLight(bLight[size - i])
						.setSkyLight(sLight[size - i]);
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
	}

	protected abstract InstancedModel<OrientedData> getRopeModel();

	protected abstract InstancedModel<OrientedData> getMagnetModel();

	protected abstract InstancedModel<OrientedData> getHalfMagnetModel();

	protected abstract InstancedModel<OrientedData> getCoilModel();

	protected abstract InstancedModel<OrientedData> getHalfRopeModel();

	protected abstract float getOffset();

	protected abstract boolean isRunning();

	protected void resizeRope() {
		int neededRopeCount = getNeededRopeCount();
		rope.resize(neededRopeCount);

		int length = MathHelper.ceil(offset);

		if (volume == null || bLight.length < length + 1) {
			volume = GridAlignedBB.from(pos.down(length), pos);
			volume.fixMinMax();

			bLight = Arrays.copyOf(bLight, length + 1);
			sLight = Arrays.copyOf(sLight, length + 1);

			initLight(world, volume);

			LightUpdater.getInstance().startListening(volume, this);
		}
	}

	private void updateOffset() {
		offset = getOffset();
	}

	private int getNeededRopeCount() {
		return Math.max(0, MathHelper.ceil(offset - 1.25f));
	}

	private boolean shouldRenderHalfRope() {
		float f = offset % 1;
		return offset > .75f && (f < .25f || f > .75f);
	}

	private int getMagnetModelIndex() {
		if (isRunning() || offset == 0) {
			return  offset > .25f ? 0 : 1;
		} else {
			return -1;
		}
	}

	@Override
	public boolean decreaseFramerateWithDistance() {
		return false;
	}

	@Override
	public boolean onLightUpdate(IBlockDisplayReader world, LightType type, GridAlignedBB changed) {
		changed.intersectAssign(volume);

		initLight(world, changed);

		return false;
	}

	private void initLight(IBlockDisplayReader world, GridAlignedBB changed) {
		int top = this.pos.getY();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		changed.forEachContained((x, y, z) -> {
			pos.setPos(x, y, z);
			byte block = (byte) world.getLightLevel(LightType.BLOCK, pos);
			byte sky = (byte) world.getLightLevel(LightType.SKY, pos);

			int i = top - y;

			bLight[i] = block;
			sLight[i] = sky;
		});
	}
}
