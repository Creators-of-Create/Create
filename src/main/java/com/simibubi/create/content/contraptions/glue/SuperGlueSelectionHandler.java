package com.simibubi.create.content.contraptions.glue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Objects;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.chassis.AbstractChassisBlock;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;

public class SuperGlueSelectionHandler {

	private static final int PASSIVE = 0x4D9162;
	private static final int HIGHLIGHT = 0x68c586;
	private static final int FAIL = 0xc5b548;

	private Object clusterOutlineSlot = new Object();
	private Object bbOutlineSlot = new Object();
	private int clusterCooldown;

	private BlockPos firstPos;
	private BlockPos hoveredPos;
	private Set<BlockPos> currentCluster;
	private int glueRequired;

	private SuperGlueEntity selected;
	private BlockPos soundSourceForRemoval;

	public void tick() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		BlockPos hovered = null;
		ItemStack stack = player.getMainHandItem();

		if (!isGlue(stack)) {
			if (firstPos != null)
				discard();
			return;
		}

		if (clusterCooldown > 0) {
			if (clusterCooldown == 25)
				player.displayClientMessage(Components.immutableEmpty(), true);
			CreateClient.OUTLINER.keep(clusterOutlineSlot);
			clusterCooldown--;
		}

		AABB scanArea = player.getBoundingBox()
			.inflate(32, 16, 32);

		List<SuperGlueEntity> glueNearby = mc.level.getEntitiesOfClass(SuperGlueEntity.class, scanArea);

		selected = null;
		if (firstPos == null) {
			double range = player.getAttribute(ForgeMod.REACH_DISTANCE.get())
				.getValue() + 1;
			Vec3 traceOrigin = RaycastHelper.getTraceOrigin(player);
			Vec3 traceTarget = RaycastHelper.getTraceTarget(player, range, traceOrigin);

			double bestDistance = Double.MAX_VALUE;
			for (SuperGlueEntity glueEntity : glueNearby) {
				Optional<Vec3> clip = glueEntity.getBoundingBox()
					.clip(traceOrigin, traceTarget);
				if (clip.isEmpty())
					continue;
				Vec3 vec3 = clip.get();
				double distanceToSqr = vec3.distanceToSqr(traceOrigin);
				if (distanceToSqr > bestDistance)
					continue;
				selected = glueEntity;
				soundSourceForRemoval = new BlockPos(vec3);
				bestDistance = distanceToSqr;
			}

			for (SuperGlueEntity glueEntity : glueNearby) {
				boolean h = clusterCooldown == 0 && glueEntity == selected;
				AllSpecialTextures faceTex = h ? AllSpecialTextures.GLUE : null;
				CreateClient.OUTLINER.showAABB(glueEntity, glueEntity.getBoundingBox())
					.colored(h ? HIGHLIGHT : PASSIVE)
					.withFaceTextures(faceTex, faceTex)
					.disableLineNormals()
					.lineWidth(h ? 1 / 16f : 1 / 64f);
			}
		}

		HitResult hitResult = mc.hitResult;
		if (hitResult != null && hitResult.getType() == Type.BLOCK)
			hovered = ((BlockHitResult) hitResult).getBlockPos();

		if (hovered == null) {
			hoveredPos = null;
			return;
		}

		if (firstPos != null && !firstPos.closerThan(hovered, 24)) {
			Lang.translate("super_glue.too_far")
				.color(FAIL)
				.sendStatus(player);
			return;
		}

		boolean cancel = player.isSteppingCarefully();
		if (cancel && firstPos == null)
			return;

		AABB currentSelectionBox = getCurrentSelectionBox();

		boolean unchanged = Objects.equal(hovered, hoveredPos);

		if (unchanged) {
			if (currentCluster != null) {
				boolean canReach = currentCluster.contains(hovered);
				boolean canAfford = SuperGlueSelectionHelper.collectGlueFromInventory(player, glueRequired, true);
				int color = HIGHLIGHT;
				String key = "super_glue.click_to_confirm";

				if (!canReach) {
					color = FAIL;
					key = "super_glue.cannot_reach";
				} else if (!canAfford) {
					color = FAIL;
					key = "super_glue.not_enough";
				} else if (cancel) {
					color = FAIL;
					key = "super_glue.click_to_discard";
				}

				Lang.translate(key)
					.color(color)
					.sendStatus(player);

				if (currentSelectionBox != null)
					CreateClient.OUTLINER.showAABB(bbOutlineSlot, currentSelectionBox)
						.colored(canReach && canAfford && !cancel ? HIGHLIGHT : FAIL)
						.withFaceTextures(AllSpecialTextures.GLUE, AllSpecialTextures.GLUE)
						.disableLineNormals()
						.lineWidth(1 / 16f);

				CreateClient.OUTLINER.showCluster(clusterOutlineSlot, currentCluster)
					.colored(0x4D9162)
					.disableLineNormals()
					.lineWidth(1 / 64f);
			}

			return;
		}

		hoveredPos = hovered;

		Set<BlockPos> cluster = SuperGlueSelectionHelper.searchGlueGroup(mc.level, firstPos, hoveredPos, true);
		currentCluster = cluster;
		glueRequired = 1;
	}

	private boolean isGlue(ItemStack stack) {
		return stack.getItem() instanceof SuperGlueItem;
	}

	private AABB getCurrentSelectionBox() {
		return firstPos == null || hoveredPos == null ? null : new AABB(firstPos, hoveredPos).expandTowards(1, 1, 1);
	}

	public boolean onMouseInput(boolean attack) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;

		if (!isGlue(player.getMainHandItem()))
			return false;
		if (!player.mayBuild())
			return false;

		if (attack) {
			if (selected == null)
				return false;
			AllPackets.getChannel().sendToServer(new SuperGlueRemovalPacket(selected.getId(), soundSourceForRemoval));
			selected = null;
			clusterCooldown = 0;
			return true;
		}

		if (player.isSteppingCarefully()) {
			if (firstPos != null) {
				discard();
				return true;
			}
			return false;
		}

		if (hoveredPos == null)
			return false;

		Direction face = null;
		if (mc.hitResult instanceof BlockHitResult bhr) {
			face = bhr.getDirection();
			BlockState blockState = level.getBlockState(hoveredPos);
			if (blockState.getBlock()instanceof AbstractChassisBlock cb)
				if (cb.getGlueableSide(blockState, bhr.getDirection()) != null)
					return false;
		}

		if (firstPos != null && currentCluster != null) {
			boolean canReach = currentCluster.contains(hoveredPos);
			boolean canAfford = SuperGlueSelectionHelper.collectGlueFromInventory(player, glueRequired, true);

			if (!canReach || !canAfford)
				return true;

			confirm();
			return true;
		}

		firstPos = hoveredPos;
		if (face != null)
			SuperGlueItem.spawnParticles(level, firstPos, face, true);
		Lang.translate("super_glue.first_pos")
			.sendStatus(player);
		AllSoundEvents.SLIME_ADDED.playAt(level, firstPos, 0.5F, 0.85F, false);
		level.playSound(player, firstPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
		return true;
	}

	public void discard() {
		LocalPlayer player = Minecraft.getInstance().player;
		currentCluster = null;
		firstPos = null;
		Lang.translate("super_glue.abort")
			.sendStatus(player);
		clusterCooldown = 0;
	}

	public void confirm() {
		LocalPlayer player = Minecraft.getInstance().player;
		AllPackets.getChannel().sendToServer(new SuperGlueSelectionPacket(firstPos, hoveredPos));
		AllSoundEvents.SLIME_ADDED.playAt(player.level, hoveredPos, 0.5F, 0.95F, false);
		player.level.playSound(player, hoveredPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);

		if (currentCluster != null)
			CreateClient.OUTLINER.showCluster(clusterOutlineSlot, currentCluster)
				.colored(0xB5F2C6)
				.withFaceTextures(AllSpecialTextures.GLUE, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.disableLineNormals()
				.lineWidth(1 / 24f);

		discard();
		Lang.translate("super_glue.success")
			.sendStatus(player);
		clusterCooldown = 40;
	}

}
