package com.example.smartroommanagement.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartroommanagement.R;
import com.example.smartroommanagement.data.entity.RoomEntity;
import com.example.smartroommanagement.util.FinanceUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

public class RoomAdapter extends ListAdapter<RoomEntity, RoomAdapter.RoomViewHolder> {

    private OnRoomClickListener listener;
    private OnRoomLongClickListener longClickListener;

    public interface OnRoomClickListener {
        void onRoomClick(RoomEntity room);
    }

    public interface OnRoomLongClickListener {
        void onRoomLongClick(RoomEntity room);
    }

    public void setOnRoomClickListener(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void setOnRoomLongClickListener(OnRoomLongClickListener listener) {
        this.longClickListener = listener;
    }

    public RoomAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<RoomEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<RoomEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull RoomEntity oldItem, @NonNull RoomEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RoomEntity oldItem, @NonNull RoomEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getBasePrice() == newItem.getBasePrice() &&
                    (oldItem.getDescription() == null ? newItem.getDescription() == null : oldItem.getDescription().equals(newItem.getDescription()));
        }
    };

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, price;
        private final Chip statusChip;
        private final ImageView iconRoom;
        private final MaterialCardView iconContainer;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_room_name);
            price = itemView.findViewById(R.id.text_room_price);
            statusChip = itemView.findViewById(R.id.chip_status);
            iconRoom = itemView.findViewById(R.id.icon_room);
            iconContainer = itemView.findViewById(R.id.card_icon_container);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (listener != null && pos != RecyclerView.NO_POSITION) {
                    listener.onRoomClick(getItem(pos));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (longClickListener != null && pos != RecyclerView.NO_POSITION) {
                    longClickListener.onRoomLongClick(getItem(pos));
                    return true;
                }
                return false;
            });
        }

        public void bind(RoomEntity room) {
            Context context = itemView.getContext();
            name.setText(room.getName());
            price.setText(FinanceUtils.formatCurrency(room.getBasePrice()));
            
            // Làm cho chữ trạng thái in hoa và đậm hơn
            statusChip.setText(room.getStatus().toUpperCase());
            statusChip.setTypeface(null, Typeface.BOLD);
            
            if ("Trống".equalsIgnoreCase(room.getStatus())) {
                // PHÒNG TRỐNG: Màu ĐỎ ĐẬM cực kỳ nổi bật
                int colorRed = Color.parseColor("#E11D48"); // Đỏ hồng đậm
                int bgRed = Color.parseColor("#FFF1F2"); // Nền hồng rất nhạt
                
                statusChip.setChipBackgroundColor(ColorStateList.valueOf(bgRed));
                statusChip.setTextColor(colorRed);
                statusChip.setChipStrokeColor(ColorStateList.valueOf(colorRed));
                statusChip.setChipStrokeWidth(2f);
                
                iconRoom.setColorFilter(colorRed);
                iconContainer.setCardBackgroundColor(ColorStateList.valueOf(bgRed));
            } else {
                // PHÒNG ĐÃ THUÊ: Màu XANH LÁ ĐẬM nổi bật
                int colorGreen = Color.parseColor("#15803D"); // Xanh lá đậm
                int bgGreen = Color.parseColor("#F0FDF4"); // Nền xanh rất nhạt
                
                statusChip.setChipBackgroundColor(ColorStateList.valueOf(bgGreen));
                statusChip.setTextColor(colorGreen);
                statusChip.setChipStrokeColor(ColorStateList.valueOf(colorGreen));
                statusChip.setChipStrokeWidth(2f);
                
                iconRoom.setColorFilter(colorGreen);
                iconContainer.setCardBackgroundColor(ColorStateList.valueOf(bgGreen));
            }
        }
    }
}
