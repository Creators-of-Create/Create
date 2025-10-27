package com.simibubi.create.api.contraption.train;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.trains.track.AllPortalTracks;

import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A provider for portal track connections.
 * Takes a track inbound through a portal and finds the exit location for the outbound track.
 */
@FunctionalInterface
public interface PortalTrackProvider {
	SimpleRegistry<Block, PortalTrackProvider> REGISTRY = SimpleRegistry.create();

	/**
	 * Find the exit location for a track going through a portal.
	 * @param level the level of the inbound track
	 * @param face the face of the inbound track
	 */
	Exit findExit(ServerLevel level, BlockFace face);

	/**
	 * Checks if a given {@link BlockState} represents a supported portal block.
	 * @param state The block state to check.
	 * @return {@code true} if the block state represents a supported portal; {@code false} otherwise.
	 */
	static boolean isSupportedPortal(BlockState state) {
		return REGISTRY.get(state) != null;
	}

	/**
	 * Retrieves the corresponding outbound track on the other side of a portal.
	 * @param level        The current {@link ServerLevel}.
	 * @param inboundTrack The inbound track {@link BlockFace}.
	 * @return the found outbound track, or null if one wasn't found.
	 */
	@Nullable
	static Exit getOtherSide(ServerLevel level, BlockFace inboundTrack) {
		BlockPos portalPos = inboundTrack.getConnectedPos();
		BlockState portalState = level.getBlockState(portalPos);
		PortalTrackProvider provider = REGISTRY.get(portalState);
		return provider == null ? null : provider.findExit(level, inboundTrack);
	}

	/**
	 * Find an exit location by using an {@link Portal} instance.
	 * @param level              The level of the inbound track
	 * @param face				 The face of the inbound track
	 * @param firstDimension     The first dimension (typically the Overworld)
	 * @param secondDimension    The second dimension (e.g., Nether, Aether)
	 * @param portal 			 The portal
	 * @return A found exit, or null if one wasn't found
	 */
	static Exit fromPortal(ServerLevel level, BlockFace face, ResourceKey<Level> firstDimension,
							   ResourceKey<Level> secondDimension, Portal portal) {
		return AllPortalTracks.fromPortal(level, face, firstDimension, secondDimension, portal);
	}

	record Exit(ServerLevel level, BlockFace face) {
	}
}
