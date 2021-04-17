package com.simibubi.create.content.optics.mirror;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.optics.BeamSegment;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.utility.BeaconHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class MirrorTileEntity extends KineticTileEntity implements ILightHandler<MirrorTileEntity> {
	public final List<BeamSegment> beam;
	protected float angle;
	protected float clientAngleDiff;
	private float prevAngle;
	private double length;
	private Optional<BeaconTileEntity> beacon;

	public MirrorTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		beacon = Optional.empty();
		beam = new ArrayList<>();
		setLazyTickRate(20);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putFloat("Angle", angle);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		if (wasMoved) {
			super.fromTag(state, compound, clientPacket);
			return;
		}

		angle = compound.getFloat("Angle");
		super.fromTag(state, compound, clientPacket);
	}

	public float getInterpolatedAngle(float partialTicks) {
		if (isVirtual())
			return MathHelper.lerp(partialTicks + .5f, prevAngle, angle);
		return MathHelper.lerp(partialTicks, angle, angle + getAngularSpeed());
	}

	public float getAngularSpeed() {
		float speed = getSpeed() * 3 / 10f;
		if (getSpeed() == 0)
			speed = 0;
		if (world != null && world.isRemote) {
			speed *= ServerSpeedProvider.get();
			speed += clientAngleDiff / 3f;
		}
		return speed;
	}

	@Override
	public void tick() {
		super.tick();

		prevAngle = angle;
		if (world != null && world.isRemote)
			clientAngleDiff /= 2;

		float angularSpeed = getAngularSpeed();
		float newAngle = angle + angularSpeed;
		angle = newAngle % 360;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		beacon = BeaconHelper.getBeaconTE(pos, world);
		length = getBeamLenght();

		if (length < 1)
			return;
		Vector3d direction = VecHelper.step(getBeamDirection());
		Vector3d startPos = VecHelper.getCenterOf(getPos());
		beam.clear();

		float[] startColor = DyeColor.WHITE.getColorComponentValues(); // TODO: Add mirroring of color
		BeamSegment segment = new BeamSegment(startColor, startPos, direction);

		for (int i = 0; i < length; i++) {
			startPos = startPos.add(direction); // check next block
			float[] newColor = BeaconHelper.getBeaconColorAt(startPos, world);
			if (newColor != null && !Arrays.equals(startColor, newColor)) {
				beam.add(segment);
				startColor = new float[]{(segment.colors[0] + newColor[0]) / 2.0F, (segment.colors[1] + newColor[1]) / 2.0F, (segment.colors[2] + newColor[2]) / 2.0F};
				segment = new BeamSegment(newColor, startPos, direction);
			} else {
				segment.incrementLength();
			}
		}
		beam.add(segment);
	}

	@Override
	public boolean shouldRenderAsTE() {
		return true;
	}

	public void setAngle(float forcedAngle) {
		angle = forcedAngle;
	}

	@Override
	public Vector3d getBeamDirection() {
		// TODO: Implement properly
		return VecHelper.step(Vector3d.of(Direction.SOUTH.getDirectionVec()));
	}

	@Override
	public MirrorTileEntity getTile() {
		return this;
	}
}
