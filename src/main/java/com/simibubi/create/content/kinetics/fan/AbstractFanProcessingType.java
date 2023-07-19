package com.simibubi.create.content.kinetics.fan;

import com.google.common.collect.Maps;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

/**
 * Make Class extending this if you want to add your own Fan processing type.
 * After that, register it's condition using {@link ProcessingTypeTransformerRegistry#registerProcessingTypeTransformer(int, ProcessingTypeTransformerRegistry.ProcessingTypeTransformer)}
 */
public abstract class AbstractFanProcessingType {

	public static final Map<String, AbstractFanProcessingType> REGISTRY = Maps.newConcurrentMap();
	public static final AbstractFanProcessingType NONE = new AbstractFanProcessingType("NONE") {
		@Override
		public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		}

		@Override
		public void affectEntity(Entity entity, Level level) {
		}

		@Override
		public void particleMorphType(AirFlowParticle particle) {

		}

		@Override
		public List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world) {
			return null;
		}

		@Override
		public boolean canProcess(ItemStack stack, Level level) {
			return false;
		}
	};

	public final String name;

	public AbstractFanProcessingType(String name) {
		this.name = name;
		AbstractFanProcessingType old = REGISTRY.put(name, this);
		if (old != null) {
			throw new IllegalArgumentException("repeated processing type name: "
					+ name + " for class " + old.getClass().getCanonicalName()
					+ " and class " + this.getClass().getCanonicalName());
		}
	}

	public abstract boolean canProcess(ItemStack stack, Level level);

	public abstract void spawnParticlesForProcessing(Level level, Vec3 pos);

	public abstract void affectEntity(Entity entity, Level level);

	public abstract void particleMorphType(AirFlowParticle particle);

	public abstract List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world);

	public static AbstractFanProcessingType valueOf(String name) {
		return REGISTRY.getOrDefault(name, NONE);
	}

	public String name() {
		return this.name;
	}

}
