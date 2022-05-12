package com.simibubi.create.foundation.utility;

import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;


public class GlueHelper {

    /**
     * Applies glue everywhere in a cuboid
     *
     * @param axes axes to apply the glue on.
     * @return number of applied glue patches
     * TODO: skip exposed faces - when in water, when gluing things that are not glueable etc.
     */
    public static int fill(Level level, BlockPos pos1, BlockPos pos2, @NotNull EnumSet<Direction.Axis> axes) {
        var area = BoundingBox.fromCorners(pos1, pos2);
        var glued = 0;

        for (var glueAxis : new Direction.Axis[]{Direction.Axis.Y, Direction.Axis.Z, Direction.Axis.X}) {
            if (!axes.contains(glueAxis)) continue; // if axis specified - apply only for this one
            var dir = Direction.get(Direction.AxisDirection.POSITIVE, glueAxis);

            for (var pos : BlockPos.betweenClosed(pos1, pos2)) {
                var targetPos = pos.relative(dir.getOpposite());
                if (!area.isInside(targetPos)) continue; // neighboring block inside area

                SuperGlueEntity entity = new SuperGlueEntity(level, pos.immutable(), dir);
                if (!entity.onValidSurface()) continue;

                ++glued;
                level.addFreshEntity(entity);
            }
        }
        return glued;
    }

    /**
     * Removes glue in a cuboid.
     *
     * @param axis optional, to clear only glue on in one axis;
     * @return number of removed glue patches
     */
    public static int clear(Level level, BlockPos pos1, BlockPos pos2, @NotNull EnumSet<Direction.Axis> axes) {
        int removed = 0;
        for (var glueEntity : level.getEntities(
            EntityTypeTest.forClass(SuperGlueEntity.class),
            new AABB(pos1, pos2).inflate(0.5),
            e -> axes.contains(e.getFacingDirection().getAxis()))
        ) {
            glueEntity.setRemoved(Entity.RemovalReason.KILLED);
            ++removed;
        }
        return removed;
    }

    /**
     * Floodifill the glue propagating it from source block. BlockPredicate
     *
     * @param source source block to start from
     * @param limit  max number of glue patches to apply
     * @param mask mask predicate to glue only blocks that match it
     * @return
     */
    public static int floodfill(Level level, BlockPos source, int limit, @Nullable Predicate<BlockInWorld> mask) {
        // FIXME: this code and sail code are both BFS-order traversals. Maybe they should be refactored to one algo?
        var frontier = new ArrayDeque<>(List.of(source));
        var visited = new ArrayDeque<>(List.of(source));
        int glued = 0;

        while (!frontier.isEmpty()) {
            final var pos = frontier.pop();

            for (var dir : Iterate.directions) {

                final BlockPos neighbor = pos.relative(dir);
                if (visited.contains(neighbor)) continue;
                if (mask != null && !mask.test(new BlockInWorld(level, neighbor, true))) {
                    visited.add(neighbor);
                    continue;
                }

                if (level.getBlockState(neighbor).isAir()) {
                    visited.add(neighbor);
                    continue;
                }
                SuperGlueEntity entity = new SuperGlueEntity(level, pos.immutable(), dir.getOpposite());
                if (!entity.onValidSurface()) continue;

                if (++glued >= limit) return glued;
                level.addFreshEntity(entity);

                var nextPos = pos.relative(dir);
                frontier.add(nextPos);
                visited.add(nextPos);
            }
        }
        return glued;
    }
}
