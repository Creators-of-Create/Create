#version 110

#flwbeginbody

#FLWPrefixFields(Fragment, varying, v2f_)

void main() {
    Fragment f;
    #FLWAssignFields(Fragment, f., v2f_)

    FLWMain(f);
}
