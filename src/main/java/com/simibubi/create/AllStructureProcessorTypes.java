package com.simibubi.create;

import com.simibubi.create.content.schematics.SchematicProcessor;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AllStructureProcessorTypes {
	private static final DeferredRegister<StructureProcessorType<?>> REGISTER = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Create.ID);

	public static final RegistryObject<StructureProcessorType<SchematicProcessor>> SCHEMATIC = REGISTER.register("schematic", () -> () -> SchematicProcessor.CODEC);

	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
