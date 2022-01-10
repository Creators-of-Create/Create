package com.simibubi.create.content.contraptions.processing.fan;

import com.simibubi.create.content.contraptions.particle.AirFlowParticle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TypeCustom extends AbstractFanProcessingType {

	public TypeCustom(int priority, String name) {
		super(priority, name);
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		return false;
	}

	@Override
	public void spawnParticlesForProcessing(Level level, Vec3 pos) {

	}

	@Override
	public void affectEntity(Entity entity, Level level) {

	}

	@Override
	public boolean isApplicable(BlockGetter reader, BlockPos pos) {
		return false;
	}

	@Nullable
	@Override
	public List<ItemStack> process(ItemStack stack, Level world) {
		return null;
	}

	@Override
	public void morphType(AirFlowParticle particle) {

	}
}
