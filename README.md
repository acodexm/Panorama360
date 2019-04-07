### About this panorama application:
This application is a study project

# Features:
* 2D/3D user interface
* ability to taking photos and stitchung them to panorama
* ability to view pictures in gallery

# This android project uses:
* OpenCV library
* LibGDX library
* ButterKnife

# How to use app:
* The pictures will be taken ONLY when you are pointing in a centre of every rectangle AND if you dont shake camera too mach
* In FullAuto mode press shutter and move phone around.
* In Manual mode press shutter every time you want take a picture.
* If you done, press save button to process stitching and saving procedure
* Tap gallery icon to see the panorama
* Tap restart to clear everything and start making new photo
* Swipe from left side to manage settings

# Settings:
* Auto: all pictures are processed
* Panorama: longest chain of pictures horizontally will be processed (might not work)
* Wide: pictures that makes bigest area will be processed (might not work)
* 360: only when every cell on sphere is filled with picture panorama will be proccessed (not working/not optimised, try only on lovest picture quality)
* High quality: highest your divice provide (dont stitch more than 4 pictures)
* Low quality: pictures in HD (around 720p)
* Lowest quality: lowest provided by device (fast for testing)

# Required:
* opencv-3.3.1-android-sdk
* android-ndk-r15c 64bit
