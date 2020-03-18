package com.simibubi.create.config;

import com.simibubi.create.AllBlocks;

public class StressConfigDefaults {

	public static double getDefaultStressCapacity(AllBlocks block) {
	
		switch (block) {
		case CREATIVE_MOTOR:
			return 1024;
		case FURNACE_ENGINE:
			return 512;
		case MECHANICAL_BEARING:
			return 256;
		case ENCASED_FAN:
		case HAND_CRANK:
			return 16;
		case WATER_WHEEL:
			return 4;
		default:
			return -1;
		}
	}

	public static double getDefaultStressImpact(AllBlocks block) {
	
		switch (block) {
		case CRUSHING_WHEEL:
		case MECHANICAL_PRESS:
			return 8;
	
		case DRILL:
		case SAW:
		case DEPLOYER:
		case ENCASED_FAN:
		case MECHANICAL_MIXER:
			return 4;
	
		case MECHANICAL_CRAFTER:
		case TURNTABLE:
		case MECHANICAL_PISTON:
		case MECHANICAL_BEARING:
		case CLOCKWORK_BEARING:
		case ROPE_PULLEY:
		case STICKY_MECHANICAL_PISTON:
			return 2;
			
		case BELT:
		case CUCKOO_CLOCK:
			return 1;
			
		default:
			return 0;
		}
	}

}
