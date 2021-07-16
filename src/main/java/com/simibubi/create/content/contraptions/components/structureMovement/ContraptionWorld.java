package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;

public class ContraptionWorld extends WrappedWorld {
    final Contraption contraption;

    public ContraptionWorld(World world, Contraption contraption) {
        super(world);

        this.contraption = contraption;
    }


    @Override
    public BlockState getBlockState(BlockPos pos) {
        Template.BlockInfo blockInfo = contraption.getBlocks().get(pos);

        if (blockInfo != null)
            return blockInfo.state;

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public void playSound(PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {

        Vector3d worldPos = ContraptionCollider.getWorldToLocalTranslation(new Vector3d(x, y, z), this.contraption.entity);

        worldPos = worldPos.add(x, y, z);

        world.playSound(player, worldPos.x, worldPos.y, worldPos.z, soundIn, category, volume, pitch);
    }

    @Override
    public void playLocalSound(double x, double y, double z, SoundEvent pSoundIn, SoundCategory pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
        world.playLocalSound(x, y, z, pSoundIn, pCategory, pVolume, pPitch, pDistanceDelay);
    }
}
