#version 110

#flwbeginbody
#FLWPrefixFields(FLWVertexData, attribute, a_v_)
#FLWPrefixFields(FLWInstanceData, attribute, a_i_)

#FLWPrefixFields(FLWFragment, varying, v2f_)

void main() {
    FLWVertexData v;
    #FLWAssignFields(FLWVertexData, v., a_v_)

    FLWInstanceData i;
    #FLWAssignFields(FLWInstanceData, i., a_i_)

    FLWFragment o = FLWMain(v, i);

    #FLWAssignFields(FLWFragment, v2f_, o.)
}
