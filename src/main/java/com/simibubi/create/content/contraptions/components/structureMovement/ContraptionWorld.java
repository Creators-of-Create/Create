package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

public class ContraptionWorld extends WrappedWorld {
    final Contraption contraption;
	private final int minY;
	private final int height;

	public ContraptionWorld(Level world, Contraption contraption) {
        super(world);

        this.contraption = contraption;

		// Include 1 block above/below contraption height range to avoid certain edge-case Starlight crashes with
		// downward-facing mechanical pistons.
		minY = nextMultipleOf16(contraption.bounds.minY - 1);
		height = nextMultipleOf16(contraption.bounds.maxY + 1) - minY;
	}

	// https://math.stackexchange.com/questions/291468
	private static int nextMultipleOf16(double a) {
		return (((Math.abs((int) a) - 1) | 15) + 1) * Mth.sign(a);
	}

	@Override
    public BlockState getBlockState(BlockPos pos) {
        StructureTemplate.StructureBlockInfo blockInfo = contraption.getBlocks().get(pos);

        if (blockInfo != null)
            return blockInfo.state;

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public void playSound(Player player, double x, double y, double z, SoundEvent soundIn, SoundSource category, float volume, float pitch) {

        Vec3 worldPos = ContraptionCollider.getWorldToLocalTranslation(new Vec3(x, y, z), this.contraption.entity);

        worldPos = worldPos.add(x, y, z);

        world.playSound(player, worldPos.x, worldPos.y, worldPos.z, soundIn, category, volume, pitch);
    }

    @Override
    public void playLocalSound(double x, double y, double z, SoundEvent p_184134_7_, SoundSource p_184134_8_, float p_184134_9_, float p_184134_10_, boolean p_184134_11_) {
        world.playLocalSound(x, y, z, p_184134_7_, p_184134_8_, p_184134_9_, p_184134_10_, p_184134_11_);
    }

	// Ensure that we provide accurate information about ContraptionWorld height to mods (such as Starlight) which
	// expect Levels to only have blocks located in chunks within their height range.

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getMinBuildHeight() {
		return minY;
	}
}
