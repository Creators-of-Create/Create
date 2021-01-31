package com.simibubi.create.content.schematics;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.simibubi.create.foundation.utility.NBTProcessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;

import javax.annotation.Nullable;
import java.util.Optional;

public class SchematicProcessor extends StructureProcessor {
	public static final SchematicProcessor INSTANCE = new SchematicProcessor();

	@Nullable
    @Override
    public Template.BlockInfo process(IWorldReader world, BlockPos pos, Template.BlockInfo rawInfo, Template.BlockInfo info, PlacementSettings settings, @Nullable Template template) {
		if (info.nbt != null) {
			TileEntity te = info.state.createTileEntity(world);
			if (te != null) {
				CompoundNBT nbt = NBTProcessors.process(te, info.nbt, false);
				if (nbt != info.nbt)
					return new Template.BlockInfo(info.pos, info.state, nbt);
			}
		}
        return info;
    }

	@Nullable
    @Override
    public Template.EntityInfo processEntity(IWorldReader world, BlockPos pos, Template.EntityInfo rawInfo, Template.EntityInfo info, PlacementSettings settings, Template template) {
		return EntityType.readEntityType(info.nbt)
			.flatMap(type -> {
				if (world instanceof World) {
					Entity e = type.create((World) world);
					if (e != null && !e.ignoreItemEntityData()) {
						return Optional.of(info);
					}
				}
				return Optional.empty();
			})
			.orElse(null);
    }

    @Override
	protected IStructureProcessorType getType() {
		return dynamic -> INSTANCE;
	}

	@Override
	protected <T> Dynamic<T> serialize0(DynamicOps<T> ops) {
		return new Dynamic<>(ops, ops.emptyMap());
	}
}
