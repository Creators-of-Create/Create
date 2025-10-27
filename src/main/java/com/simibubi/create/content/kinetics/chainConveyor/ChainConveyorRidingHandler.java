package com.simibubi.create.content.kinetics.chainConveyor;

import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity.ConnectionStats;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class ChainConveyorRidingHandler {

	public static BlockPos ridingChainConveyor;
	public static float chainPosition;
	public static BlockPos ridingConnection;
	public static boolean flipped;
	public static int catchingUp;

	public static void embark(BlockPos lift, float position, BlockPos connection) {
		ridingChainConveyor = lift;
		chainPosition = position;
		ridingConnection = connection;
		catchingUp = 20;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level.getBlockEntity(ridingChainConveyor) instanceof ChainConveyorBlockEntity clbe)
			flipped = clbe.getSpeed() < 0;

		Component component = Component.translatable("mount.onboard", mc.options.keyShift.getTranslatedKeyMessage());
		mc.gui.setOverlayMessage(component, false);
		mc.getSoundManager()
			.play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_HIT, 1f, 0.5f));
	}

	public static void clientTick() {
		if (ridingChainConveyor == null)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused())
			return;
		if (!mc.player.isHolding(AllItemTags.CHAIN_RIDEABLE::matches)) {
			stopRiding();
			return;
		}
		BlockEntity blockEntity = mc.level.getBlockEntity(ridingChainConveyor);
		if (mc.player.isShiftKeyDown() || !(blockEntity instanceof ChainConveyorBlockEntity clbe)) {
			stopRiding();
			return;
		}
		if (ridingConnection != null && !clbe.connections.contains(ridingConnection)) {
			stopRiding();
			return;
		}

		clbe.prepareStats();

		float chainYOffset = 0.5f * mc.player.getScale();
		Vec3 playerPosition = mc.player.position()
			.add(0, mc.player.getBoundingBox()
				.getYsize() + chainYOffset, 0);

		updateTargetPosition(mc, clbe);

		blockEntity = mc.level.getBlockEntity(ridingChainConveyor);
		if (!(blockEntity instanceof ChainConveyorBlockEntity))
			return;

		clbe = (ChainConveyorBlockEntity) blockEntity;
		clbe.prepareStats();

		Vec3 targetPosition;

		if (ridingConnection != null) {
			ConnectionStats stats = clbe.connectionStats.get(ridingConnection);
			targetPosition = stats.start()
				.add((stats.end()
					.subtract(stats.start())).normalize()
					.scale(Math.min(stats.chainLength(), chainPosition)));
		} else {
			targetPosition = Vec3.atBottomCenterOf(ridingChainConveyor)
				.add(VecHelper.rotate(new Vec3(0, 0.25, 1), chainPosition, Axis.Y));
		}

		if (catchingUp > 0)
			catchingUp--;

		Vec3 diff = targetPosition.subtract(playerPosition);
		if (catchingUp == 0 && (diff.length() > 3 || diff.y < -1)) {
			stopRiding();
			return;
		}

		mc.player.setDeltaMovement(mc.player.getDeltaMovement()
			.scale(0.75)
			.add(diff.scale(0.25)));
		if (AnimationTickHolder.getTicks() % 10 == 0)
			CatnipServices.NETWORK.sendToServer(new ServerboundChainConveyorRidingPacket(ridingChainConveyor, false));
	}

	private static void stopRiding() {
		if (ridingChainConveyor != null)
			CatnipServices.NETWORK.sendToServer(new ServerboundChainConveyorRidingPacket(ridingChainConveyor, true));
		ridingChainConveyor = null;
		ridingConnection = null;
		Minecraft.getInstance()
			.getSoundManager()
			.play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_HIT, 0.75f, 0.35f));
	}

	private static void updateTargetPosition(Minecraft mc, ChainConveyorBlockEntity clbe) {
		float serverSpeed = ServerSpeedProvider.get();
		float speed = clbe.getSpeed() / 360f;
		float radius = 1.5f;
		float distancePerTick = Math.abs(speed);
		float degreesPerTick = (speed / (Mth.PI * radius)) * 360f;

		if (ridingConnection != null) {
			ConnectionStats stats = clbe.connectionStats.get(ridingConnection);

			if (flipped != clbe.getSpeed() < 0) {
				flipped = clbe.getSpeed() < 0;
				ridingChainConveyor = clbe.getBlockPos()
					.offset(ridingConnection);
				chainPosition = stats.chainLength() - chainPosition;
				ridingConnection = ridingConnection.multiply(-1);
				return;
			}

			chainPosition += serverSpeed * distancePerTick;
			chainPosition = Math.min(stats.chainLength(), chainPosition);
			if (chainPosition < stats.chainLength())
				return;

			// transfer to other
			if (mc.level.getBlockEntity(clbe.getBlockPos()
				.offset(ridingConnection)) instanceof ChainConveyorBlockEntity clbe2) {
				chainPosition = clbe.wrapAngle(stats.tangentAngle() + 180 + 2 * 35 * (clbe.reversed ? -1 : 1));
				ridingChainConveyor = clbe2.getBlockPos();
				ridingConnection = null;
			}

			return;
		}

		float prevChainPosition = chainPosition;
		chainPosition += serverSpeed * degreesPerTick;
		chainPosition = clbe.wrapAngle(chainPosition);

		BlockPos nearestLooking = BlockPos.ZERO;
		double bestDiff = Double.MAX_VALUE;
		for (BlockPos connection : clbe.connections) {
			double diff = Vec3.atLowerCornerOf(connection)
				.normalize()
				.distanceToSqr(mc.player.getLookAngle()
					.normalize());
			if (diff > bestDiff)
				continue;
			nearestLooking = connection;
			bestDiff = diff;
		}

		if (nearestLooking == BlockPos.ZERO)
			return;

		float offBranchAngle = clbe.connectionStats.get(nearestLooking)
			.tangentAngle();
		if (!clbe.loopThresholdCrossed(chainPosition, prevChainPosition, offBranchAngle))
			return;

		chainPosition = 0;
		ridingConnection = nearestLooking;
	}

}
