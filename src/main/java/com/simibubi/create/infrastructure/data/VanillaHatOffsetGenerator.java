package com.simibubi.create.infrastructure.data;

import com.simibubi.create.api.data.TrainHatInfoProvider;

import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class VanillaHatOffsetGenerator extends TrainHatInfoProvider {
	public VanillaHatOffsetGenerator(PackOutput output) {
		super(output);
	}

	@Override
	protected void createOffsets() {
		this.makeInfoFor(EntityType.AXOLOTL, new Vec3(0.0F, 1.0F, -2.0F), "head", 0.75F);
		this.makeInfoFor(EntityType.BAT, new Vec3(0.0F, 3.0F, 0.0F));
		this.makeInfoFor(EntityType.BEE, new Vec3(0.0F, 2.0F, -2.0F), "body", 0.5F);
		this.makeInfoFor(EntityType.BLAZE, new Vec3(0.0F, 4.0F, 0.0F));
		this.makeInfoFor(EntityType.CAMEL, new Vec3(0.0F, -8.0F, -11.5F), "body/head", 1, 1.0F);
		this.makeInfoFor(EntityType.CAT, new Vec3(0.0F, 1.0F, -0.25F));
		this.makeInfoFor(EntityType.CAVE_SPIDER, new Vec3(0.0F, 2.0F, -3.5F));
		this.makeInfoFor(EntityType.CHICKEN, new Vec3(0.0F, 0.0F, -0.25F));
		this.makeInfoFor(EntityType.COD, new Vec3(0.0F, 10.0F, 0.0F));
		this.makeInfoFor(EntityType.COW, new Vec3(0.0F, 2.0F, -1.25F));
		this.makeInfoFor(EntityType.DOLPHIN, new Vec3(0.0F, 3.0F, 0.0F), "body/head", 0.75F);
		this.makeInfoFor(EntityType.DONKEY, new Vec3(0.0F, 0.0F, 2.0F));
		this.makeInfoFor(EntityType.ELDER_GUARDIAN, new Vec3(0.0F, 20.0F, 0.0F));
		this.makeInfoFor(EntityType.ENDERMITE, new Vec3(0.0F, 2.5F, 0.5F), "segment0", 0.75F);
		this.makeInfoFor(EntityType.FOX, new Vec3(0.75F, 2.5F, -2.0F));
		this.makeInfoFor(EntityType.FROG, new Vec3(0.0F, -3.0F, -4.25F), "body/head", 0.5F);
		this.makeInfoFor(EntityType.GHAST, new Vec3(0.0F, 6.0F, 0.0F), "body");
		this.makeInfoFor(EntityType.GLOW_SQUID, new Vec3(0.0F, 6.0F, 0.0F), "body");
		this.makeInfoFor(EntityType.GOAT, new Vec3(-0.5F, 2.0F, 0.0F), "nose", 0.5F);
		this.makeInfoFor(EntityType.GUARDIAN, new Vec3(0.0F, 20.0F, 0.0F));
		this.makeInfoFor(EntityType.HOGLIN, new Vec3(0.0F, 0.0F, -4.5F), 0.5F);
		this.makeInfoFor(EntityType.HORSE, new Vec3(0.0F, 0.0F, 2.0F));
		this.makeInfoFor(EntityType.IRON_GOLEM, new Vec3(0.0F, -2.0F, -1.5F));
		this.makeInfoFor(EntityType.MAGMA_CUBE, new Vec3(0.0F, 16.0F, 0.0F), "cube7");
		this.makeInfoFor(EntityType.MOOSHROOM, new Vec3(0.0F, 3.0F, -1.75F));
		this.makeInfoFor(EntityType.MULE, new Vec3(0.0F, 0.0F, 2.0F));
		this.makeInfoFor(EntityType.OCELOT, new Vec3(0.0F, 1.0F, -0.25F));
		this.makeInfoFor(EntityType.PANDA, new Vec3(0.0F, 4.0F, 0.5F), 0.75F);
		this.makeInfoFor(EntityType.PARROT, new Vec3(0.0F, 0.0F, -1.5F));
		this.makeInfoFor(EntityType.PHANTOM, new Vec3(0.0F, 0.0F, -1.0F), "body/head");
		this.makeInfoFor(EntityType.PIG, new Vec3(0.0F, 3.0F, -4.0F));
		this.makeInfoFor(EntityType.POLAR_BEAR, new Vec3(0.0F, 3.0F, 0.0F));
		this.makeInfoFor(EntityType.PUFFERFISH, new Vec3(0.0F, -0.5F, 0.0F), "body", 0.75F);
		this.makeInfoFor(EntityType.RAVAGER, new Vec3(0.0F, 0.0F, -5.5F), "neck/head");
		this.makeInfoFor(EntityType.SALMON, new Vec3(0.0F, 1.0F, 0.0F));
		this.makeInfoFor(EntityType.SHEEP, new Vec3(0.0F, 0.5F, -0.75F));
		this.makeInfoFor(EntityType.SILVERFISH, new Vec3(0.0F, 3.0F, 0.0F), "segment1");
		this.makeInfoFor(EntityType.SKELETON_HORSE, new Vec3(0.0F, 0.0F, 2.0F));
		this.makeInfoFor(EntityType.SLIME, new Vec3(0.0F, 12.0F, 0.0F), "cube", 1.25F);
		this.makeInfoFor(EntityType.SNIFFER, new Vec3(0.0F, 8.0F, -5.0F), "bone/body/head");
		this.makeInfoFor(EntityType.SPIDER, new Vec3(0.0F, 2.0F, -3.5F));
		this.makeInfoFor(EntityType.SQUID, new Vec3(0.0F, 6.0F, 0.0F), "body");
		this.makeInfoFor(EntityType.STRIDER, new Vec3(0.0F, 5.0F, 0.0F), "body");
		this.makeInfoFor(EntityType.TADPOLE, new Vec3(0.0F, 1.0F, 1.5F), "body");
		this.makeInfoFor(EntityType.TROPICAL_FISH, new Vec3(0.0F, 1.0F, -2.0F), "body", 0.5F);
		this.makeInfoFor(EntityType.TURTLE, new Vec3(0.0F, 3.0F, 0.0F));
		this.makeInfoFor(EntityType.WARDEN, new Vec3(0.0F, 3.5F, 0.5F), "bone/body/head", 0.5F);
		this.makeInfoFor(EntityType.WITHER, new Vec3(0.0F, 3.0F, 0.0F), "center_head");
		this.makeInfoFor(EntityType.WOLF, new Vec3(0.5F, 2.5F, 0.25F), "real_head");
		this.makeInfoFor(EntityType.ZOGLIN, new Vec3(0.0F, 0.0F, -4.5F), 0.5F);
		this.makeInfoFor(EntityType.ZOMBIE_HORSE, new Vec3(0.0F, 0.0F, 2.0F));
	}
}
