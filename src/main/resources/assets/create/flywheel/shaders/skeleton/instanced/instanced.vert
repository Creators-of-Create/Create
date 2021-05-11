#version 110
#FLWPrefixFields(FLWVertexData, attribute __a_)
#FLWPrefixFields(FLWInstanceData, attribute __a_)

#FLWPrefixFields(FLWOut, varying __v2f_)

void main() {
    FLWVertexData v;
    #FLWAssignToFields(FLWVertexData, v, a_)

    FLWInstanceData i;
    #FLWAssignToFields(FLWInstanceData, i, a_)

    FLWOut o = FLWMain(v, i);

    #FLWAssignFromFields(FLWOut, o, v2f_)
}
