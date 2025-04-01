package com.edufun.createpdf.Model;

import android.net.Uri;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

public class ImageListModel {
    Uri uri;
    long fileSize;
    String fileName;
    int rotation;
    Rectangle pageSize;

    public ImageListModel(Uri uri, long fileSize, String fileName, int rotation, Rectangle pageSize) {
        this.uri = uri;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.rotation = rotation;
        this.pageSize = pageSize;
    }

    public void setPageSize(Rectangle pageSize) {
        this.pageSize = pageSize;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getReadableFileSize(){
        if (fileSize<1024){
            return fileSize+" B";
        } else if (fileSize < 1024*1024) {
            return (fileSize/1024) + " KB";
        }else {
            return (fileSize/(1024*1024))+" MB";
        }
    }
}
