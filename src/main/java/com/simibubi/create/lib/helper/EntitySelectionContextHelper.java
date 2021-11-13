package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.extensions.EntityCollisionContextExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

public final class EntitySelectionContextHelper {
	public static Entity getEntity(EntityCollisionContext context) {
		return MixinHelper.<EntityCollisionContextExtensions>cast(context).create$getCachedEntity();
	}

	public static Entity getEntity(CollisionContext context) {
		if (context instanceof EntityCollisionContext) {
			return getEntity((EntityCollisionContext) context);
		}
		return null;
	}

	private EntitySelectionContextHelper() {}
}
