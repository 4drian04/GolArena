package com.adriangg.golarena.ListAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.adriangg.golarena.Modelos.Message;
import com.adriangg.golarena.R;

import java.util.List;

public class ListAdapterChat extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT = 1;

    private final List<Message> messages;

    public ListAdapterChat(List<Message> messages) {
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser ? TYPE_USER : TYPE_BOT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_USER){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gemma_msg, parent, false);
            return new BotViewHolder(view);
        }
    }
    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String message = messages.get(position).text;
        if(holder instanceof UserViewHolder){
            ((UserViewHolder) holder).textView.setText(message);
        } else {
            ((BotViewHolder) holder).textView.setText(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        UserViewHolder(View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.textViewMessage);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        BotViewHolder(View itemView){
            super(itemView);
            textView = itemView.findViewById(R.id.textViewMessage);
        }
    }
}
