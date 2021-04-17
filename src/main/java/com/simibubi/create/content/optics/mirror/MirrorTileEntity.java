package com.simibubi.create.content.optics.mirror;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.optics.BeamSegment;
import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.utility.BeaconHelper;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MirrorTileEntity extends KineticTileEntity implements ILightHandler<MirrorTileEntity> {
	public final List<BeamSegment> beam;
	protected float angle;
	protected float clientAngleDiff;
	private float prevAngle;
	private Optional<BeaconTileEntity> beacon;
	private float[] initialColor = DyeColor.WHITE.getColorComponentValues();

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
		super.fromTag(state, compound, clientPacket);

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
		beam.clear();
		beam.addAll(constructOutBeam(getBeamDirection()));
	}

	@Override
	public boolean shouldRenderAsTE() {
		return true;
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

	@Override
	public void setColor(float[] initialColor) {
		this.initialColor = initialColor;
	}

	@Override
	public float[] getSegmentStartColor() {
		return initialColor;
	}

	@Nonnull
	@OnlyIn(Dist.CLIENT)
	@Override
	public Vector3f getBeamRotationAround() {
		return Direction.getFacingFromAxisDirection(getBlockState().get(BlockStateProperties.AXIS), Direction.AxisDirection.POSITIVE)
				.getUnitVector();
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float forcedAngle) {
		angle = forcedAngle;
	}
}
