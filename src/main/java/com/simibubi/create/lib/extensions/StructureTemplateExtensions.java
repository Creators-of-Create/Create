package com.simibubi.create.lib.extensions;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

public interface StructureTemplateExtensions {
	List<StructureTemplate.StructureEntityInfo> create$getEntities();

	Vec3 transformedVec3d(StructurePlaceSettings placementIn, Vec3 pos);

	List<StructureTemplate.StructureEntityInfo> create$processEntityInfos(@Nullable StructureTemplate template, LevelAccessor world, BlockPos blockPos, StructurePlaceSettings settings, List<StructureTemplate.StructureEntityInfo> infos);

	void create$addEntitiesToWorld(ServerLevelAccessor world, BlockPos blockPos, StructurePlaceSettings settings);
}
