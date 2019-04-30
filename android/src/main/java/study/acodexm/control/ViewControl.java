package study.acodexm.control;


import study.acodexm.representation.MatrixF4x4;

public interface ViewControl {
    void showToastRunnable(String message);

    void showProcessingDialog();

    void hideProcessingDialog();

    void post(Runnable runnable);

    void updateRender();

    void rotateSphere(MatrixF4x4 matrix);

}
