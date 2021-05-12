#version 110

#flwbeginbody
#FLWPrefixFields(FLWVertexData, attribute __a_)
#FLWPrefixFields(FLWInstanceData, attribute __a_)

#FLWPrefixFields(FLWFragment, varying __v2f_)

void main() {
    FLWVertexData v;
    #FLWAssignFields(FLWVertexData, v., __a_)

    FLWInstanceData i;
    #FLWAssignFields(FLWInstanceData, i., __a_)

    FLWFragment o = FLWMain(v, i);

    #FLWAssignFields(FLWFragment, __v2f_, o.)
}
