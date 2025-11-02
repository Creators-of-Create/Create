package com.simibubi.create.content.contraptions;

import java.lang.ref.WeakReference;
import java.util.Collection;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.sync.ContraptionInteractionPacket;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;

import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ContraptionHandlerClient {

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void preventRemotePlayersWalkingAnimations(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof RemotePlayer remotePlayer))
			return;

		CompoundTag data = remotePlayer.getPersistentData();
		if (!data.contains("LastOverrideLimbSwingUpdate"))
			return;

		int lastOverride = data.getInt("LastOverrideLimbSwingUpdate");
		data.putInt("LastOverrideLimbSwingUpdate", lastOverride + 1);
		if (lastOverride > 5) {
			data.remove("LastOverrideLimbSwingUpdate");
			data.remove("OverrideLimbSwing");
			return;
		}

		float limbSwing = data.getFloat("OverrideLimbSwing");
		remotePlayer.xo = remotePlayer.getX() - (limbSwing / 4);
		remotePlayer.zo = remotePlayer.getZ();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void rightClickingOnContraptionsGetsHandledLocally(InputEvent.InteractionKeyMappingTriggered event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (player == null)
			return;
		if (player.isSpectator())
			return;
		if (mc.level == null)
			return;
		if (!event.isUseItem())
			return;

		Couple<Vec3> rayInputs = getRayInputs(player);
		Vec3 origin = rayInputs.getFirst();
		Vec3 target = rayInputs.getSecond();
		AABB aabb = new AABB(origin, target).inflate(16);

		Collection<WeakReference<AbstractContraptionEntity>> contraptions =
			ContraptionHandler.loadedContraptions.get(mc.level)
				.values();

		double bestDistance = Double.MAX_VALUE;
		BlockHitResult bestResult = null;
		AbstractContraptionEntity bestEntity = null;

		for (WeakReference<AbstractContraptionEntity> ref : contraptions) {
			AbstractContraptionEntity contraptionEntity = ref.get();
			if (contraptionEntity == null)
				continue;
			if (!contraptionEntity.getBoundingBox()
				.intersects(aabb))
				continue;

			BlockHitResult rayTraceResult = rayTraceContraption(origin, target, contraptionEntity);
			if (rayTraceResult == null)
				continue;

			double distance = contraptionEntity.toGlobalVector(rayTraceResult.getLocation(), 1).distanceTo(origin);
			if (distance > bestDistance)
				continue;

			bestResult = rayTraceResult;
			bestDistance = distance;
			bestEntity = contraptionEntity;
		}

		if (bestResult == null)
			return;

		InteractionHand hand = event.getHand();
		Direction face = bestResult.getDirection();
		BlockPos pos = bestResult.getBlockPos();

		if (bestEntity.handlePlayerInteraction(player, pos, face, hand)) {
			CatnipServices.NETWORK.sendToServer(new ContraptionInteractionPacket(bestEntity, hand, pos, face));
		} else
			handleSpecialInteractions(bestEntity, player, pos, face, hand);

		event.setCanceled(true);
		event.setSwingHand(false);
	}

	private static boolean handleSpecialInteractions(AbstractContraptionEntity contraptionEntity, Player player,
													 BlockPos localPos, Direction side, InteractionHand interactionHand) {
		if (AllItems.WRENCH.isIn(player.getItemInHand(interactionHand))
			&& contraptionEntity instanceof CarriageContraptionEntity car)
			return TrainRelocator.carriageWrenched(car.toGlobalVector(VecHelper.getCenterOf(localPos), 1), car);
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public static Couple<Vec3> getRayInputs(LocalPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		Vec3 origin = player.getEyePosition();
		double reach = player.blockInteractionRange();
		if (mc.hitResult != null && mc.hitResult.getLocation() != null)
			reach = Math.min(mc.hitResult.getLocation()
				.distanceTo(origin), reach);
		Vec3 target = RaycastHelper.getTraceTarget(player, reach, origin);
		return Couple.create(origin, target);
	}

	@Nullable
	public static BlockHitResult rayTraceContraption(Vec3 origin, Vec3 target,
													 AbstractContraptionEntity contraptionEntity) {
		Vec3 localOrigin = contraptionEntity.toLocalVector(origin, 1);
		Vec3 localTarget = contraptionEntity.toLocalVector(target, 1);
		Contraption contraption = contraptionEntity.getContraption();

		MutableObject<BlockHitResult> mutableResult = new MutableObject<>();
		PredicateTraceResult predicateResult = RaycastHelper.rayTraceUntil(localOrigin, localTarget, p -> {
			for (Direction d : Iterate.directions) {
				if (d == Direction.UP)
					continue;
				BlockPos pos = d == Direction.DOWN ? p : p.relative(d);
				StructureBlockInfo blockInfo = contraption.getBlocks()
					.get(pos);
				if (blockInfo == null)
					continue;
				BlockState state = blockInfo.state();
				VoxelShape raytraceShape = state.getShape(contraption.getContraptionWorld(), BlockPos.ZERO.below());
				if (raytraceShape.isEmpty())
					continue;
				if (contraption.isHiddenInPortal(pos))
					continue;
				BlockHitResult rayTrace = raytraceShape.clip(localOrigin, localTarget, pos);
				if (rayTrace != null) {
					mutableResult.setValue(rayTrace);
					return true;
				}
			}
			return false;
		});

		if (predicateResult == null || predicateResult.missed())
			return null;

		BlockHitResult rayTraceResult = mutableResult.getValue();
		return rayTraceResult;
	}

}
