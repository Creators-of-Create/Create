package com.simibubi.create.content.kinetics.mechanicalArm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ArmInteractionPointType {

	private static final Map<ResourceLocation, ArmInteractionPointType> TYPES = new HashMap<>();
	private static final List<ArmInteractionPointType> SORTED_TYPES = new ArrayList<>();

	protected final ResourceLocation id;

	public ArmInteractionPointType(ResourceLocation id) {
		this.id = id;
	}

	public static void register(ArmInteractionPointType type) {
		ResourceLocation id = type.getId();
		if (TYPES.containsKey(id))
			throw new IllegalArgumentException("Tried to override ArmInteractionPointType registration for id '" + id + "'. This is not supported!");
		TYPES.put(id, type);
		SORTED_TYPES.add(type);
		SORTED_TYPES.sort((t1, t2) -> t2.getPriority() - t1.getPriority());
	}

	@Nullable
	public static ArmInteractionPointType get(ResourceLocation id) {
		return TYPES.get(id);
	}

	public static void forEach(Consumer<ArmInteractionPointType> action) {
		SORTED_TYPES.forEach(action);
	}

	@Nullable
	public static ArmInteractionPointType getPrimaryType(Level level, BlockPos pos, BlockState state) {
		for (ArmInteractionPointType type : SORTED_TYPES)
			if (type.canCreatePoint(level, pos, state))
				return type;
		return null;
	}

	public final ResourceLocation getId() {
		return id;
	}

	public abstract boolean canCreatePoint(Level level, BlockPos pos, BlockState state);

	@Nullable
	public abstract ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state);

	public int getPriority() {
		return 0;
	}

}
