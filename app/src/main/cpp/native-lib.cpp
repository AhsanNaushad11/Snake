#include <jni.h>

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_ankanalytic_snake_NativeGameLib_nextHead(
        JNIEnv *env,
        jclass,
        jint x,
        jint y,
        jint directionOrdinal) {
    jint out[2] = {x, y};

    switch (directionOrdinal) {
        case 0:
            out[1] = y - 1;
            break;
        case 1:
            out[0] = x + 1;
            break;
        case 2:
            out[1] = y + 1;
            break;
        case 3:
            out[0] = x - 1;
            break;
        default:
            break;
    }

    jintArray result = env->NewIntArray(2);
    env->SetIntArrayRegion(result, 0, 2, out);
    return result;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_ankanalytic_snake_NativeGameLib_containsPoint(
        JNIEnv *env,
        jclass,
        jintArray flattenedPoints,
        jint x,
        jint y) {
    jsize len = env->GetArrayLength(flattenedPoints);
    if (len % 2 != 0) {
        return JNI_FALSE;
    }

    jint *coords = env->GetIntArrayElements(flattenedPoints, nullptr);
    bool found = false;

    for (jsize i = 0; i < len; i += 2) {
        if (coords[i] == x && coords[i + 1] == y) {
            found = true;
            break;
        }
    }

    env->ReleaseIntArrayElements(flattenedPoints, coords, JNI_ABORT);
    return found ? JNI_TRUE : JNI_FALSE;
}
