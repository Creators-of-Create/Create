package com.simibubi.create.modules.contraptions.relays.gauge;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.advancement.AllCriterionTriggers;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.modules.contraptions.GogglesItem;
import com.simibubi.create.modules.contraptions.base.IRotate.SpeedLevel;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class SpeedGaugeTileEntity extends GaugeTileEntity {

	public SpeedGaugeTileEntity() {
		super(AllTileEntities.SPEED_GAUGE.type);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		float speed = Math.abs(getSpeed());
		float medium = AllConfigs.SERVER.kinetics.mediumSpeed.get().floatValue();
		float fast = AllConfigs.SERVER.kinetics.fastSpeed.get().floatValue();
		float max = AllConfigs.SERVER.kinetics.maxRotationSpeed.get().floatValue();
		color = ColorHelper.mixColors(SpeedLevel.of(speed).getColor(), 0xffffff, .25f);

		if (speed == 666){
			assert world != null;
			List<ServerPlayerEntity> players = world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos).grow(6));
			players.stream().filter(GogglesItem::canSeeParticles).forEach(AllCriterionTriggers.SPEED_READ::trigger);
		}

		if (speed == 0) {
			dialTarget = 0;
			color = 0x333333;
		} else if (speed < medium) {
			dialTarget = MathHelper.lerp(speed / medium, 0, .45f);
		} else if (speed < fast) {
			dialTarget = MathHelper.lerp((speed - medium) / (fast - medium), .45f, .75f);
		} else {
			dialTarget = MathHelper.lerp((speed - fast) / (max - fast), .75f, 1.125f);
		}
	}

}
