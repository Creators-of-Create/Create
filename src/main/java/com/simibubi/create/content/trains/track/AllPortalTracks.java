package com.simibubi.create.content.trains.track;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.foundation.utility.AttachedRegistry;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.registries.ForgeRegistries;

public class AllPortalTracks {

	// Portals must be entered from the side and must lead to a different dimension
	// than the one entered from

	@FunctionalInterface
	public interface PortalTrackProvider extends UnaryOperator<Pair<ServerLevel, BlockFace>> {
	};

	private static final AttachedRegistry<Block, PortalTrackProvider> PORTAL_BEHAVIOURS =
		new AttachedRegistry<>(ForgeRegistries.BLOCKS);

	public static void registerIntegration(ResourceLocation block, PortalTrackProvider provider) {
		PORTAL_BEHAVIOURS.register(block, provider);
	}

	public static void registerIntegration(Block block, PortalTrackProvider provider) {
		PORTAL_BEHAVIOURS.register(block, provider);
	}

	public static boolean isSupportedPortal(BlockState state) {
		return PORTAL_BEHAVIOURS.get(state.getBlock()) != null;
	}

	public static Pair<ServerLevel, BlockFace> getOtherSide(ServerLevel level, BlockFace inboundTrack) {
		BlockPos portalPos = inboundTrack.getConnectedPos();
		BlockState portalState = level.getBlockState(portalPos);
		PortalTrackProvider provider = PORTAL_BEHAVIOURS.get(portalState.getBlock());
		return provider == null ? null : provider.apply(Pair.of(level, inboundTrack));
	}

	// Builtin handlers

	public static void registerDefaults() {
		registerIntegration(Blocks.NETHER_PORTAL, AllPortalTracks::nether);
		registerIntegration(new ResourceLocation("aether", "aether_portal"), AllPortalTracks::aether);
	}

	private static Pair<ServerLevel, BlockFace> nether(Pair<ServerLevel, BlockFace> inbound) {
		return standardPortalProvider(inbound, Level.OVERWORLD, Level.NETHER, ServerLevel::getPortalForcer);
	}

	private static Pair<ServerLevel, BlockFace> aether(Pair<ServerLevel, BlockFace> inbound) {
		ResourceKey<Level> aetherLevelKey =
			ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("aether", "the_aether"));
		return standardPortalProvider(inbound, Level.OVERWORLD, aetherLevelKey, level -> {
			try {
				return (ITeleporter) Class.forName("com.aetherteam.aether.block.portal.AetherPortalForcer")
					.getDeclaredConstructor(ServerLevel.class, boolean.class)
					.newInstance(level, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return level.getPortalForcer();
		});
	}

	public static Pair<ServerLevel, BlockFace> standardPortalProvider(Pair<ServerLevel, BlockFace> inbound,
		ResourceKey<Level> firstDimension, ResourceKey<Level> secondDimension,
		Function<ServerLevel, ITeleporter> customPortalForcer) {
		ServerLevel level = inbound.getFirst();
		ResourceKey<Level> resourcekey = level.dimension() == secondDimension ? firstDimension : secondDimension;
		MinecraftServer minecraftserver = level.getServer();
		ServerLevel otherLevel = minecraftserver.getLevel(resourcekey);

		if (otherLevel == null || !minecraftserver.isNetherEnabled())
			return null;

		BlockFace inboundTrack = inbound.getSecond();
		BlockPos portalPos = inboundTrack.getConnectedPos();
		BlockState portalState = level.getBlockState(portalPos);
		ITeleporter teleporter = customPortalForcer.apply(otherLevel);

		SuperGlueEntity probe = new SuperGlueEntity(level, new AABB(portalPos));
		probe.setYRot(inboundTrack.getFace()
			.toYRot());
		probe.setPortalEntrancePos();

		PortalInfo portalinfo = teleporter.getPortalInfo(probe, otherLevel, probe::findDimensionEntryPoint);
		if (portalinfo == null)
			return null;

		BlockPos otherPortalPos = new BlockPos(portalinfo.pos);
		BlockState otherPortalState = otherLevel.getBlockState(otherPortalPos);
		if (otherPortalState.getBlock() != portalState.getBlock())
			return null;

		Direction targetDirection = inboundTrack.getFace();
		if (targetDirection.getAxis() == otherPortalState.getValue(BlockStateProperties.HORIZONTAL_AXIS))
			targetDirection = targetDirection.getClockWise();
		BlockPos otherPos = otherPortalPos.relative(targetDirection);
		return Pair.of(otherLevel, new BlockFace(otherPos, targetDirection.getOpposite()));
	}

}
