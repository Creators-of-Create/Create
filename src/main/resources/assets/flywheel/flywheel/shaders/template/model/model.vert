#version 110

#flwbeginbody
#FLWPrefixFields(VertexData, attribute, a_v_)

#FLWPrefixFields(Fragment, varying, v2f_)

void main() {
    VertexData v;
    #FLWAssignFields(VertexData, v., a_v_)

    Fragment o = FLWMain(v);

    #FLWAssignFields(Fragment, v2f_, o.)
}
