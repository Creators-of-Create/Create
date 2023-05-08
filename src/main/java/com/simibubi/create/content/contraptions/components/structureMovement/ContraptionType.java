package com.simibubi.create.content.contraptions.components.structureMovement;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.components.structureMovement.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.ClockworkContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.gantry.GantryContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.PistonContraption;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyContraption;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraption;

public class ContraptionType {

	public static Map<String, ContraptionType> entries = new HashMap<>();
	public static ContraptionType
		PISTON = register("piston", PistonContraption::new), 
		BEARING = register("bearing", BearingContraption::new),
		PULLEY = register("pulley", PulleyContraption::new),
		CLOCKWORK = register("clockwork", ClockworkContraption::new),
		MOUNTED = register("mounted", MountedContraption::new),
		STABILIZED = register("stabilized", StabilizedContraption::new),
		GANTRY = register("gantry", GantryContraption::new),
		CARRIAGE = register("carriage", CarriageContraption::new),
		ELEVATOR = register("elevator", ElevatorContraption::new);

	Supplier<? extends Contraption> factory;
	String id;

	public static ContraptionType register(String id, Supplier<? extends Contraption> factory) {
		ContraptionType value = new ContraptionType(id, factory);
		entries.put(id, value);
		return value;
	}

	private ContraptionType(String id, Supplier<? extends Contraption> factory) {
		this.factory = factory;
		this.id = id;
	}

	public static Contraption fromType(String type) {
		for (Entry<String, ContraptionType> allContraptionTypes : entries.entrySet())
			if (type.equals(allContraptionTypes.getKey()))
				return allContraptionTypes.getValue().factory.get();
		return null;
	}

}
