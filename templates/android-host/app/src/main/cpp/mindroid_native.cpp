#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_dev_mindroid_host_NativeBridge_runtimeInfo(JNIEnv* env, jobject /* this */) {
    std::string info = "Mindroid native bridge (NDK) initialized";
    return env->NewStringUTF(info.c_str());
}
