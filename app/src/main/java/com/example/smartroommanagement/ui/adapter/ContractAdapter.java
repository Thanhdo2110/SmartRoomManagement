package com.example.smartroommanagement.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartroommanagement.R;
import com.example.smartroommanagement.data.entity.RoomWithTenants;
import com.example.smartroommanagement.data.entity.TenantEntity;
import com.example.smartroommanagement.util.FinanceUtils;

public class ContractAdapter extends ListAdapter<RoomWithTenants, ContractAdapter.ContractViewHolder> {

    private OnContractClickListener listener;

    public interface OnContractClickListener {
        void onContractClick(RoomWithTenants item);
    }

    public void setOnContractClickListener(OnContractClickListener listener) {
        this.listener = listener;
    }

    public ContractAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<RoomWithTenants> DIFF_CALLBACK = new DiffUtil.ItemCallback<RoomWithTenants>() {
        @Override
        public boolean areItemsTheSame(@NonNull RoomWithTenants oldItem, @NonNull RoomWithTenants newItem) {
            return oldItem.room.getId() == newItem.room.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RoomWithTenants oldItem, @NonNull RoomWithTenants newItem) {
            return oldItem.room.equals(newItem.room) &&
                    (oldItem.tenants == null ? newItem.tenants == null : oldItem.tenants.equals(newItem.tenants));
        }
    };

    @NonNull
    @Override
    public ContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contract, parent, false);
        return new ContractViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractViewHolder holder, int position) {
        RoomWithTenants item = getItem(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContractClick(item);
            }
        });
    }

    static class ContractViewHolder extends RecyclerView.ViewHolder {
        TextView roomHeader, tenantName, tenantPhone, tenantId, tenantHometown, termTag, startDate, deposit, contractDuration;

        public ContractViewHolder(@NonNull View itemView) {
            super(itemView);
            roomHeader = itemView.findViewById(R.id.text_contract_room_header);
            tenantName = itemView.findViewById(R.id.text_contract_tenant_name);
            tenantPhone = itemView.findViewById(R.id.text_contract_tenant_phone);
            tenantId = itemView.findViewById(R.id.text_contract_tenant_id);
            tenantHometown = itemView.findViewById(R.id.text_contract_tenant_hometown);
            termTag = itemView.findViewById(R.id.text_contract_term_tag);
            startDate = itemView.findViewById(R.id.text_contract_start_date);
            deposit = itemView.findViewById(R.id.text_contract_deposit);
            contractDuration = itemView.findViewById(R.id.text_contract_duration);
        }

        public void bind(RoomWithTenants item) {
            if (roomHeader != null) roomHeader.setText("HỢP ĐỒNG PHÒNG " + item.room.getName().toUpperCase());
            
            if (item.tenants != null && !item.tenants.isEmpty()) {
                TenantEntity tenant = item.tenants.get(0);
                if (tenantName != null) tenantName.setText(tenant.getName().toUpperCase());
                if (tenantPhone != null) tenantPhone.setText("SĐT: " + (tenant.getPhone() != null ? tenant.getPhone() : "--"));
                if (tenantId != null) tenantId.setText(tenant.getIdentityCard() != null ? tenant.getIdentityCard() : "--");
                if (tenantHometown != null) tenantHometown.setText(tenant.getHometown() != null ? tenant.getHometown() : "--");
                
                int term = tenant.getContractTerm() != null ? tenant.getContractTerm() : 12;
                String termStr = term + " THÁNG";
                if (termTag != null) {
                    termTag.setText(termStr);
                    termTag.setVisibility(View.VISIBLE);
                }
                if (contractDuration != null) contractDuration.setText(termStr);
                
                if (startDate != null) startDate.setText(tenant.getStartDate() != null ? tenant.getStartDate() : "--");
                if (deposit != null) deposit.setText(FinanceUtils.formatCurrency(tenant.getDeposit()));
                itemView.setAlpha(1.0f);
            } else {
                if (tenantName != null) tenantName.setText("PHÒNG TRỐNG");
                if (tenantPhone != null) tenantPhone.setText("SĐT: --");
                if (tenantId != null) tenantId.setText("--");
                if (tenantHometown != null) tenantHometown.setText("--");
                if (termTag != null) termTag.setVisibility(View.GONE);
                if (contractDuration != null) contractDuration.setText("--");
                if (startDate != null) startDate.setText("--");
                if (deposit != null) deposit.setText(FinanceUtils.formatCurrency(0));
                itemView.setAlpha(0.6f);
            }
        }
    }
}
