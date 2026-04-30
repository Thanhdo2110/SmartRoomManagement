package com.example.smartroommanagement.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartroommanagement.R;
import com.example.smartroommanagement.data.entity.TenantWithRoom;
import java.text.NumberFormat;
import java.util.Locale;

public class TenantAdapter extends ListAdapter<TenantWithRoom, TenantAdapter.TenantViewHolder> {

    private OnTenantClickListener listener;
    private OnTenantMoreClickListener moreClickListener;

    public interface OnTenantClickListener {
        void onTenantClick(TenantWithRoom tenantWithRoom);
    }

    public interface OnTenantMoreClickListener {
        void onTenantMoreClick(TenantWithRoom tenantWithRoom);
    }

    public void setOnTenantClickListener(OnTenantClickListener listener) {
        this.listener = listener;
    }

    public void setOnTenantMoreClickListener(OnTenantMoreClickListener listener) {
        this.moreClickListener = listener;
    }

    public TenantAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<TenantWithRoom> DIFF_CALLBACK = new DiffUtil.ItemCallback<TenantWithRoom>() {
        @Override
        public boolean areItemsTheSame(@NonNull TenantWithRoom oldItem, @NonNull TenantWithRoom newItem) {
            return oldItem.tenant.getId() == newItem.tenant.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TenantWithRoom oldItem, @NonNull TenantWithRoom newItem) {
            return oldItem.tenant.equals(newItem.tenant) && 
                   (oldItem.room == null ? newItem.room == null : oldItem.room.equals(newItem.room));
        }
    };

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        TenantWithRoom item = getItem(position);
        holder.bind(item);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTenantClick(item);
            }
        });

        if (holder.btnMore != null) {
            holder.btnMore.setOnClickListener(v -> {
                if (moreClickListener != null) {
                    moreClickListener.onTenantMoreClick(item);
                }
            });
        }
    }

    static class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone, room, cccd, startDate, hometown, deposit, birthDate;
        ImageButton btnMore;

        public TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_tenant_name);
            phone = itemView.findViewById(R.id.text_tenant_phone);
            room = itemView.findViewById(R.id.text_room_name);
            cccd = itemView.findViewById(R.id.text_tenant_cccd);
            startDate = itemView.findViewById(R.id.text_tenant_start_date);
            hometown = itemView.findViewById(R.id.text_tenant_hometown);
            deposit = itemView.findViewById(R.id.text_tenant_deposit);
            birthDate = itemView.findViewById(R.id.text_tenant_birthdate);
            btnMore = itemView.findViewById(R.id.btn_more_tenant);
        }

        public void bind(TenantWithRoom item) {
            name.setText(item.tenant.getName());
            phone.setText(item.tenant.getPhone());
            
            if (item.room != null) {
                room.setText(item.room.getName());
            } else {
                room.setText("Chưa gán");
            }

            cccd.setText(item.tenant.getIdentityCard() != null ? item.tenant.getIdentityCard() : "--");
            startDate.setText(item.tenant.getStartDate() != null ? item.tenant.getStartDate() : "--");
            hometown.setText(item.tenant.getHometown() != null ? item.tenant.getHometown() : "--");
            
            if (birthDate != null) {
                birthDate.setText(item.tenant.getBirthDate() != null ? item.tenant.getBirthDate() : "--");
            }
            
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            deposit.setText(formatter.format(item.tenant.getDeposit()));
        }
    }
}
