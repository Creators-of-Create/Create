package com.simibubi.create.foundation;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.utility.ISimpleReloadListener;

import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;

public class ClientResourceReloadListener implements ISimpleReloadListener {

	@Override
	public void onReload(IResourceManager resourceManager, IProfiler profiler) {
		CreateClient.invalidateRenderers();
		SoundScapes.invalidateAll();
		IHaveGoggleInformation.numberFormat.update();
	}

}
