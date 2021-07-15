package com.simibubi.create.content.contraptions.components.structureMovement;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionInteractionPacket;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
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
		if (!(event.player instanceof RemoteClientPlayerEntity))
			return;
		RemoteClientPlayerEntity remotePlayer = (RemoteClientPlayerEntity) event.player;
		CompoundNBT data = remotePlayer.getPersistentData();
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
		ClientPlayerEntity player = mc.player;
		if (player == null)
			return;
		if (player.isPassenger())
			return;
		if (mc.level == null)
			return;
		if (!event.isUseItem())
			return;
		Vector3d origin = RaycastHelper.getTraceOrigin(player);

		double reach = mc.gameMode.getPickRange();
		if (mc.hitResult != null && mc.hitResult.getLocation() != null)
			reach = Math.min(mc.hitResult.getLocation()
				.distanceTo(origin), reach);

		Vector3d target = RaycastHelper.getTraceTarget(player, reach, origin);
		for (AbstractContraptionEntity contraptionEntity : mc.level
			.getEntitiesOfClass(AbstractContraptionEntity.class, new AxisAlignedBB(origin, target))) {

			Vector3d localOrigin = contraptionEntity.toLocalVector(origin, 1);
			Vector3d localTarget = contraptionEntity.toLocalVector(target, 1);
			Contraption contraption = contraptionEntity.getContraption();

			MutableObject<BlockRayTraceResult> mutableResult = new MutableObject<>();
			PredicateTraceResult predicateResult = RaycastHelper.rayTraceUntil(localOrigin, localTarget, p -> {
				BlockInfo blockInfo = contraption.getBlocks()
					.get(p);
				if (blockInfo == null)
					return false;
				BlockState state = blockInfo.state;
				VoxelShape raytraceShape = state.getShape(Minecraft.getInstance().level, BlockPos.ZERO.below());
				if (raytraceShape.isEmpty())
					return false;
				BlockRayTraceResult rayTrace = raytraceShape.clip(localOrigin, localTarget, p);
				if (rayTrace != null) {
					mutableResult.setValue(rayTrace);
					return true;
				}
				return false;
			});

			if (predicateResult == null || predicateResult.missed())
				return;

			BlockRayTraceResult rayTraceResult = mutableResult.getValue();
			Hand hand = event.getHand();
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
