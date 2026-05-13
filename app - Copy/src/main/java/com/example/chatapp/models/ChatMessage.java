package com.example.chatapp.models;

import java.util.Date;

public class ChatMessage {
    public String senderId, receiverId, message, dateTime;
    public Date dateObject;
    public String conversionId, conversionName, conversionImage;
    public String messageType, imageUrl, videoUrl;
    public boolean isSelected = false;
}
