package com.simibubi.create.content.curiosities.projector;

import java.util.Vector;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class ChromaticProjectorTileEntity extends TileEntity {

	Vector<FilterStep> stages = FilterStep.createDefault();

	public ChromaticProjectorTileEntity(TileEntityType<?> te) {
		super(te);
	}
}
