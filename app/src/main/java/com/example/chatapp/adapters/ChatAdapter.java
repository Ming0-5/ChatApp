package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerReceivedMessageBinding;
import com.example.chatapp.databinding.ItemContainerSentMessageBinding;
import com.example.chatapp.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImage;
    private final String senderId;
    private final OnMessageSelectedListener listener;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVE = 2;

    public interface OnMessageSelectedListener{
        void onMessagesSelected(List<String> messages);
    }

    public ChatAdapter(
            List<ChatMessage> chatMessages,
            Bitmap receiverProfileImage,
            String senderId,
            OnMessageSelectedListener listener
    ) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        if(viewType == VIEW_TYPE_SENT){

            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );

        }else{

            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position
    ) {

        ChatMessage message = chatMessages.get(position);

        if(getItemViewType(position) == VIEW_TYPE_SENT){

            ((SentMessageViewHolder) holder).setData(message);

        }else{

            ((ReceivedMessageViewHolder) holder)
                    .setData(message, receiverProfileImage);
        }

        holder.itemView.setBackgroundColor(
                message.isSelected ?
                        Color.parseColor("#5532CD32")
                        : Color.TRANSPARENT
        );

        holder.itemView.setOnLongClickListener(v -> {

            message.isSelected = !message.isSelected;

            notifyItemChanged(position);

            List<String> selectedMessages = new ArrayList<>();

            for(ChatMessage chat : chatMessages){

                if(chat.isSelected){
                    selectedMessages.add(chat.message);
                }
            }

            listener.onMessagesSelected(selectedMessages);

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {

        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVE;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(
                ItemContainerSentMessageBinding itemContainerSentMessageBinding
        ){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){

            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding
        ){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){

            binding.textMessage.setText(chatMessage.message);

            binding.textDateTime.setText(chatMessage.dateTime);

            binding.imageProfile.setImageBitmap(receiverProfileImage);

            if(chatMessage.isSuspicious){
                binding.textWarning.setVisibility(View.VISIBLE);
            }else{
                binding.textWarning.setVisibility(View.GONE);
            }
        }
    }
}