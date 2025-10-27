package com.simibubi.create.content.kinetics.mechanicalArm;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ArmInteractionPointType {
	private static final List<ArmInteractionPointType> SORTED_TYPES = new ReferenceArrayList<>();
	@UnmodifiableView
	public static final List<ArmInteractionPointType> SORTED_TYPES_VIEW = Collections.unmodifiableList(SORTED_TYPES);

	@Internal
	public static void init() {
		SORTED_TYPES.clear();
		CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.forEach(SORTED_TYPES::add);
		SORTED_TYPES.sort((t1, t2) -> t2.getPriority() - t1.getPriority());
	}

	@Nullable
	public static ArmInteractionPointType getPrimaryType(Level level, BlockPos pos, BlockState state) {
		for (ArmInteractionPointType type : SORTED_TYPES_VIEW)
			if (type.canCreatePoint(level, pos, state))
				return type;
		return null;
	}

	public abstract boolean canCreatePoint(Level level, BlockPos pos, BlockState state);

	@Nullable
	public abstract ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state);

	public int getPriority() {
		return 0;
	}
}
