package com.simibubi.create.content.logistics.trains.track;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackNodeLocation.DiscoveredLocation;
import com.simibubi.create.content.logistics.trains.entity.TrainRelocator;
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TrackRemoval {

	static BlockPos startPos;
	static BlockPos hoveringPos;
	static Set<BlockPos> toRemove;
	
	// TODO localisation

	public static void sneakWrenched(BlockPos pos) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (startPos != null) {
			startPos = null;
			player.displayClientMessage(new TextComponent("Track removal aborted").withStyle(ChatFormatting.RED), true);
			return;
		}
		startPos = pos;
	}

	public static void wrenched(BlockPos pos) {
		if (startPos == null || hoveringPos == null || toRemove == null)
			return;
		if (TrainRelocator.isRelocating())
			return;
		AllPackets.channel.sendToServer(new TrackRemovalPacket(toRemove));
		startPos = null;
	}

	@OnlyIn(Dist.CLIENT)
	public static void clientTick() {
		if (startPos == null)
			return;

		LocalPlayer player = Minecraft.getInstance().player;
		ItemStack stack = player.getMainHandItem();
		HitResult hitResult = Minecraft.getInstance().hitResult;

		if (hitResult == null)
			return;
		if (hitResult.getType() != Type.BLOCK)
			return;

		if (!AllItems.WRENCH.isIn(stack)) {
			hoveringPos = null;
			startPos = null;
			player.displayClientMessage(new TextComponent("Track removal aborted").withStyle(ChatFormatting.RED), true);
			return;
		}

		BlockHitResult result = (BlockHitResult) hitResult;
		BlockPos blockPos = result.getBlockPos();
		Level level = player.level;
		BlockState blockState = level.getBlockState(blockPos);
		if (!(blockState.getBlock()instanceof ITrackBlock track)) {
			player.displayClientMessage(new TextComponent("Select a second track piece, Unequip Wrench to abort"),
				true);
			return;
		}

		if (blockPos.equals(hoveringPos)) {
			if (hoveringPos.equals(startPos)) {
				player.displayClientMessage(
					new TextComponent("Starting point selected. Right-Click a second Track piece"), true);
			} else if (toRemove == null) {
				player.displayClientMessage(new TextComponent("Starting point not reachable, Sneak-Click to abort")
					.withStyle(ChatFormatting.RED), true);
			} else
				player.displayClientMessage(new TextComponent("Right-Click to confirm").withStyle(ChatFormatting.GREEN),
					true);

			//

			return;
		}

		hoveringPos = blockPos;
		toRemove = new HashSet<>();

		List<BlockPos> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();

		if (search(level, hoveringPos, frontier, visited, 0)) {
			toRemove.add(hoveringPos);
			toRemove.add(startPos);
			return;
		}

		toRemove = null;
	}

	private static boolean search(Level level, BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited,
		int depth) {
		if (pos.equals(startPos))
			return true;
		if (depth > 32)
			return false;
		if (!visited.add(pos))
			return false;
		BlockState blockState = level.getBlockState(pos);
		if (!(blockState.getBlock()instanceof ITrackBlock track))
			return false;
		for (DiscoveredLocation discoveredLocation : track.getConnected(level, pos, blockState, false, null)) {
			for (BlockPos blockPos : discoveredLocation.allAdjacent()) {
				if (!search(level, blockPos, frontier, visited, depth + 1))
					continue;
				toRemove.add(pos);
				return true;
			}
		}
		return false;
	}

}
