package com.simibubi.create.foundation.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllDamageTypes;
import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;

public class DamageTypeTagGen extends TagsProvider<DamageType> {
	public DamageTypeTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(output, Registries.DAMAGE_TYPE, lookupProvider, Create.ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(DamageTypeTags.BYPASSES_ARMOR)
				.add(AllDamageTypes.CRUSH, AllDamageTypes.FAN_FIRE, AllDamageTypes.FAN_LAVA, AllDamageTypes.DRILL, AllDamageTypes.SAW);
		tag(DamageTypeTags.IS_FIRE)
				.add(AllDamageTypes.FAN_FIRE, AllDamageTypes.FAN_LAVA);
		tag(DamageTypeTags.IS_EXPLOSION)
				.add(AllDamageTypes.CUCKOO_SURPRISE);
	}

	@Override
	public String getName() {
		return "Create's Damage Type Tags";
	}
}
