#include "study_acodexm_NativePanorama.h"
#include "opencv2/opencv.hpp"
#include <android/log.h>
#include "ImgStitcher.h"
#include "CroppImg.h"

#define  LOG_TAG    "NativePanorama"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
using namespace std;
using namespace cv;

int stitchImg(vector<Mat> &imagesArg, Mat &result,vector<string> params);

int cropp(Mat &result);
/*
 * This method uses the openCV Stitcher class to create panorama image from given pictures list
 * Additionally if the stitching was successful it crops the image to rectangular shape
 */
JNIEXPORT void JNICALL
Java_study_acodexm_NativePanorama_processPanorama
        (JNIEnv *env, jclass clazz, jlongArray imageAddressArray, jlong outputAddress, jobjectArray stringArray) {

     bool isCropped = false;
     int size = env->GetArrayLength(stringArray);
     vector<string> params;
     for (int i=0; i < size; ++i)
     {
         jstring args = (jstring)env->GetObjectArrayElement(stringArray, i);
         const char* value = env->GetStringUTFChars(args, 0);
          if (string(value) == "cropp"){
            isCropped = true;
          }
          else{
            params.push_back(value);
          }
         env->ReleaseStringUTFChars(args, value);
         env->DeleteLocalRef(args);
     }

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
        // Convert to a 3 channel Mat
        cvtColor(curimage, newimage, CV_BGRA2RGB);
        imgVec.push_back(newimage);
    }

    Mat &result = *(Mat *) outputAddress;
    int status = stitchImg(imgVec, result, params);
    if (status != 0) {
        LOGE("Can't stitch images, error code = %d", status);
    } else {
        LOGD("Stitch SUCCESS");
        if (isCropped) {
            LOGD("cropping...");
            if (cropp(result) != 0) {
                LOGE("cropping FAILED");
            } else {
                LOGD("cropping SUCCESS");
            }
        }
    }
    // Release the jlong array
    env->ReleaseLongArrayElements(imageAddressArray, imgAddressArr, 0);
}

JNIEXPORT jint JNICALL
Java_study_acodexm_NativePanorama_getProgress
        (JNIEnv *env, jclass clazz) {
    return getProgress();
}