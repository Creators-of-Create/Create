package com.simibubi.create.foundation.config;

import java.util.HashMap;
import java.util.Map;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.crank.ValveHandleBlock;
import com.simibubi.create.foundation.block.BlockStressDefaults;
import com.simibubi.create.foundation.block.BlockStressValues.IStressValueProvider;

import com.simibubi.create.lib.config.ConfigValue;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class CStress extends ConfigBase implements IStressValueProvider {

	private final Map<ResourceLocation, ConfigValue<Double>> capacities = new HashMap<>();
	private final Map<ResourceLocation, ConfigValue<Double>> impacts = new HashMap<>();

	@Override
	protected void registerAll(ConfigSpec builder) {
//		builder.comment("", Comments.su, Comments.impact)
//			.push("impact");
		BlockStressDefaults.DEFAULT_IMPACTS
			.forEach((r, i) -> {
//				if (r.getNamespace().equals(Create.ID))
//					getImpacts().put(r, builder.define(r.getPath(), i));
			});
//		builder.pop();

//		builder.comment("", Comments.su, Comments.capacity)
//			.push("capacity");
		BlockStressDefaults.DEFAULT_CAPACITIES
			.forEach((r, i) -> {
//				if (r.getNamespace().equals(Create.ID))
//					getCapacities().put(r, builder.define(r.getPath(), i));
			});
//		builder.pop();
	}

	@Override
	public double getImpact(Block block) {
		block = redirectValues(block);
		ResourceLocation key = Registry.BLOCK.getKey(block);
		ConfigValue<Double> value = getImpacts().get(key);
		if (value != null) {
			return value.get();
		}
		return 0;
	}

	@Override
	public double getCapacity(Block block) {
		block = redirectValues(block);
		ResourceLocation key = Registry.BLOCK.getKey(block);
		ConfigValue<Double> value = getCapacities().get(key);
		if (value != null) {
			return value.get();
		}
		return 0;
	}

	@Override
	public boolean hasImpact(Block block) {
		block = redirectValues(block);
		ResourceLocation key = Registry.BLOCK.getKey(block);
		return getImpacts().containsKey(key);
	}

	@Override
	public boolean hasCapacity(Block block) {
		block = redirectValues(block);
		ResourceLocation key = Registry.BLOCK.getKey(block);
		return getCapacities().containsKey(key);
	}

	protected Block redirectValues(Block block) {
		if (block instanceof ValveHandleBlock) {
			return AllBlocks.HAND_CRANK.get();
		}
		return block;
	}

	@Override
	public String getName() {
		return "stressValues.v" + BlockStressDefaults.FORCED_UPDATE_VERSION;
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
