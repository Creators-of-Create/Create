#version 110

#flwbeginbody
#FLWPrefixFields(VertexData, attribute, a_v_)
#FLWPrefixFields(InstanceData, attribute, a_i_)

#FLWPrefixFields(Fragment, varying, v2f_)

void main() {
    VertexData v;
    #FLWAssignFields(VertexData, v., a_v_)

    InstanceData i;
    #FLWAssignFields(InstanceData, i., a_i_)

    Fragment o = FLWMain(v, i);

    #FLWAssignFields(Fragment, v2f_, o.)
}
