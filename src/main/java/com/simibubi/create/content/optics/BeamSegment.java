package com.simibubi.create.content.optics;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

import static com.simibubi.create.foundation.utility.VecHelper.UP;

public class BeamSegment {
	public final float[] colors;
	private final Vector3d direction;
	private final Vector3d start;
	private final LazyValue<Vector3d> normalized;
	private final LazyValue<Quaternion> beaconBeamModifier;
	private final ILightHandler<? extends TileEntity> handler;
	private int length;

	public BeamSegment(ILightHandler<? extends TileEntity> handler, @Nonnull float[] color, Vector3d start, Vector3d direction) {
		this.handler = handler;
		this.colors = color;
		this.direction = direction;
		this.start = start;
		this.length = 1;
		this.normalized = new LazyValue<>(direction::normalize);
		beaconBeamModifier = new LazyValue<>(this::constructBeaconModifierQuat);
	}

	public void incrementLength() {
		++this.length;
	}

	public float[] getColors() {
		return this.colors;
	}

	public int getLength() {
		return this.length;
	}

	public Vector3d getDirection() {
		return direction;
	}

	public Vector3d getStart() {
		return start;
	}

	public Vector3d getNormalized() {
		return normalized.getValue();
	}

	public ILightHandler<? extends TileEntity> getHandler() {
		return handler;
	}

	@OnlyIn(Dist.CLIENT)
	private Quaternion constructBeaconModifierQuat() {
		double dotProd = getNormalized()
				.dotProduct(UP);

		Direction axis = getHandler().getBeamRotationAround();
		if (axis == null)
			return Quaternion.IDENTITY;
		Vector3f unitVec = axis.getUnitVector();
		return unitVec.getRadialQuaternion((float) (-Math.acos(dotProd) * Math.signum(new Vector3d(unitVec).dotProduct(getNormalized().crossProduct(UP)))));
	}

	@OnlyIn(Dist.CLIENT)
	public Quaternion getBeaconBeamModifier() {
		return beaconBeamModifier.getValue();
	}
}
