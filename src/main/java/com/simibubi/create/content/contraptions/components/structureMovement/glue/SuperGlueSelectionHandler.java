package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.outliner.Outline.OutlineParams;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class SuperGlueSelectionHandler {

	private static final int SUCCESS = 0x68c586;
	private static final int FAIL = 0xc5b548;

	private Object clusterOutlineSlot = new Object();
	private Object bbOutlineSlot = new Object();

	private BlockPos firstPos;
	private BlockPos hoveredPos;
	private Set<BlockPos> currentCluster;
	private int glueRequired;

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

		HitResult hitResult = mc.hitResult;
		if (hitResult != null && hitResult.getType() == Type.BLOCK)
			hovered = ((BlockHitResult) hitResult).getBlockPos();

		if (hovered == null) {
			hoveredPos = null;
			return;
		}

		if (firstPos != null && !firstPos.closerThan(hovered, 24)) {
			Lang.sendStatus(player, FAIL, "super_glue.too_far");
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

				if (!canReach)
					Lang.sendStatus(player, FAIL, "super_glue.cannot_reach");
				else if (!canAfford)
					Lang.sendStatus(player, FAIL, "super_glue.not_enough");
				else if (cancel)
					Lang.sendStatus(player, FAIL, "super_glue.click_to_discard");
				else
					Lang.sendStatus(player, SUCCESS, "super_glue.click_to_confirm");

				CreateClient.OUTLINER.showCluster(clusterOutlineSlot, currentCluster)
					.colored(canReach && canAfford && !cancel ? SUCCESS : FAIL)
					.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
					.lineWidth(1 / 16f);
			}
			if (currentSelectionBox != null) {
				OutlineParams params =
					firstPos == null ? CreateClient.OUTLINER.showAABB(bbOutlineSlot, currentSelectionBox)
						: CreateClient.OUTLINER.chaseAABB(bbOutlineSlot, currentSelectionBox);
				params.colored(0x111111)
					.disableNormals()
					.lineWidth(1 / 128f);
			}
			return;
		}

		hoveredPos = hovered;

		Pair<Set<BlockPos>, List<BlockFace>> pair =
			SuperGlueSelectionHelper.searchGlueGroup(mc.level, firstPos, hoveredPos);

		currentCluster = pair == null ? null : pair.getFirst();
		glueRequired = pair == null ? 0
			: pair.getSecond()
				.size();
	}

	private boolean isGlue(ItemStack stack) {
		return stack.getItem() instanceof SuperGlueItem;
	}

	private AABB getCurrentSelectionBox() {
		return firstPos == null || hoveredPos == null ? null : new AABB(firstPos, hoveredPos).expandTowards(1, 1, 1);
	}

	public boolean onMouseInput() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;

		if (!isGlue(player.getMainHandItem()))
			return false;

		if (player.isSteppingCarefully()) {
			if (firstPos != null) {
				discard();
				return true;
			}
			return false;
		}

		if (hoveredPos == null)
			return false;

		if (mc.hitResult instanceof BlockHitResult bhr) {
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
		Lang.sendStatus(player, "super_glue.first_pos");
		return true;
	}

	public void discard() {
		LocalPlayer player = Minecraft.getInstance().player;
		currentCluster = null;
		firstPos = null;
		Lang.sendStatus(player, "super_glue.abort");
	}

	public void confirm() {
		LocalPlayer player = Minecraft.getInstance().player;
		AllPackets.channel.sendToServer(new SuperGlueSelectionPacket(firstPos, hoveredPos));
		discard();
		Lang.sendStatus(player, "super_glue.sucess");
	}

}
