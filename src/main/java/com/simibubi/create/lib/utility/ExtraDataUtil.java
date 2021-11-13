package com.simibubi.create.lib.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.simibubi.create.lib.helper.EntityHelper;
import com.simibubi.create.lib.helper.TileEntityHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ExtraDataUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	public static CompoundTag getExtraData(Object o) {
		if (o instanceof Entity) {
			return EntityHelper.getExtraCustomData((Entity) o);
		}

		if (o instanceof BlockEntity) {
			return TileEntityHelper.getExtraCustomData((BlockEntity) o);
		}

		LOGGER.warn("Attempted to get extra data of an object that cannot have extra data!");
		return new CompoundTag();
	}
}
