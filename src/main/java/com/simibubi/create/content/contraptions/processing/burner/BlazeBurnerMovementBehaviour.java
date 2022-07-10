package com.simibubi.create.content.contraptions.processing.burner;

import java.util.Random;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraption;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlazeBurnerMovementBehaviour implements MovementBehaviour {

	@Override
	public boolean renderAsNormalTileEntity() {
		return false;
	}

	@Override
	public void tick(MovementContext context) {
		if (!context.world.isClientSide())
			return;
		if (!shouldRender(context))
			return;

		Random r = context.world.getRandom();
		Vec3 c = context.position;
		Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f)
			.multiply(1, 0, 1));
		if (r.nextInt(3) == 0 && context.motion.length() < 1 / 64f)
			context.world.addParticle(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);

		LerpedFloat headAngle = getHeadAngle(context);
		boolean quickTurn = shouldRenderHat(context) && !Mth.equal(context.relativeMotion.length(), 0);
		headAngle.chase(
			headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), getTargetAngle(context)), .5f,
			quickTurn ? Chaser.EXP : Chaser.exp(5));
		headAngle.tickChaser();
	}

	public void invalidate(MovementContext context) {
		context.data.remove("Conductor");
	}

	private boolean shouldRender(MovementContext context) {
		return context.state.getOptionalValue(BlazeBurnerBlock.HEAT_LEVEL)
			.orElse(HeatLevel.NONE) != HeatLevel.NONE;
	}

	private LerpedFloat getHeadAngle(MovementContext context) {
		if (!(context.temporaryData instanceof LerpedFloat))
			context.temporaryData = LerpedFloat.angular()
				.startWithValue(getTargetAngle(context));
		return (LerpedFloat) context.temporaryData;
	}

	private float getTargetAngle(MovementContext context) {
		if (shouldRenderHat(context) && !Mth.equal(context.relativeMotion.length(), 0)
			&& context.contraption.entity instanceof CarriageContraptionEntity cce) {

			float angle = AngleHelper.deg(-Mth.atan2(context.relativeMotion.x, context.relativeMotion.z));
			return cce.getInitialOrientation()
				.getAxis() == Axis.X ? angle + 180 : angle;
		}

		Entity player = Minecraft.getInstance().cameraEntity;
		if (player != null && !player.isInvisible() && context.position != null) {
			Vec3 applyRotation = context.contraption.entity.reverseRotation(player.position()
				.subtract(context.position), 1);
			double dx = applyRotation.x;
			double dz = applyRotation.z;
			return AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
		}
		return 0;
	}

	private boolean shouldRenderHat(MovementContext context) {
		CompoundTag data = context.data;
		if (!data.contains("Conductor"))
			data.putBoolean("Conductor", determineIfConducting(context));
		return data.getBoolean("Conductor") && (context.contraption.entity instanceof CarriageContraptionEntity cce)
			&& cce.hasSchedule();
	}

	private boolean determineIfConducting(MovementContext context) {
		Contraption contraption = context.contraption;
		if (!(contraption instanceof CarriageContraption carriageContraption))
			return false;
		Direction assemblyDirection = carriageContraption.getAssemblyDirection();
		for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis()))
			if (carriageContraption.inControl(context.localPos, direction))
				return true;
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		if (!shouldRender(context))
			return;
		BlazeBurnerRenderer.renderInContraption(context, renderWorld, matrices, buffer, getHeadAngle(context),
			shouldRenderHat(context));
	}

}
