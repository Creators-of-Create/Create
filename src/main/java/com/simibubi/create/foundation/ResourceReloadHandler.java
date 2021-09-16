package com.simibubi.create.foundation;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.block.render.SpriteShifter;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.utility.ISimpleReloadListener;

import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;

public class ResourceReloadHandler implements ISimpleReloadListener {

	@Override
	public void onReload(ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
		SpriteShifter.reloadUVs();
		CreateClient.invalidateRenderers();
		IHaveGoggleInformation.numberFormat.update();
		SoundScapes.invalidateAll();
	}

}
