package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.cache.Cache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget;
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetSelectionHandler;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.TickBasedCache;

import net.createmod.catnip.data.WorldAttached;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.Tags;

@EventBusSubscriber(Dist.CLIENT)
public class ChainConveyorInteractionHandler {

	public static WorldAttached<Cache<BlockPos, List<ChainConveyorShape>>> loadedChains =
		new WorldAttached<>($ -> new TickBasedCache<>(60, true));

	public static BlockPos selectedLift;
	public static float selectedChainPosition;
	public static BlockPos selectedConnection;
	public static Vec3 selectedBakedPosition;
	public static ChainConveyorShape selectedShape;

	public static void clientTick() {
		if (!isActive()) {
			selectedLift = null;
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		boolean isWrench = mc.player.isHolding(i -> i.is(Tags.Items.TOOLS_WRENCH));
		boolean dismantling = isWrench && mc.player.isShiftKeyDown();
		double range = mc.player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;

		Vec3 from = mc.player.getEyePosition();
		Vec3 to = RaycastHelper.getTraceTarget(mc.player, range, from);
		HitResult hitResult = mc.hitResult;

		double bestDiff = Float.MAX_VALUE;
		if (hitResult != null)
			bestDiff = hitResult.getLocation()
				.distanceToSqr(from);

		BlockPos bestLift = null;
		ChainConveyorShape bestShape = null;
		selectedConnection = null;

		for (Entry<BlockPos, List<ChainConveyorShape>> entry : loadedChains.get(Minecraft.getInstance().level)
			.asMap()
			.entrySet()) {
			BlockPos liftPos = entry.getKey();
			for (ChainConveyorShape chainConveyorShape : entry.getValue()) {
				if (chainConveyorShape instanceof ChainConveyorShape.ChainConveyorBB && dismantling)
					continue;
				Vec3 liftVec = Vec3.atLowerCornerOf(liftPos);
				Vec3 intersect = chainConveyorShape.intersect(from.subtract(liftVec), to.subtract(liftVec));
				if (intersect == null)
					continue;

				double distanceToSqr = intersect.add(liftVec)
					.distanceToSqr(from);
				if (distanceToSqr > bestDiff)
					continue;
				bestDiff = distanceToSqr;
				bestLift = liftPos;
				bestShape = chainConveyorShape;
				selectedChainPosition = chainConveyorShape.getChainPosition(intersect);
				if (chainConveyorShape instanceof ChainConveyorShape.ChainConveyorOBB obb)
					selectedConnection = obb.connection;
			}
		}

		selectedLift = bestLift;
		if (bestLift == null)
			return;

		selectedShape = bestShape;
		selectedBakedPosition = bestShape.getVec(bestLift, selectedChainPosition);

		if (!isWrench) {
			Outliner.getInstance()
				.chaseAABB("ChainPointSelection", new AABB(selectedBakedPosition, selectedBakedPosition))
				.colored(Color.WHITE)
				.lineWidth(1 / 6f)
				.disableLineNormals();
		}
	}

	private static boolean isActive() {
		Minecraft mc = Minecraft.getInstance();
		ItemStack mainHandItem = mc.player.getMainHandItem();
		return mc.player.isHolding(AllItemTags.CHAIN_RIDEABLE::matches) || AllBlocks.PACKAGE_FROGPORT.isIn(mainHandItem)
			|| PackageItem.isPackage(mainHandItem);
	}

	public static boolean onUse() {
		if (selectedLift == null)
			return false;

		Minecraft mc = Minecraft.getInstance();
		ItemStack mainHandItem = mc.player.getMainHandItem();

		if (mc.player.isHolding(AllItemTags.CHAIN_RIDEABLE::matches)) {
			ItemStack offHandItem = mc.player.getOffhandItem();
			ItemStack usedItem = AllItemTags.CHAIN_RIDEABLE.matches(mainHandItem) ? mainHandItem : offHandItem;

			if (!mc.player.isShiftKeyDown()) {
				ChainConveyorRidingHandler.embark(selectedLift, selectedChainPosition, selectedConnection);
				return true;
			}

			CatnipServices.NETWORK.sendToServer(new ChainConveyorConnectionPacket(selectedLift, selectedLift.offset(selectedConnection),
				usedItem, false));
			return true;
		}

		if (AllBlocks.PACKAGE_FROGPORT.isIn(mainHandItem)) {
			PackagePortTargetSelectionHandler.exactPositionOfTarget = selectedBakedPosition;
			PackagePortTargetSelectionHandler.activePackageTarget =
				new PackagePortTarget.ChainConveyorFrogportTarget(selectedLift, selectedChainPosition, selectedConnection, false);
			return true;
		}

		if (PackageItem.isPackage(mainHandItem)) {
			CatnipServices.NETWORK.sendToServer(new ChainPackageInteractionPacket(selectedLift, selectedConnection, selectedChainPosition,
				false));
			return true;
		}

		return true;
	}

	public static void drawCustomBlockSelection(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
		if (selectedLift == null || selectedShape == null)
			return;

		VertexConsumer vb = buffer.getBuffer(RenderType.lines());
		ms.pushPose();
		ms.translate(selectedLift.getX() - camera.x, selectedLift.getY() - camera.y, selectedLift.getZ() - camera.z);
		selectedShape.drawOutline(selectedLift, ms, vb);
		ms.popPose();
	}

	@SubscribeEvent
	public static void hideVanillaBlockSelection(RenderHighlightEvent.Block event) {
		if (selectedLift == null || selectedShape == null)
			return;

		event.setCanceled(true);
	}

}
