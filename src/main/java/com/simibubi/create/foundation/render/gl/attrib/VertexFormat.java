package com.simibubi.create.foundation.render.gl.attrib;

public class VertexFormat {

    private final VertexAttribute[] elements;

    private final int numAttributes;
    private final int stride;

    public VertexFormat(VertexAttribute... elements) {
        this.elements = elements;
        int numAttributes = 0, stride = 0;
        for (VertexAttribute element : elements) {
            numAttributes += element.getAttributeCount();
            stride += element.getSize();
        }
        this.numAttributes = numAttributes;
        this.stride = stride;
    }

    public VertexFormat(VertexFormat start, VertexAttribute... elements) {
        int baseLength = start.elements.length;
        int addedLength = elements.length;
        this.elements = new VertexAttribute[baseLength + addedLength];
        System.arraycopy(start.elements, 0, this.elements, 0, baseLength);
        System.arraycopy(elements, 0, this.elements, baseLength, addedLength);

        int numAttributes = 0, stride = 0;
        for (VertexAttribute element : this.elements) {
            numAttributes += element.getAttributeCount();
            stride += element.getSize();
        }
        this.numAttributes = numAttributes;
        this.stride = stride;
    }

    public int getShaderAttributeCount() {
        return numAttributes;
    }

    public int getStride() {
        return stride;
    }

    public void informAttributes(int index) {
        int offset = 0;
        for (VertexAttribute element : this.elements) {
            element.registerForBuffer(stride, index, offset);
            index += element.getAttributeCount();
            offset += element.getSize();
        }
    }
}
