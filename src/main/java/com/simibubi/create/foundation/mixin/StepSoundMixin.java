package com.simibubi.create.foundation.mixin;

import com.simibubi.create.content.contraptions.components.structureMovement.*;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.Reference;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Entity.class)
public abstract class StepSoundMixin {

    @Shadow public boolean collided;

    @Shadow public World world;

    @Shadow public abstract BlockPos getPosition();

    @Shadow public abstract Vec3d getPositionVec();

    @Shadow private float nextStepDistance;

    @Shadow protected abstract float determineNextStepDistance();

    @Shadow public abstract AxisAlignedBB getBoundingBox();

    @Shadow protected abstract void playStepSound(BlockPos p_180429_1_, BlockState p_180429_2_);

    @Inject(at = @At(
            value = "JUMP",
            opcode = 154, //IFNE
            ordinal = 4
            ),
            method = "move"
    )
    private void movementMixin(MoverType mover, Vec3d movement, CallbackInfo ci) {
        Entity thi = (Entity) (Object) this;

        World entityWorld = world;

        Set<AbstractContraptionEntity> contraptions = ContraptionHandler.loadedContraptions.get(entityWorld)
                                                                                               .values()
                                                                                               .stream()
                                                                                               .map(Reference::get)
                                                                                               .filter(cEntity -> cEntity != null && cEntity.collidingEntities.containsKey(thi)).collect(Collectors.toSet());

        contraptions.addAll(entityWorld.getEntitiesWithinAABB(AbstractContraptionEntity.class, getBoundingBox().grow(1f)));

        Vec3d worldPos = thi.getPositionVector().add(0, -0.2, 0);

        boolean stepped = false;
        for (AbstractContraptionEntity cEntity : contraptions) {
            Vec3d localPos = ContraptionCollider.getWorldToLocalTranslation(worldPos, cEntity);

            localPos = worldPos.add(localPos);

            BlockPos blockPos = new BlockPos(localPos);
            Contraption contraption = cEntity.getContraption();
            Template.BlockInfo info = contraption.getBlocks().get(blockPos);

            if (info != null) {
                BlockState blockstate = info.state;

                this.world = contraption.getContraptionWorld();

                this.playStepSound(blockPos, blockstate);

                stepped = true;
            }
        }

        if (stepped)
            this.nextStepDistance = this.determineNextStepDistance();

        world = entityWorld;
    }

}
