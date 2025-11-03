package com.example.ai_travel_planner.ui.home.friend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ai_travel_planner.R;
import com.example.ai_travel_planner.network.models.ItineraryCollaborator;
import com.example.ai_travel_planner.network.models.User;

import java.util.ArrayList;
import java.util.List;

public class CollaboratorAdapter extends RecyclerView.Adapter<CollaboratorAdapter.ViewHolder> {

    private List<ItineraryCollaborator> collaborators = new ArrayList<>();
    private Context context;

    public CollaboratorAdapter(Context context) {
        this.context = context;
    }

    public void setCollaborators(List<ItineraryCollaborator> collaborators) {
        this.collaborators = collaborators;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_partner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItineraryCollaborator collaborator = collaborators.get(position);
        User user = collaborator.getUser();

        if (user != null) {
            holder.txtName.setText(user.getDisplayName());
            Glide.with(context)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.avt1)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.txtName.setText("Đang tải...");
        }

        holder.txtRole.setText(collaborator.getRole());
    }

    @Override
    public int getItemCount() {
        return collaborators.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtName;
        TextView txtRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Các ID này phải khớp với file item_partner.xml
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtName = itemView.findViewById(R.id.txtName);
            txtRole = itemView.findViewById(R.id.txtRole);
        }
    }
}