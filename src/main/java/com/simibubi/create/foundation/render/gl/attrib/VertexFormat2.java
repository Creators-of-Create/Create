package com.simibubi.create.foundation.render.gl.attrib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class VertexFormat2 {

    private final ArrayList<Class<? extends Enum<? extends IVertexAttrib>>> allAttributes;

    public VertexFormat2(ArrayList<Class<? extends Enum<? extends IVertexAttrib>>> allAttributes) {
        this.allAttributes = allAttributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Stream<IVertexAttrib> getAttributeStream() {
        return (Stream<IVertexAttrib>) allAttributes.stream().flatMap(it -> Arrays.stream(it.getEnumConstants()));
    }

    public static class Builder {
        private final ArrayList<Class<? extends Enum<? extends IVertexAttrib>>> allAttributes;

        public Builder() {
            allAttributes = new ArrayList<>();
        }

        public <A extends Enum<A> & IVertexAttrib> Builder addAttributes(Class<A> attribEnum) {
            allAttributes.add(attribEnum);
            return this;
        }

        public VertexFormat2 build() {
            return new VertexFormat2(allAttributes);
        }
    }
}
