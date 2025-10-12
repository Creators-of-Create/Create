package com.simibubi.create.api.event;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.neoforged.neoforge.event.entity.EntityEvent;

public abstract class ContraptionEvent extends EntityEvent {
    public ContraptionEvent(AbstractContraptionEntity entity) {
        super(entity);
    }
    public static class Assemble extends ContraptionEvent {
        public Assemble(AbstractContraptionEntity entity) {
            super(entity);
        }
    }
    public static class Disassemble extends ContraptionEvent {
        public Disassemble(AbstractContraptionEntity entity) {
            super(entity);
        }
    }
}
