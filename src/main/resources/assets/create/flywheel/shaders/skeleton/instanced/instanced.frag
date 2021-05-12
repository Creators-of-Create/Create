#version 110

#flwbeginbody

#FLWPrefixFields(FLWFragment, varying __v2f_)

void main() {
    FLWFragment f;
    #FLWAssignFields(FLWFragment, f., __v2f_)

    FLWMain(f);
}
