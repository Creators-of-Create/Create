package com.simibubi.create.content.schematics;

import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
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

public class SchematicProcessor extends StructureProcessor {
	
	public static final SchematicProcessor INSTANCE = new SchematicProcessor();
	public static final Codec<SchematicProcessor> CODEC = Codec.unit(() -> {
		return INSTANCE;
	});
	
	public static IStructureProcessorType<SchematicProcessor> TYPE;
	
	public static void register() {
		TYPE = IStructureProcessorType.register("schematic", CODEC);
	}
	
	@Nullable
	@Override
	public Template.BlockInfo process(IWorldReader world, BlockPos pos, BlockPos anotherPos, Template.BlockInfo rawInfo,
			Template.BlockInfo info, PlacementSettings settings, @Nullable Template template) {
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
	public Template.EntityInfo processEntity(IWorldReader world, BlockPos pos, Template.EntityInfo rawInfo,
			Template.EntityInfo info, PlacementSettings settings, Template template) {
		return EntityType.by(info.nbt).flatMap(type -> {
			if (world instanceof World) {
				Entity e = type.create((World) world);
				if (e != null && !e.onlyOpCanSetNbt()) {
					return Optional.of(info);
				}
			}
			return Optional.empty();
		}).orElse(null);
	}

	@Override
	protected IStructureProcessorType<?> getType() {
		return TYPE;
	}

}
