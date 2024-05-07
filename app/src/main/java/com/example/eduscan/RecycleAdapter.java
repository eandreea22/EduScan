package com.example.eduscan;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

    private ArrayList<Uri> uriArrayList;

    public RecycleAdapter(ArrayList<Uri> uriArrayList) {
        this.uriArrayList = uriArrayList;
    }

    @NonNull
    @Override
    public RecycleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.custom_single_image, parent, false);

        return new ViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull RecycleAdapter.ViewHolder holder, int position) {
//        holder.imageView.setImageURI(uriArrayList.get(position));
//    }

    @Override
    public void onBindViewHolder(@NonNull RecycleAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Uri imageUri = uriArrayList.get(position);
        holder.imageView.setImageURI(imageUri);
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Apelăm metoda pentru ștergerea imaginii atunci când se face long click
                deleteImage(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return uriArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        public ViewHolder(@NonNull View itemView){

            super(itemView);

            imageView = itemView.findViewById(R.id.image);
        }

    }

    // Metodă pentru ștergerea unei imagini din listă
    private void deleteImage(int position) {
        uriArrayList.remove(position);
        notifyItemRemoved(position);
    }
}
