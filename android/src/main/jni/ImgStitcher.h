//
// Created by Adam on 23.04.2019.
//
#include <opencv2/opencv.hpp>

#ifndef PANORAMA360_IMGSTITCHER_H
#define PANORAMA360_IMGSTITCHER_H

template<typename T>
int stitchImg(T &, cv::Mat &, std::vector<std::string>);

int getProgress();

#endif //PANORAMA360_IMGSTITCHER_H
