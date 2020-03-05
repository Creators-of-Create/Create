package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

public enum TerrainBrushes {
	
	Cuboid(new CuboidBrush()),
	Sphere(new SphereBrush()),
	Cylinder(new CylinderBrush()),
	
	;
	
	private Brush brush;

	private TerrainBrushes(Brush brush) {
		this.brush = brush;
	}

	public Brush get() {
//		if (this == Cylinder)
//			brush = new CylinderBrush();
//		if (this == Sphere)
//			brush = new SphereBrush();
		return brush;
	}

}
