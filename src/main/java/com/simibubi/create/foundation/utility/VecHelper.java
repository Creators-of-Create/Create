package com.simibubi.create.foundation.utility;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

public class VecHelper {

	public static final Vector3f ZERO_3F = new Vector3f(0, 0, 0);
	public static final Vector3d CENTER_OF_ORIGIN = new Vector3d(.5, .5, .5);

	public static Vector3d rotate(Vector3d vec, Vector3d rotationVec) {
		return rotate(vec, rotationVec.x, rotationVec.y, rotationVec.z);
	}

	public static Vector3d rotate(Vector3d vec, double xRot, double yRot, double zRot) {
		return rotate(rotate(rotate(vec, xRot, Axis.X), yRot, Axis.Y), zRot, Axis.Z);
	}

	public static Vector3d rotateCentered(Vector3d vec, double deg, Axis axis) {
		Vector3d shift = getCenterOf(BlockPos.ZERO);
		return VecHelper.rotate(vec.subtract(shift), deg, axis).add(shift);
	}

	public static Vector3d rotate(Vector3d vec, double deg, Axis axis) {
		if (deg == 0)
			return vec;
		if (vec == Vector3d.ZERO)
			return vec;

		float angle = (float) (deg / 180f * Math.PI);
		double sin = MathHelper.sin(angle);
		double cos = MathHelper.cos(angle);
		double x = vec.x;
		double y = vec.y;
		double z = vec.z;

		if (axis == Axis.X)
			return new Vector3d(x, y * cos - z * sin, z * cos + y * sin);
		if (axis == Axis.Y)
			return new Vector3d(x * cos + z * sin, y, z * cos - x * sin);
		if (axis == Axis.Z)
			return new Vector3d(x * cos - y * sin, y * cos + x * sin, z);
		return vec;
	}

	public static Vector3d mirrorCentered(Vector3d vec, Mirror mirror) {
		Vector3d shift = getCenterOf(BlockPos.ZERO);
		return VecHelper.mirror(vec.subtract(shift), mirror).add(shift);
	}

	public static Vector3d mirror(Vector3d vec, Mirror mirror) {
		if (mirror == null || mirror == Mirror.NONE)
			return vec;
		if (vec == Vector3d.ZERO)
			return vec;

		double x = vec.x;
		double y = vec.y;
		double z = vec.z;

		if (mirror == Mirror.LEFT_RIGHT)
			return new Vector3d(x, y, -z);
		if (mirror == Mirror.FRONT_BACK)
			return new Vector3d(-x, y, z);
		return vec;
	}

	public static Vector3d lookAt(Vector3d vec, Vector3d fwd) {
		fwd = fwd.normalize();
		Vector3d up = new Vector3d(0,1,0);
		double dot = fwd.dot(up);
		if (Math.abs(dot) > 1 - 1.0E-3)
			up = new Vector3d(0, 0, dot > 0 ? 1 : -1);
		Vector3d right = fwd.cross(up).normalize();
		up = right.cross(fwd).normalize();
		double x = vec.x * right.x + vec.y * up.x + vec.z * fwd.x;
		double y = vec.x * right.y + vec.y * up.y + vec.z * fwd.y;
		double z = vec.x * right.z + vec.y * up.z + vec.z * fwd.z;
		return new Vector3d(x, y, z);
	}

	public static boolean isVecPointingTowards(Vector3d vec, Direction direction) {
		return Vector3d.atLowerCornerOf(direction.getNormal())
			.dot(vec.normalize()) > 0.125; // slight tolerance to activate perpendicular movement actors
	}

	public static Vector3d getCenterOf(Vector3i pos) {
		if (pos.equals(Vector3i.ZERO))
			return CENTER_OF_ORIGIN;
		return Vector3d.atLowerCornerOf(pos)
			.add(.5f, .5f, .5f);
	}

	public static Vector3d offsetRandomly(Vector3d vec, Random r, float radius) {
		return new Vector3d(vec.x + (r.nextFloat() - .5f) * 2 * radius, vec.y + (r.nextFloat() - .5f) * 2 * radius,
			vec.z + (r.nextFloat() - .5f) * 2 * radius);
	}

	public static Vector3d axisAlingedPlaneOf(Vector3d vec) {
		vec = vec.normalize();
		return new Vector3d(1, 1, 1).subtract(Math.abs(vec.x), Math.abs(vec.y), Math.abs(vec.z));
	}

	public static Vector3d axisAlingedPlaneOf(Direction face) {
		return axisAlingedPlaneOf(Vector3d.atLowerCornerOf(face.getNormal()));
	}

	public static ListNBT writeNBT(Vector3d vec) {
		ListNBT listnbt = new ListNBT();
		listnbt.add(DoubleNBT.valueOf(vec.x));
		listnbt.add(DoubleNBT.valueOf(vec.y));
		listnbt.add(DoubleNBT.valueOf(vec.z));
		return listnbt;
	}

	public static Vector3d readNBT(ListNBT list) {
		if (list.isEmpty())
			return Vector3d.ZERO;
		return new Vector3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
	}

	public static Vector3d voxelSpace(double x, double y, double z) {
		return new Vector3d(x, y, z).scale(1 / 16f);
	}

	public static int getCoordinate(Vector3i pos, Axis axis) {
		return axis.choose(pos.getX(), pos.getY(), pos.getZ());
	}

	public static float getCoordinate(Vector3d vec, Axis axis) {
		return (float) axis.choose(vec.x, vec.y, vec.z);
	}

	public static boolean onSameAxis(BlockPos pos1, BlockPos pos2, Axis axis) {
		if (pos1.equals(pos2))
			return true;
		for (Axis otherAxis : Axis.values())
			if (axis != otherAxis)
				if (getCoordinate(pos1, otherAxis) != getCoordinate(pos2, otherAxis))
					return false;
		return true;
	}

	public static Vector3d clamp(Vector3d vec, float maxLength) {
		return vec.length() > maxLength ? vec.normalize()
			.scale(maxLength) : vec;
	}

	public static Vector3d lerp(float p, Vector3d from, Vector3d to) {
		return from.add(to.subtract(from)
			.scale(p));
	}

	public static Vector3d clampComponentWise(Vector3d vec, float maxLength) {
		return new Vector3d(MathHelper.clamp(vec.x, -maxLength, maxLength),
			MathHelper.clamp(vec.y, -maxLength, maxLength), MathHelper.clamp(vec.z, -maxLength, maxLength));
	}

	public static Vector3d project(Vector3d vec, Vector3d ontoVec) {
		if (ontoVec.equals(Vector3d.ZERO))
			return Vector3d.ZERO;
		return ontoVec.scale(vec.dot(ontoVec) / ontoVec.lengthSqr());
	}

	@Nullable
	public static Vector3d intersectSphere(Vector3d origin, Vector3d lineDirection, Vector3d sphereCenter,
		double radius) {
		if (lineDirection.equals(Vector3d.ZERO))
			return null;
		if (lineDirection.length() != 1)
			lineDirection = lineDirection.normalize();

		Vector3d diff = origin.subtract(sphereCenter);
		double lineDotDiff = lineDirection.dot(diff);
		double delta = lineDotDiff * lineDotDiff - (diff.lengthSqr() - radius * radius);
		if (delta < 0)
			return null;
		double t = -lineDotDiff + MathHelper.sqrt(delta);
		return origin.add(lineDirection.scale(t));
	}

	// https://forums.minecraftforge.net/topic/88562-116solved-3d-to-2d-conversion/?do=findComment&comment=413573 slightly modified
	public static Vector3d projectToPlayerView(Vector3d target, float partialTicks) {
		/*
		 * The (centered) location on the screen of the given 3d point in the world.
		 * Result is (dist right of center screen, dist up from center screen, if < 0, then in front of view plane)
		 */
		ActiveRenderInfo ari = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vector3d camera_pos = ari.getPosition();
		Quaternion camera_rotation_conj = ari.rotation()
			.copy();
		camera_rotation_conj.conj();

		Vector3f result3f = new Vector3f((float) (camera_pos.x - target.x), (float) (camera_pos.y - target.y),
			(float) (camera_pos.z - target.z));
		result3f.transform(camera_rotation_conj);

		// ----- compensate for view bobbing (if active) -----
		// the following code adapted from GameRenderer::applyBobbing (to invert it)
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.bobView) {
			Entity renderViewEntity = mc.getCameraEntity();
			if (renderViewEntity instanceof PlayerEntity) {
				PlayerEntity playerentity = (PlayerEntity) renderViewEntity;
				float distwalked_modified = playerentity.walkDist;

				float f = distwalked_modified - playerentity.walkDistO;
				float f1 = -(distwalked_modified + f * partialTicks);
				float f2 = MathHelper.lerp(partialTicks, playerentity.oBob, playerentity.bob);
				Quaternion q2 = new Quaternion(Vector3f.XP,
					Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, true);
				q2.conj();
				result3f.transform(q2);

				Quaternion q1 =
					new Quaternion(Vector3f.ZP, MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, true);
				q1.conj();
				result3f.transform(q1);

				Vector3f bob_translation = new Vector3f((MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F),
					(-Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2)), 0.0f);
				bob_translation.setY(-bob_translation.y()); // this is weird but hey, if it works
				result3f.add(bob_translation);
			}
		}

		// ----- adjust for fov -----
		float fov = (float) mc.gameRenderer.getFov(ari, partialTicks, true);

		float half_height = (float) mc.getWindow()
			.getGuiScaledHeight() / 2;
		float scale_factor = half_height / (result3f.z() * (float) Math.tan(Math.toRadians(fov / 2)));
		return new Vector3d(-result3f.x() * scale_factor, result3f.y() * scale_factor, result3f.z());
	}

}
