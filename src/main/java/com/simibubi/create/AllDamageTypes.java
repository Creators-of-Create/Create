package com.simibubi.create;

import com.simibubi.create.foundation.damageTypes.DamageTypeData;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public class AllDamageTypes {
	public static final DamageTypeData CUCKOO_SURPRISE = DamageTypeData.builder()
			.simpleId("cuckoo_surprise")
			.exhaustion(0.1f)
			.scaling(DamageScaling.ALWAYS)
			.tag(DamageTypeTags.IS_EXPLOSION)
			.build();
	public static final DamageTypeData CRUSH = DamageTypeData.builder()
			.simpleId("crush")
			.scaling(DamageScaling.ALWAYS)
			.tag(DamageTypeTags.BYPASSES_ARMOR)
			.build();
	public static final DamageTypeData DRILL = DamageTypeData.builder()
			.simpleId("mechanical_drill")
			.tag(DamageTypeTags.BYPASSES_ARMOR)
			.build();
	public static final DamageTypeData FAN_FIRE = DamageTypeData.builder()
			.simpleId("fan_fire")
			.effects(DamageEffects.BURNING)
			.tag(DamageTypeTags.IS_FIRE, DamageTypeTags.BYPASSES_ARMOR)
			.build();
	public static final DamageTypeData FAN_LAVA = DamageTypeData.builder()
			.simpleId("fan_lava")
			.effects(DamageEffects.BURNING)
			.tag(DamageTypeTags.IS_FIRE, DamageTypeTags.BYPASSES_ARMOR)
			.build();
	public static final DamageTypeData SAW = DamageTypeData.builder()
			.simpleId("mechanical_saw")
			.tag(DamageTypeTags.BYPASSES_ARMOR)
			.build();
	public static final DamageTypeData ROLLER = DamageTypeData.builder()
			.simpleId("mechanical_roller")
			.build();
	public static final DamageTypeData POTATO_CANNON = DamageTypeData.builder()
			.simpleId("potato_cannon")
			.build();
	public static final DamageTypeData RUN_OVER = DamageTypeData.builder()
			.simpleId("run_over")
			.build();

	public static void bootstrap(BootstapContext<DamageType> ctx) {
		DamageTypeData.allInNamespace(Create.ID).forEach(data -> data.register(ctx));
	}
}
