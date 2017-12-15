package study.acodexm.utils;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import study.acodexm.settings.PictureMode;

import static study.acodexm.AndroidCamera.LAT;
import static study.acodexm.AndroidCamera.LON;

public class ImageChooser {
    private static final String TAG = ImageChooser.class.getSimpleName();

    public static List<Mat> loadPictures(PictureMode pictureMode, List<Integer> ids) {
        Log.d(TAG, "loadPictures: current ids" + ids);
        List<Mat> pictures = new ArrayList<>();
        switch (pictureMode) {
            case auto:
                for (int id : ids)
                    pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                break;
            case panorama:
                List<Integer> longestIDS = idsForPanorama(ids);
                if (longestIDS != null && longestIDS.size() > 0)
                    for (int id : longestIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    Log.e(TAG, "panorama loadPictures failed: ",
                            new Throwable("empty list or null"));
                break;
            case widePicture:
                List<Integer> optimalIDS = idsForWide(ids).get(0);
                if (optimalIDS != null && optimalIDS.size() > 0)
                    for (int id : optimalIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    Log.e(TAG, "widePicture loadPictures failed: ",
                            new Throwable("empty list or null"));
                break;
            case picture360:
                if (ids.size() == LAT * LON)
                    for (int id : ids)
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                else
                    Log.e(TAG, "Picture360 loadPictures failed: ",
                            new Throwable("not enough pictures"));
                break;
        }

        return pictures;
    }

    private static Mat bitmapToMat(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        bitmap.recycle();
        return mat;
    }

    /**
     * Function determinate which matrix of pictures is the biggest and returns one or more
     * vectors of the same size
     *
     * @param integers
     * @return
     */
    private static List<List<Integer>> chooseBiggest(List<List<Integer>> integers) {
        int max = 0;
        System.out.println(integers);
        List<List<Integer>> biggest = new ArrayList<>();
        for (List<Integer> i : integers) {
            if (i.size() > max) {
                max = i.size();
                System.out.println(max);
            }
        }
        for (List<Integer> i : integers) {
            if (i.size() == max)
                biggest.add(new ArrayList<>(i));
        }
        return biggest;
    }

    /**
     * Function should return list of ids which are pointing on pictures that should be
     * the best candidates for wide panorama picture
     *
     * @param ids
     * @return
     */
    private static List<List<Integer>> idsForWide(List<Integer> ids) {
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        List<Integer> list3 = new ArrayList<>();
        List<Integer> list4 = new ArrayList<>();
        List<Integer> list5 = new ArrayList<>();

        for (int id : ids) {
            if (id > 10 && id < 21)
                list1.add(id);
            if (id > 21 && id < 32)
                list2.add(id);
            if (id > 32 && id < 43)
                list3.add(id);
            if (id > 43 && id < 54)
                list4.add(id);
            if (id > 54 && id < 65)
                list5.add(id);
        }
//        System.out.println(list1);
//        System.out.println(list2);
//        System.out.println(list3);
//        System.out.println(list4);
//        System.out.println(list5);
        int L1 = list1.size();
        int L2 = list2.size();
        int L3 = list3.size();
        int L4 = list4.size();
        int L5 = list5.size();
        List<List<Integer>> pList1 = new ArrayList<>();
        List<List<Integer>> pList2 = new ArrayList<>();
        List<List<Integer>> pList3 = new ArrayList<>();
        List<List<Integer>> pList4 = new ArrayList<>();
        List<List<Integer>> pList5 = new ArrayList<>();
        List<List<Integer>> allCombinations = new ArrayList<>();
        if (L1 > 2) {
            collinearIds(pList1, list1, 0);
        }
        if (L2 > 2) {
            collinearIds(pList2, list2, 0);
        }
        if (L3 > 2) {
            collinearIds(pList3, list3, 0);
        }
        if (L4 > 2) {
            collinearIds(pList4, list4, 0);
        }
        if (L5 > 2) {
            collinearIds(pList5, list5, 0);
        }
//        System.out.println("----------");
//        System.out.println(pList1);
//        System.out.println(pList2);
//        System.out.println(pList3);
//        System.out.println(pList4);
//        System.out.println(pList5);
        allCombinations.addAll(new ArrayList<>(pList1));
        allCombinations.addAll(new ArrayList<>(pList2));
        allCombinations.addAll(new ArrayList<>(pList3));
        allCombinations.addAll(new ArrayList<>(pList4));
        allCombinations.addAll(new ArrayList<>(pList5));
        List<List<Integer>> matrices = new ArrayList<>();
        List<Integer> matrix;
        int tmp = 0;
        for (List<Integer> allCombination : allCombinations) {
            matrix = allCombination;
            tmp = matrix.size();
            for (int j = 1; j < allCombinations.size(); j++) {
//                System.out.println(matrix+" "+ allCombinations.get(j));
                makeValidMatrix(matrix, tmp, allCombinations.get(j));
                if (!matrices.contains(matrix)) {
                    matrices.add(new ArrayList<>(matrix));
                }
            }
        }
//        System.out.println("--------------");
//        for (List<Integer>m:matrices)
//            System.out.print(m.size()+", ");
//        System.out.println("paczaj tu" + matrices);
        return chooseBiggest(matrices);
    }

    /**
     * Function creates a matrix of nearest ids as nxn where n>=3
     *
     * @param matrix
     * @param s
     * @param vector
     */
    private static void makeValidMatrix(List<Integer> matrix, int s, List<Integer> vector) {
        if (matrix.size() % vector.size() == 0 && s % vector.size() == 0) {
//            System.out.println(matrix.get(matrix.size() - 1)== (vector.get(vector.size() - 1) - 11));
            if (matrix.get(matrix.size() - 1) == vector.get(vector.size() - 1) - 11) {
                matrix.addAll(vector);
            }
        }
    }

    /**
     * Function makes a list of all possible vectors (v.size>=3) that are incremental by 1
     * from given ids a.k.a. ids={1,2,3,4...} list={[1,2,3],[2,3,4],[1,2,3,4],...}
     *
     * @param allPossibleLists
     * @param ids
     * @param i
     * @return
     */
    private static List<Integer> collinearIds(List<List<Integer>> allPossibleLists, List<Integer> ids, int i) {
        int tmp = ids.get(i);
        List<Integer> tmpList = new ArrayList<>();
        for (int a = i; a < ids.size(); a++) {
            int id = ids.get(a);
            if (tmp == id) {
                tmpList.add(id);
                tmp = id + 1;
                if (tmpList.size() > 2) {
                    addIfNotContains(allPossibleLists, tmpList);
                    List<Integer> list = collinearIds(allPossibleLists, ids, ids.indexOf(tmpList.get(1)));
                    if (list.size() > 2)
                        addIfNotContains(allPossibleLists, list);
                }
            } else {
                List<Integer> list = collinearIds(allPossibleLists, ids, ids.indexOf(id));
                if (list.size() > 2)
                    addIfNotContains(allPossibleLists, list);
            }
        }
        if (tmpList.size() > 2)
            addIfNotContains(allPossibleLists, tmpList);
        return tmpList;
    }

    private static void addIfNotContains(List<List<Integer>> matrix, List<Integer> list) {
        if (!matrix.contains(list))
            matrix.add(new ArrayList<>(list));
    }

    /**
     * Function should return list of ids which are pointing on pictures that should be
     * the best candidates for normal panorama picture
     *
     * @param ids
     * @return
     */
    private static List<Integer> idsForPanorama(List<Integer> ids) {
        Collections.sort(ids);
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        List<Integer> list3 = new ArrayList<>();
        List<Integer> list4 = new ArrayList<>();
        List<Integer> list5 = new ArrayList<>();

        for (int id : ids) {
            if (id > 10 && id < 21)
                list1.add(id);
            if (id > 21 && id < 32)
                list2.add(id);
            if (id > 32 && id < 43)
                list3.add(id);
            if (id > 43 && id < 54)
                list4.add(id);
            if (id > 54 && id < 65)
                list5.add(id);
        }
        int L1 = list1.size();
        int L2 = list2.size();
        int L3 = list3.size();
        int L4 = list4.size();
        int L5 = list5.size();
        int max = chooseLongest(new ArrayList<>(Arrays.asList(
                L1, L2, L3, L4, L5)));
        if (max == L1)
            return list1;
        if (max == L2)
            return list2;
        if (max == L3)
            return list3;
        if (max == L4)
            return list4;
        if (max == L5)
            return list5;
        return null;
    }

    /**
     * Function returns the longest vector size from given list of vectors
     *
     * @param integers
     * @return
     */
    private static int chooseLongest(List<Integer> integers) {
        int max = 0;
        for (int i : integers) {
            if (i > max)
                max = i;
        }
        return max;
    }
}
