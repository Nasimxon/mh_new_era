package com.jim.finansia.intropage;

/**
 * Created by DEV on 21.06.2016.
 */

public class DataIntro {
    private String intoTitle;
    private String contentText;
    private int imageRes;
    private int miniImageRes;


    public DataIntro(String intoTitle, String contentText, int imageRes, int miniImageRes) {
        this.intoTitle = intoTitle;
        this.contentText = contentText;
        this.imageRes = imageRes;
        this.miniImageRes = miniImageRes;
    }

    public String getIntoTitle() {
        return intoTitle;
    }

    public void setIntoTitle(String intoTitle) {
        this.intoTitle = intoTitle;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public int getMiniImageRes() {
        return miniImageRes;
    }

    public void setMiniImageRes(int miniImageRes) {
        this.miniImageRes = miniImageRes;
    }
}
