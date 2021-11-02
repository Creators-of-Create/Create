package com.simibubi.create.foundation;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.utility.ISimpleReloadListener;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class ClientResourceReloadListener implements ISimpleReloadListener {

	@Override
	public void onReload(ResourceManager resourceManager, ProfilerFiller profiler) {
		CreateClient.invalidateRenderers();
		SoundScapes.invalidateAll();
		IHaveGoggleInformation.numberFormat.update();
	}

}
