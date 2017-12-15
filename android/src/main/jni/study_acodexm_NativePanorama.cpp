#include "study_acodexm_NativePanorama.h"
#include "opencv2/opencv.hpp"
#include <android/log.h>

#define  LOG_TAG    "NativePanorama"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
using namespace std;
using namespace cv;

JNIEXPORT void JNICALL
Java_study_acodexm_NativePanorama_processPanorama
        (JNIEnv *env, jclass clazz, jlongArray imageAddressArray, jlong outputAddress) {
    // Get the length of the long array
    jsize a_len = env->GetArrayLength(imageAddressArray);
    // Convert the jlongArray to an array of jlong
    jlong *imgAddressArr = env->GetLongArrayElements(imageAddressArray, 0);
    // Create a vector to store all the image
    vector<Mat> imgVec;

    for (int k = 0; k < a_len; k++) {
        // Get the image
        Mat &curimage = *(Mat *) imgAddressArr[k];
        Mat newimage;
        // Convert to a 3 channel Mat to use with Stitcher module
        cvtColor(curimage, newimage, CV_BGRA2RGB);
        // Reduce the resolution for fast computation
        float scale = 1000.0f / curimage.cols;
        resize(newimage, newimage, Size((int) (scale * curimage.cols),
                                        (int) (scale * curimage.rows)));
        imgVec.push_back(newimage);
    }
    Mat &result = *(Mat *) outputAddress;
    Stitcher::Mode mode = Stitcher::PANORAMA;
    Ptr<Stitcher> stitcher = Stitcher::create(mode, false);
    Stitcher::Status status = stitcher->stitch(imgVec, result);

    if (status != Stitcher::OK) {
        LOGE("Can't stitch images, error code = %d", int(status));
    } else {
        LOGD("Success code = %d", int(status));
    }
    // Release the jlong array
    env->ReleaseLongArrayElements(imageAddressArray, imgAddressArr, 0);
}