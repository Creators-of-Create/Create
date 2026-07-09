package com.simibubi.create.infrastructure.data;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.data.TrainHatInfoProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3;

public class VanillaHatOffsetGenerator extends TrainHatInfoProvider {
	public VanillaHatOffsetGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void createOffsets() {
		this.makeInfoFor(EntityTypes.ARMADILLO, new Vec3(0, 4, 0), "body/head/head_cube", 0.92F);
		this.makeInfoFor(EntityTypes.AXOLOTL, new Vec3(0, 1, -2), "head", 0.75F);
		this.makeInfoFor(EntityTypes.BAT, new Vec3(0, -.2, 0), "head", 0.8F);
		this.makeInfoFor(EntityTypes.BEE, new Vec3(0, 2, -2), "body", 0.5F);
		this.makeInfoFor(EntityTypes.BLAZE, new Vec3(0, 4, 0));
		this.makeInfoFor(EntityTypes.BREEZE, new Vec3(0, -5, -0.1), "body/head", 0.8F);
		this.makeInfoFor(EntityTypes.CAMEL, new Vec3(0, -8, -11.5), "body/head", 1, 1F);
		this.makeInfoFor(EntityTypes.CAT, new Vec3(0, 1, -0.25));
		this.makeInfoFor(EntityTypes.CAVE_SPIDER, new Vec3(0, 2, -3.5));
		this.makeInfoFor(EntityTypes.CHICKEN, new Vec3(0, 0, -0.25));
		this.makeInfoFor(EntityTypes.COD, new Vec3(0, 10, 0));
		this.makeInfoFor(EntityTypes.COW, new Vec3(0, 3, -3), 0.87F);
		this.makeInfoFor(EntityTypes.DOLPHIN, new Vec3(0, 3, 0), "body/head", 0.75F);
		this.makeInfoFor(EntityTypes.DONKEY, new Vec3(0, -0.8, 2));
		this.makeInfoFor(EntityTypes.ELDER_GUARDIAN, new Vec3(0, 20, 0), 0.95F);
		this.makeInfoFor(EntityTypes.ENDERMITE, new Vec3(0, 2.5, 0.5), "segment0", 0.75F);
		this.makeInfoFor(EntityTypes.FOX, new Vec3(1, 2.5, -2.0), 0.9F);
		this.makeInfoFor(EntityTypes.FROG, new Vec3(0, -4, -2.5), "body/head", 0.75F);
		this.makeInfoFor(EntityTypes.GHAST, new Vec3(0, 6, 0), "body", 0.92F);
		this.makeInfoFor(EntityTypes.GLOW_SQUID, new Vec3(0, 7, 0), "body", 0.92F);
		this.makeInfoFor(EntityTypes.GOAT, new Vec3(-0.5, -8.5, -9), "head", 2F);
		this.makeInfoFor(EntityTypes.GUARDIAN, new Vec3(0, 20, 0), 0.9F);
		this.makeInfoFor(EntityTypes.HOGLIN, new Vec3(0, 0, -4.5), 0.5F);
		this.makeInfoFor(EntityTypes.HORSE, new Vec3(0, -0.8, 2));
		this.makeInfoFor(EntityTypes.IRON_GOLEM, new Vec3(0, -2, -1.5));
		this.makeInfoFor(EntityTypes.MAGMA_CUBE, new Vec3(0, 16, 0), "cube7");
		this.makeInfoFor(EntityTypes.MOOSHROOM, new Vec3(0, 3, -3), 0.87F);
		this.makeInfoFor(EntityTypes.MULE, new Vec3(0, -0.8, 2));
		this.makeInfoFor(EntityTypes.OCELOT, new Vec3(0, 1, -0.25F));
		this.makeInfoFor(EntityTypes.PANDA, new Vec3(0, 4, 0.5), 0.75F);
		this.makeInfoFor(EntityTypes.PARROT, new Vec3(0, 0, -1.5));
		this.makeInfoFor(EntityTypes.PHANTOM, new Vec3(0, 0, -1), "body/head");
		this.makeInfoFor(EntityTypes.PIG, new Vec3(0, 3, -4));
		this.makeInfoFor(EntityTypes.PIGLIN, new Vec3(0, 0, 0), 0.92F);
		this.makeInfoFor(EntityTypes.PIGLIN_BRUTE, new Vec3(0, 0, 0), 0.92F);
		this.makeInfoFor(EntityTypes.POLAR_BEAR, new Vec3(0, 3, 0));
		this.makeInfoFor(EntityTypes.PUFFERFISH, new Vec3(0, -0.5, 0), "body", 0.75F);
		this.makeInfoFor(EntityTypes.RAVAGER, new Vec3(0, 0, -5.5), "neck/head");
		this.makeInfoFor(EntityTypes.SALMON, new Vec3(0, 1, 0));
		this.makeInfoFor(EntityTypes.SHEEP, new Vec3(0, 0.4, -1), 0.87F);
		this.makeInfoFor(EntityTypes.SILVERFISH, new Vec3(0, 3, 0), "segment1");
		this.makeInfoFor(EntityTypes.SKELETON_HORSE, new Vec3(0, -0.8, 2));
		this.makeInfoFor(EntityTypes.SLIME, new Vec3(0, 21, 0), "cube", 1.25F);
		this.makeInfoFor(EntityTypes.SNIFFER, new Vec3(0, 8, -5), "bone/body/head");
		this.makeInfoFor(EntityTypes.SNOW_GOLEM, new Vec3(0, -0.2, 0), 0.82F);
		this.makeInfoFor(EntityTypes.SPIDER, new Vec3(0, 2, -3.5));
		this.makeInfoFor(EntityTypes.SQUID, new Vec3(0, 7, 0), "body", 0.92F);
		this.makeInfoFor(EntityTypes.STRIDER, new Vec3(0, 8, 0), "body", 0.95F);
		this.makeInfoFor(EntityTypes.TADPOLE, new Vec3(0, 1, 1.5), "body");
		this.makeInfoFor(EntityTypes.TROPICAL_FISH, new Vec3(0, 1, -2), "body", 0.5F);
		this.makeInfoFor(EntityTypes.TURTLE, new Vec3(0, 3, 0));
		this.makeInfoFor(EntityTypes.WARDEN, new Vec3(0, 0, 0.5), "bone/body/head", 0.9F);
		this.makeInfoFor(EntityTypes.WITCH, new Vec3(0, -1.8, 0), 1F);
		this.makeInfoFor(EntityTypes.WITHER, new Vec3(0, 3, 0), "center_head");
		this.makeInfoFor(EntityTypes.WOLF, new Vec3(1, 2.5, 1), "real_head");
		this.makeInfoFor(EntityTypes.ZOGLIN, new Vec3(0, 0, -4.5), 0.5F);
		this.makeInfoFor(EntityTypes.ZOMBIE_HORSE, new Vec3(0, -0.8, 2));
		this.makeInfoFor(EntityTypes.ZOMBIFIED_PIGLIN, new Vec3(0, 0, 0), 0.92F);
	}
}
