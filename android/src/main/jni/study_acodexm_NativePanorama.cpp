#include "study_acodexm_NativePanorama.h"
#include "opencv2/opencv.hpp"
#include <android/log.h>

#define  LOG_TAG    "NativePanorama"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
using namespace std;
using namespace cv;

bool checkInteriorExterior(const Mat &mask, const Rect &croppingMask,
                           int &top,
                           int &bottom,
                           int &left,
                           int &right) {
    // return true if the rectangle is fine as it is!
    bool returnVal = true;

    Mat sub = mask(croppingMask);
    int x = 0;
    int y = 0;

    // count how many exterior pixels are at the
    int cTop = 0; // top row
    int cBottom = 0; // bottom row
    int cLeft = 0; // left column
    int cRight = 0; // right column
    // and choose that side for reduction where mose exterior pixels occurred (that's the heuristic)

    for (y = 0, x = 0; x < sub.cols; ++x) {
        // if there is an exterior part in the interior we have to move the top side of the rect a bit to the bottom
        if (sub.at<char>(y, x) == 0) {
            returnVal = false;
            ++cTop;
        }
    }

    for (y = (sub.rows - 1), x = 0; x < sub.cols; ++x) {
        // if there is an exterior part in the interior we have to move the bottom side of the rect a bit to the top
        if (sub.at<char>(y, x) == 0) {
            returnVal = false;
            ++cBottom;
        }
    }

    for (y = 0, x = 0; y < sub.rows; ++y) {
        // if there is an exterior part in the interior
        if (sub.at<char>(y, x) == 0) {
            returnVal = false;
            ++cLeft;
        }
    }

    for (x = (sub.cols - 1), y = 0; y < sub.rows; ++y) {
        // if there is an exterior part in the interior
        if (sub.at<char>(y, x) == 0) {
            returnVal = false;
            ++cRight;
        }
    }

    // The idea is to set `top = 1` if it's better to reduce
    // the rect at the top than anywhere else.
    if (cTop > cBottom) {
        if (cTop > cLeft)
            if (cTop > cRight)
                top = 1;
    } else if (cBottom > cLeft)
        if (cBottom > cRight)
            bottom = 1;

    if (cLeft >= cRight) {
        if (cLeft >= cBottom)
            if (cLeft >= cTop)
                left = 1;
    } else if (cRight >= cTop)
        if (cRight >= cBottom)
            right = 1;

    return returnVal;
}

bool compareX(Point a, Point b) {
    return a.x < b.x;
}

bool compareY(Point a, Point b) {
    return a.y < b.y;
}

/*
 * This method is used to crop given image to get rid of black space surrounding the image after
 * stitching.
 * It search for the biggest rectangle that fits inside the image. The rectangle is not rotated
 * relative to original picture edges.
 */
void cropResult(Mat &result) {
    LOGD("crop begin");
    Mat gray;
    result.convertTo(result, CV_8U);
    cvtColor(result, gray, CV_BGR2GRAY);

    // extract all the black background (and some interior parts maybe)
    Mat mask1 = gray > 0;

    // now extract the outer contour
    std::vector<std::vector<Point> > contours;
    std::vector<Vec4i> hierarchy;

    findContours(mask1, contours, hierarchy, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_NONE, Point(0, 0));
    Mat contourImage = Mat::zeros(result.size(), CV_8UC3);;

    // find contour with max elements
    int maxSize = 0;
    int id = 0;
    for (int i = 0; i < contours.size(); ++i) {
        if (contours.at((unsigned long) i).size() > maxSize) {
            maxSize = (int) contours.at((unsigned long) i).size();
            id = i;
        }
    }

    // Draw filled contour to obtain a mask with interior parts
    Mat contourMask = Mat::zeros(result.size(), CV_8UC1);
    drawContours(contourMask, contours, id, Scalar(255), -1, 8, hierarchy, 0, Point());

    // sort contour in x/y directions to easily find min/max and next
    std::vector<Point> cSortedX = contours.at((unsigned long) id);
    std::sort(cSortedX.begin(), cSortedX.end(), compareX);
    std::vector<Point> cSortedY = contours.at((unsigned long) id);
    std::sort(cSortedY.begin(), cSortedY.end(), compareY);


    int minXId = 0;
    int maxXId = (int) (cSortedX.size() - 1);
    int minYId = 0;
    int maxYId = (int) (cSortedY.size() - 1);

    Rect croppingMask;
    while ((minXId < maxXId) && (minYId < maxYId)) {
        Point min(cSortedX[minXId].x, cSortedY[minYId].y);
        Point max(cSortedX[maxXId].x, cSortedY[maxYId].y);
        croppingMask = Rect(min.x, min.y, max.x - min.x, max.y - min.y);
        // out-codes: if one of them is set, the rectangle size has to be reduced at that border
        int ocTop = 0;
        int ocBottom = 0;
        int ocLeft = 0;
        int ocRight = 0;

        bool finished = checkInteriorExterior(contourMask, croppingMask, ocTop, ocBottom, ocLeft, ocRight);
        if (finished) {
            break;
        }

        // reduce rectangle at border if necessary
        if (ocLeft)++minXId;
        if (ocRight) --maxXId;
        if (ocTop) ++minYId;
        if (ocBottom)--maxYId;
    }
    //crop image with created mask
    result = result(croppingMask);
    LOGD("crop end");
}
/*
 * This method uses the openCV Stitcher class to create panorama image from given pictures list
 * Additionally if the stitching was successful it crops the image to rectangular shape
 */
JNIEXPORT void JNICALL
Java_study_acodexm_NativePanorama_processPanorama
        (JNIEnv *env, jclass clazz, jlongArray imageAddressArray, jlong outputAddress, jboolean isCropped) {
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
        resize(newimage, newimage, Size((int) (scale * curimage.cols), (int) (scale * curimage.rows)));
        imgVec.push_back(newimage);
    }
    Mat &result = *(Mat *) outputAddress;
    Stitcher::Mode mode = Stitcher::PANORAMA;
    Ptr<Stitcher> stitcher = Stitcher::create(mode, true);
    Stitcher::Status status = stitcher->stitch(imgVec, result);
    if (status != Stitcher::OK) {
        LOGE("Can't stitch images, error code = %d", int(status));
    } else {
        LOGD("Success code = %d", int(status));
        if(isCropped){
            cropResult(result);
        }
    }
    // Release the jlong array
    env->ReleaseLongArrayElements(imageAddressArray, imgAddressArr, 0);
}