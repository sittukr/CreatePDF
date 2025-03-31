package com.edufun.createpdf.Model;

import android.net.Uri;

public class PdfFileModel {
    Uri uri;
    String fileName;
    long fileSize;

    public PdfFileModel(Uri uri, String fileName, long fileSize) {
        this.uri = uri;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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
