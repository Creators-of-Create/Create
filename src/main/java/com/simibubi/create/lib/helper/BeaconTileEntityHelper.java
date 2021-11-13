package com.simibubi.create.lib.helper;

import java.util.List;

import com.simibubi.create.lib.mixin.accessor.BeaconBlockEntityAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity.BeaconBeamSection;

public final class BeaconTileEntityHelper {
	public static List<BeaconBeamSection> getBeamSegments(BeaconBlockEntity bte) {
		return get(bte).create$beamSections();
	}

	public static int getLevels(BeaconBlockEntity bte) {
		return get(bte).create$getLevels();
	}

	private static BeaconBlockEntityAccessor get(BeaconBlockEntity bte) {
		return MixinHelper.cast(bte);
	}

	private BeaconTileEntityHelper() {}
}
