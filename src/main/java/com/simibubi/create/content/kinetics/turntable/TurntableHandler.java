package com.simibubi.create.content.kinetics.turntable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class TurntableHandler {

	public static void gameRenderTick() {
		Minecraft mc = Minecraft.getInstance();
		BlockPos pos = mc.player.blockPosition();

		if (!AllBlocks.TURNTABLE.has(mc.level.getBlockState(pos)))
			return;
		if (!mc.player.isOnGround())
			return;
		if (mc.isPaused())
			return;

		BlockEntity blockEntity = mc.level.getBlockEntity(pos);
		if (!(blockEntity instanceof TurntableBlockEntity))
			return;
		
		TurntableBlockEntity turnTable = (TurntableBlockEntity) blockEntity;
		float speed = turnTable.getSpeed() * 3/10;

		if (speed == 0)
			return;
		
		Vec3 origin = VecHelper.getCenterOf(pos);
		Vec3 offset = mc.player.position().subtract(origin);
		
		if (offset.length() > 1/4f)
			speed *= Mth.clamp((1/2f - offset.length()) * 2, 0, 1);

		mc.player.setYRot(mc.player.yRotO - speed * AnimationTickHolder.getPartialTicks());
		mc.player.yBodyRot = mc.player.getYRot();
	}

}
