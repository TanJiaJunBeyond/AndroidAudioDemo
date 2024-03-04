#include <jni.h>
#include "lame/lame.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tanjiajun_androidaudiodemo_ui_MainActivity_getLameVersion(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(get_lame_version());
}