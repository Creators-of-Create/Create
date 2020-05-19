package com.simibubi.create.config;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.KineticBlock;
import com.simibubi.create.modules.contraptions.components.flywheel.engine.EngineBlock;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class CStress extends ConfigBase {

	private Map<ResourceLocation, ConfigValue<Double>> capacities = new HashMap<>();
	private Map<ResourceLocation, ConfigValue<Double>> impacts = new HashMap<>();

	@Override
	protected void registerAll(Builder builder) {
		builder.comment("", Comments.su, Comments.impact)
			.push("impact");
		
		// old
		for (AllBlocks block : AllBlocks.values())
			if (block.get() instanceof KineticBlock)
				initStressEntry(block, builder);
		//
		
		StressConfigDefaults.registeredDefaultImpacts.forEach((r, i) -> getImpacts().put(r, builder.define(r.getPath(), i)));
		builder.pop();

		builder.comment("", Comments.su, Comments.capacity)
			.push("capacity");
		
		// old
		for (AllBlocks block : AllBlocks.values())
			if (block.get() instanceof KineticBlock || block.get() instanceof EngineBlock)
				initStressCapacityEntry(block, builder);
		//

		StressConfigDefaults.registeredDefaultCapacities
			.forEach((r, i) -> getCapacities().put(r, builder.define(r.getPath(), i)));
		builder.pop();
	}

	public double getImpactOf(Block block) {
		ResourceLocation key = block.getRegistryName();
		return getImpacts().containsKey(key) ? getImpacts().get(key).get() : 0;
	}
	
	public double getCapacityOf(Block block) {
		ResourceLocation key = block.getRegistryName();
		return getCapacities().containsKey(key) ? getCapacities().get(key).get() : 0;
	}
	
	@Deprecated
	private void initStressEntry(AllBlocks block, final ForgeConfigSpec.Builder builder) {
		String name = Lang.asId(block.name());
		double defaultStressImpact = StressConfigDefaults.getDefaultStressImpact(block);
		getImpacts().put(block.get()
			.getRegistryName(), builder.define(name, defaultStressImpact));
	}

	@Deprecated
	private void initStressCapacityEntry(AllBlocks block, final ForgeConfigSpec.Builder builder) {
		double defaultStressCapacity = StressConfigDefaults.getDefaultStressCapacity(block);
		if (defaultStressCapacity == -1)
			return;
		String name = Lang.asId(block.name());
		getCapacities().put(block.get()
			.getRegistryName(), builder.define(name, defaultStressCapacity));
	}

	@Override
	public String getName() {
		return "stressValues.v" + StressConfigDefaults.forcedUpdateVersion;
	}

	public Map<ResourceLocation, ConfigValue<Double>> getImpacts() {
		return impacts;
	}

	public Map<ResourceLocation, ConfigValue<Double>> getCapacities() {
		return capacities;
	}

	private static class Comments {
		static String su = "[in Stress Units]";
		static String impact =
			"Configure the individual stress impact of mechanical blocks. Note that this cost is doubled for every speed increase it receives.";
		static String capacity = "Configure how much stress a source can accommodate for.";
	}

}
