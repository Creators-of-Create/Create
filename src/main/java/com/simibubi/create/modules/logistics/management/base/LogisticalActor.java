package com.simibubi.create.modules.logistics.management.base;

public abstract class LogisticalActor {

	public enum Actors {
		SUPPLY(new Supply()),
		STORAGE(new Storage()),
		DEMAND(new Demand()),
		
		;
		
		private LogisticalActor actor;
		
		public LogisticalActor get() {
			return this.actor;
		}
		
		private Actors(LogisticalActor actor) {
			this.actor = actor;
		}
	}
	
	public static class Supply extends LogisticalActor {
		
	}
	
	public static class Storage extends LogisticalActor {
		
	}
	
	public static class Demand extends LogisticalActor {
		
	}
	
	
}
