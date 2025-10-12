package com.simibubi.create.api.behaviour.movement;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record ContraptionHandoffContainer(ListeningMovementBehaviour behaviour, ContraptionHandoffContext context) {

    public static ContraptionHandoffContainer of(@NotNull ListeningMovementBehaviour behaviour, @NotNull BlockState state, BlockEntity blockEntity, BlockPos structurePos, BlockPos realPos) {
        return new ContraptionHandoffContainer(behaviour, new ContraptionHandoffContext(state, blockEntity, structurePos, realPos));
    }

    public static record ContraptionHandoffContext(@NotNull BlockState state, BlockEntity blockEntity, BlockPos structurePos, BlockPos realPos) {
        @Override
        public final String toString() {
            String be = blockEntity == null ? " (No BlockEntity)" : (" '" + blockEntity.getClass().getSimpleName() + "'");
            return "ContraptionHandoffContext[" + state  + be + " at (" + realPos.getX() + ", " + realPos.getY() + ", " + realPos.getZ() + "), (" + structurePos.getX() + ", " + structurePos.getY() + ", " + structurePos.getZ() + ")";
        }
    }
}
