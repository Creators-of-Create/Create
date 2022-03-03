package com.simibubi.create.content.contraptions.processing.fan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.particle.AirFlowParticle;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFanProcessingType implements Comparable<AbstractFanProcessingType> {

	public static final Map<ResourceLocation, AbstractFanProcessingType> MAP = Maps.newConcurrentMap();

	public static final AbstractFanProcessingType NONE = new AbstractFanProcessingType(-3000, new ResourceLocation(Create.ID, "none")) {
		@Override
		public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		}

		@Override
		public void affectEntity(Entity entity, Level level) {
		}

		@Override
		public boolean isApplicable(BlockGetter reader, BlockPos pos) {
			return true;
		}

		@Override
		public List<ItemStack> process(ItemStack stack, Level world) {
			return null;
		}

		@Override
		public void morphType(AirFlowParticle particle) {

		}

		@Override
		public boolean canProcess(ItemStack stack, Level level) {
			return false;
		}
	};

	public static AbstractFanProcessingType valueOf(ResourceLocation type) {
		return MAP.getOrDefault(type, NONE);
	}

	public static AbstractFanProcessingType byBlock(BlockGetter reader, BlockPos pos) {
		List<AbstractFanProcessingType> list = new ArrayList<>(MAP.values());
		list.sort(AbstractFanProcessingType::compareTo);
		for (AbstractFanProcessingType type : list) {
			if (type.isApplicable(reader, pos)) {
				return type;
			}
		}
		return NONE;
	}

	public final ResourceLocation name;
	public int priority;

	public AbstractFanProcessingType(int priority, ResourceLocation name) {
		this.name = name;
		this.priority = priority;
		AbstractFanProcessingType old = MAP.put(name, this);
		if (old != null) {
			throw new IllegalArgumentException("repeated processing type name: " + name + " for class " + old.getClass().getCanonicalName() + " and class " + this.getClass().getCanonicalName());
		}
	}

	public int compareTo(AbstractFanProcessingType other) {
		return Integer.compare(other.priority, priority);
	}

	public abstract boolean canProcess(ItemStack stack, Level level);

	public abstract void spawnParticlesForProcessing(Level level, Vec3 pos);

	public abstract void affectEntity(Entity entity, Level level);

	public abstract boolean isApplicable(BlockGetter reader, BlockPos pos);

	public ResourceLocation name() {
		return name;
	}

	public abstract @Nullable
	List<ItemStack> process(ItemStack stack, Level world);

	public abstract void morphType(AirFlowParticle particle);

}
