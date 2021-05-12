#version 110

#flwbeginbody

#FLWPrefixFields(FLWFragment, varying, v2f_)

void main() {
    FLWFragment f;
    #FLWAssignFields(FLWFragment, f., v2f_)

    FLWMain(f);
}
