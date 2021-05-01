package com.jozufozu.flywheel.backend.gl.attrib;

import java.util.ArrayList;
import java.util.Arrays;

public class VertexFormat {

	private final ArrayList<IVertexAttrib> allAttributes;

	private final int numAttributes;
	private final int stride;

	public VertexFormat(ArrayList<IVertexAttrib> allAttributes) {
		this.allAttributes = allAttributes;

		int numAttributes = 0, stride = 0;
		for (IVertexAttrib attrib : allAttributes) {
			IAttribSpec spec = attrib.attribSpec();
			numAttributes += spec.getAttributeCount();
			stride += spec.getSize();
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

	public void vertexAttribPointers(int index) {
		int offset = 0;
		for (IVertexAttrib attrib : this.allAttributes) {
			IAttribSpec spec = attrib.attribSpec();
			spec.vertexAttribPointer(stride, index, offset);
			index += spec.getAttributeCount();
			offset += spec.getSize();
		}
	}

	public static Builder builder() {
		return new Builder();
	}


	public static class Builder {
		private final ArrayList<IVertexAttrib> allAttributes;

		public Builder() {
			allAttributes = new ArrayList<>();
		}

		public <A extends Enum<A> & IVertexAttrib> Builder addAttributes(Class<A> attribEnum) {
			allAttributes.addAll(Arrays.asList(attribEnum.getEnumConstants()));
			return this;
		}

		public VertexFormat build() {
			return new VertexFormat(allAttributes);
		}
	}
}
