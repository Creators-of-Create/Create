package com.simibubi.create.content.logistics.trains.entity;

import com.simibubi.create.content.logistics.trains.BogeyRenderer.BogeySize;
import com.simibubi.create.content.logistics.trains.IBogeyBlock;

import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Map;


public class BogeyStyle extends ForgeRegistryEntry<BogeyStyle> implements IForgeRegistryEntry<BogeyStyle> {
	public Map<BogeySize, IBogeyBlock> blocks;
	final Class<? extends BogeyInstance> instance;

	public BogeyStyle(Class<? extends BogeyInstance> instance) {
		this.instance = instance;
	}

	public <C extends BogeyInstance> BogeyInstance getInstance() throws IllegalAccessException, InstantiationException {
		return instance.newInstance();
	}
}
