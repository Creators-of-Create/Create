package com.simibubi.create.foundation.metadoc;

public abstract class MetaDocInstruction {
	
	public boolean isBlocking() {
		return false;
	}
	
	public abstract boolean isComplete();
	
	public abstract void tick(MetaDocScene scene);

}
