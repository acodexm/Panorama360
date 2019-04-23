LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
#opencv
OPENCVROOT:= YOUR/PATH/TO/OpenCV-android-sdk
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk
LOCAL_SRC_FILES := study_acodexm_NativePanorama.cpp ImgStitcher.cpp CroppImg.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_CFLAGS += -std=c++11 -frtti -fexceptions -fopenmp -w
LOCAL_LDLIBS += -llog -L$(SYSROOT)/usr/lib
LOCAL_LDFLAGS += -fopenmp
LOCAL_MODULE := MyLib
include $(BUILD_SHARED_LIBRARY)