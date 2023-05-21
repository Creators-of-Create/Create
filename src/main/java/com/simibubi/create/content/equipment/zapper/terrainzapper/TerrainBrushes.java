package com.simibubi.create.content.equipment.zapper.terrainzapper;

public enum TerrainBrushes {
	
	Cuboid(new CuboidBrush()),
	Sphere(new SphereBrush()),
	Cylinder(new CylinderBrush()),
	Surface(new DynamicBrush(true)),
	Cluster(new DynamicBrush(false)),
	
	;
	
	private Brush brush;

	private TerrainBrushes(Brush brush) {
		this.brush = brush;
	}

	public Brush get() {
		return brush;
	}

}
