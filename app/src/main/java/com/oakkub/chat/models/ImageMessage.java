package com.oakkub.chat.models;

/**
 * Created by OaKKuB on 11/12/2015.
 */
public class ImageMessage extends Message {

    String imagePath;
    String thumbnailPath;

    public ImageMessage() {
    }

    public ImageMessage(String roomId, String imagePath, String thumbnailPath, String sentBy, String imageMessage) {
        super(roomId, imageMessage, sentBy);
        this.imagePath = imagePath;
        this.thumbnailPath = thumbnailPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

}
