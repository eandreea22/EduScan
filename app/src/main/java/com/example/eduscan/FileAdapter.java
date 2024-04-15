package com.example.eduscan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {


    private ArrayList<FileModel> fileList;
    private ArrayList<String> selectedFiles = new ArrayList<>();
    private SelectionChangeListener listener;

    public FileAdapter(ArrayList<FileModel> fileList) {
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {

        FileModel file = fileList.get(position);
        holder.fileNameTextView.setText(file.getFileName());
        holder.fileCheckBox.setChecked(selectedFiles.contains(file.getFileName()));

        // Ascultător pentru selectarea/deselectarea fișierului
        holder.fileCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedFiles.add(file.getFileName());
            } else {
                selectedFiles.remove(file.getFileName());
            }
            listener.onSelectionChanged(selectedFiles.size());
        });

    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }


    // Returnează fișierele selectate
    public ArrayList<String> getSelectedFiles() {
        return selectedFiles;
    }
    public void updateFiles(ArrayList<FileModel> fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }



    // Clasa internă pentru ținerea elementelor de fișier
    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;
        CheckBox fileCheckBox;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            fileCheckBox = itemView.findViewById(R.id.fileCheckBox);
        }
    }

    public void setSelectionChangeListener(SelectionChangeListener listener) {
        this.listener = listener;
    }

    interface SelectionChangeListener {
        void onSelectionChanged(int numSelected);
    }
}



