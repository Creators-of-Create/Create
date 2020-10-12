package com.simibubi.create.content.contraptions.components.fan;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@MethodsReturnNonnullByDefault
public interface IAirCurrentSource {
	@Nullable
	AirCurrent getAirCurrent();

	@Nullable
	World getAirCurrentWorld();

	BlockPos getAirCurrentPos();

	float getSpeed();

	Direction getAirflowOriginSide();

	@Nullable
	Direction getAirFlowDirection();

	default float getMaxDistance() {
		float speed = Math.abs(this.getSpeed());
		CKinetics config = AllConfigs.SERVER.kinetics;
		float distanceFactor = Math.min(speed / config.fanRotationArgmax.get(), 1);
		float pushDistance = MathHelper.lerp(distanceFactor, 3, config.fanPushDistance.get());
		float pullDistance = MathHelper.lerp(distanceFactor, 3f, config.fanPullDistance.get());
		return this.getSpeed() > 0 ? pushDistance : pullDistance;
	}

	boolean isSourceRemoved();
}
