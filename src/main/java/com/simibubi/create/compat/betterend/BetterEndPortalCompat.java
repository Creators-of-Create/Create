package com.simibubi.create.compat.betterend;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

import com.simibubi.create.Create;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;

public class BetterEndPortalCompat {
	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	private static MethodHandle constructorHandle;
	private static VarHandle portalEntrancePosHandle;
	private static MethodHandle findDimensionEntryPointHandle;

	private static boolean hasErrored = false;

	static {
		try {
			Class<?> travelerStateClass = Class.forName("org.betterx.betterend.portal.TravelerState");
			MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(travelerStateClass, lookup);

			MethodType travelerStateConstructorTypes = MethodType.methodType(void.class, Entity.class);
			constructorHandle = lookup.findConstructor(travelerStateClass, travelerStateConstructorTypes);

			portalEntrancePosHandle = privateLookup.findVarHandle(travelerStateClass, "portalEntrancePos", BlockPos.class);

			MethodType findDimensionEntryPointTypes = MethodType.methodType(PortalInfo.class, ServerLevel.class);
			findDimensionEntryPointHandle = privateLookup.findVirtual(travelerStateClass, "findDimensionEntryPoint", findDimensionEntryPointTypes);
		} catch (Exception e) {
			Create.LOGGER.error("Create's Better End Portal compat failed to initialize: ", e);
			hasErrored = true;
		}
	}

	/**
	 * Retrieves the adjusted {@link PortalInfo} for the Better End portal using reflection.
	 *
	 * @param targetLevel The target {@link ServerLevel} (dimension).
	 * @param entity      The probe {@link Entity} used for portal traversal calculations.
	 * @return The adjusted {@link PortalInfo} for the target dimension, or {@code null} if an error occurs.
	 */
	public static PortalInfo getBetterEndPortalInfo(ServerLevel targetLevel, Entity entity) {
		if (!hasErrored) {
			try {
				Object travelerState = constructorHandle.invoke(entity);

				// Set the private portalEntrancePos field to the entity's block position
				// as assumed in TravelerState#findDimensionEntryPoint
				portalEntrancePosHandle.set(travelerState, entity.blockPosition().immutable());

				return (PortalInfo) findDimensionEntryPointHandle.invoke(travelerState, targetLevel);
			} catch (Throwable e) {
				Create.LOGGER.error("Create's Better End Portal compat failed to initialize: ", e);
			}
		}

		return null;
	}
}
