package com.simibubi.create.content.contraptions.components.structureMovement;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionInteractionPacket;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ContraptionHandlerClient {

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void preventRemotePlayersWalkingAnimations(PlayerTickEvent event) {
		if (event.phase == Phase.START)
			return;
		if (!(event.player instanceof RemotePlayer))
			return;
		RemotePlayer remotePlayer = (RemotePlayer) event.player;
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
	public static void rightClickingOnContraptionsGetsHandledLocally(ClickInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null)
			return;
		if (player.isPassenger())
			return;
		if (mc.level == null)
			return;
		if (!event.isUseItem())
			return;
		Vec3 origin = RaycastHelper.getTraceOrigin(player);

		double reach = mc.gameMode.getPickRange();
		if (mc.hitResult != null && mc.hitResult.getLocation() != null)
			reach = Math.min(mc.hitResult.getLocation()
				.distanceTo(origin), reach);

		Vec3 target = RaycastHelper.getTraceTarget(player, reach, origin);
		for (AbstractContraptionEntity contraptionEntity : mc.level
			.getEntitiesOfClass(AbstractContraptionEntity.class, new AABB(origin, target))) {

			Vec3 localOrigin = contraptionEntity.toLocalVector(origin, 1);
			Vec3 localTarget = contraptionEntity.toLocalVector(target, 1);
			Contraption contraption = contraptionEntity.getContraption();

			MutableObject<BlockHitResult> mutableResult = new MutableObject<>();
			PredicateTraceResult predicateResult = RaycastHelper.rayTraceUntil(localOrigin, localTarget, p -> {
				StructureBlockInfo blockInfo = contraption.getBlocks()
					.get(p);
				if (blockInfo == null)
					return false;
				BlockState state = blockInfo.state;
				VoxelShape raytraceShape = state.getShape(Minecraft.getInstance().level, BlockPos.ZERO.below());
				if (raytraceShape.isEmpty())
					return false;
				BlockHitResult rayTrace = raytraceShape.clip(localOrigin, localTarget, p);
				if (rayTrace != null) {
					mutableResult.setValue(rayTrace);
					return true;
				}
				return false;
			});

			if (predicateResult == null || predicateResult.missed())
				return;

			BlockHitResult rayTraceResult = mutableResult.getValue();
			InteractionHand hand = event.getHand();
			Direction face = rayTraceResult.getDirection();
			BlockPos pos = rayTraceResult.getBlockPos();

			if (!contraptionEntity.handlePlayerInteraction(player, pos, face, hand))
				return;
			AllPackets.channel.sendToServer(new ContraptionInteractionPacket(contraptionEntity, hand, pos, face));
			event.setCanceled(true);
			event.setSwingHand(false);
		}
	}

}
