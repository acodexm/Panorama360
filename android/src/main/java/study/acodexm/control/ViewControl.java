package study.acodexm.control;


public interface ViewControl {
    void showToastRunnable(String message);

    void showProcessingDialog();

    void hideProcessingDialog();
}
