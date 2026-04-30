package com.example.smartroommanagement.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
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
import com.example.smartroommanagement.data.entity.BillEntity;
import com.example.smartroommanagement.data.entity.BillWithRoomAndTenant;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.Locale;

public class BillAdapter extends ListAdapter<Object, BillAdapter.BillViewHolder> {

    private OnBillActionListener actionListener;
    private OnBillClickListener clickListener;
    private OnBillLongClickListener longClickListener;
    private OnBillMoreClickListener moreClickListener;

    public interface OnBillActionListener {
        void onBillClick(Object bill);
        void onShareBill(BillWithRoomAndTenant bill);
        void onMarkAsPaid(BillWithRoomAndTenant bill);
        void onPayQR(BillWithRoomAndTenant bill);
    }

    public interface OnBillClickListener {
        void onBillClick(BillEntity bill);
    }

    public interface OnBillLongClickListener {
        void onBillLongClick(BillWithRoomAndTenant bill);
    }

    public interface OnBillMoreClickListener {
        void onBillMoreClick(BillWithRoomAndTenant bill);
    }

    public void setOnBillActionListener(OnBillActionListener listener) {
        this.actionListener = listener;
    }

    public void setOnBillClickListener(OnBillClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnBillLongClickListener(OnBillLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnBillMoreClickListener(OnBillMoreClickListener listener) {
        this.moreClickListener = listener;
    }

    public BillAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Object> DIFF_CALLBACK = new DiffUtil.ItemCallback<Object>() {
        @Override
        public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof BillWithRoomAndTenant && newItem instanceof BillWithRoomAndTenant) {
                return ((BillWithRoomAndTenant) oldItem).bill.getId() == ((BillWithRoomAndTenant) newItem).bill.getId();
            } else if (oldItem instanceof BillEntity && newItem instanceof BillEntity) {
                return ((BillEntity) oldItem).getId() == ((BillEntity) newItem).getId();
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof BillWithRoomAndTenant && newItem instanceof BillWithRoomAndTenant) {
                BillEntity o = ((BillWithRoomAndTenant) oldItem).bill;
                BillEntity n = ((BillWithRoomAndTenant) newItem).bill;
                return o.isPaid() == n.isPaid() && 
                       Double.compare(o.getTotalAmount(), n.getTotalAmount()) == 0 &&
                       Double.compare(o.getElectricityUsage(), n.getElectricityUsage()) == 0 &&
                       Double.compare(o.getWaterUsage(), n.getWaterUsage()) == 0 &&
                       Double.compare(o.getLaundryFee(), n.getLaundryFee()) == 0 &&
                       Double.compare(o.getTrashFee(), n.getTrashFee()) == 0 &&
                       Double.compare(o.getWifiFee(), n.getWifiFee()) == 0 &&
                       Double.compare(o.getOtherFee(), n.getOtherFee()) == 0 &&
                       TextUtils.equals(o.getMonthYear(), n.getMonthYear()) &&
                       TextUtils.equals(o.getTenantName(), n.getTenantName()) &&
                       TextUtils.equals(o.getOtherFeeNote(), n.getOtherFeeNote());
            } else if (oldItem instanceof BillEntity && newItem instanceof BillEntity) {
                BillEntity o = (BillEntity) oldItem;
                BillEntity n = (BillEntity) newItem;
                return o.isPaid() == n.isPaid() && 
                       Double.compare(o.getTotalAmount(), n.getTotalAmount()) == 0 &&
                       TextUtils.equals(o.getMonthYear(), n.getMonthYear());
            }
            return false;
        }
    };

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Object item = getItem(position);
        holder.bind(item, actionListener, clickListener, longClickListener, moreClickListener);
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView roomName, month, status, electricity, water, tenant, total;
        MaterialButton btnShare, btnPay, btnPayQR, btnCancel;
        ImageButton btnMore;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.text_bill_room_name);
            month = itemView.findViewById(R.id.text_bill_month);
            status = itemView.findViewById(R.id.text_bill_status);
            electricity = itemView.findViewById(R.id.text_bill_electricity);
            water = itemView.findViewById(R.id.text_bill_water);
            tenant = itemView.findViewById(R.id.text_bill_tenant);
            total = itemView.findViewById(R.id.text_bill_total);
            btnShare = itemView.findViewById(R.id.btn_share_bill);
            btnPay = itemView.findViewById(R.id.btn_mark_paid);
            btnPayQR = itemView.findViewById(R.id.btn_pay_qr);
            btnCancel = itemView.findViewById(R.id.btn_cancel_paid);
            btnMore = itemView.findViewById(R.id.btn_more_bill);
        }

        public void bind(Object item, OnBillActionListener actionListener, OnBillClickListener clickListener, OnBillLongClickListener longClickListener, OnBillMoreClickListener moreClickListener) {
            Context context = itemView.getContext();
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            if (item instanceof BillWithRoomAndTenant) {
                BillWithRoomAndTenant fullItem = (BillWithRoomAndTenant) item;
                roomName.setText(fullItem.room != null ? fullItem.room.getName() : "Phòng --");
                month.setText("Tháng " + fullItem.bill.getMonthYear());
                setPaidStatus(fullItem.bill.isPaid(), context);
                electricity.setText("Điện: " + fullItem.bill.getElectricityUsage() + " số");
                water.setText("Nước: " + fullItem.bill.getWaterUsage() + " khối");
                
                String tenantDisplayName = fullItem.bill.getTenantName();
                if (tenantDisplayName == null || tenantDisplayName.isEmpty()) {
                    tenantDisplayName = (fullItem.tenant != null ? fullItem.tenant.getName() : "Trống");
                }
                tenant.setText("Người thuê: " + tenantDisplayName);
                
                total.setText("Tổng: " + formatter.format(fullItem.bill.getTotalAmount()));
                
                btnShare.setVisibility(View.VISIBLE);
                btnShare.setOnClickListener(v -> { if (actionListener != null) actionListener.onShareBill(fullItem); });
                
                btnPay.setOnClickListener(v -> { if (actionListener != null) actionListener.onMarkAsPaid(fullItem); });
                if (btnCancel != null) {
                    btnCancel.setOnClickListener(v -> { if (actionListener != null) actionListener.onMarkAsPaid(fullItem); });
                }
                
                if (btnPayQR != null) {
                    btnPayQR.setVisibility(fullItem.bill.isPaid() ? View.GONE : View.VISIBLE);
                    btnPayQR.setOnClickListener(v -> { if (actionListener != null) actionListener.onPayQR(fullItem); });
                }

                if (btnMore != null) {
                    btnMore.setOnClickListener(v -> {
                        if (moreClickListener != null) {
                            moreClickListener.onBillMoreClick(fullItem);
                        }
                    });
                }

                itemView.setOnLongClickListener(v -> {
                    if (longClickListener != null) {
                        longClickListener.onBillLongClick(fullItem);
                        return true;
                    }
                    return false;
                });

            } else if (item instanceof BillEntity) {
                BillEntity bill = (BillEntity) item;
                roomName.setText("Phòng " + bill.getRoomId());
                month.setText("Tháng " + bill.getMonthYear());
                setPaidStatus(bill.isPaid(), context);
                electricity.setText("Điện: " + bill.getElectricityUsage() + " số");
                water.setText("Nước: " + bill.getWaterUsage() + " khối");
                tenant.setText("Người thuê: " + (bill.getTenantName() != null ? bill.getTenantName() : "N/A"));
                total.setText("Tổng: " + formatter.format(bill.getTotalAmount()));
                
                btnShare.setVisibility(View.GONE);
                if (btnPayQR != null) btnPayQR.setVisibility(View.GONE);
                btnPay.setVisibility(bill.isPaid() ? View.GONE : View.VISIBLE);
                if (btnCancel != null) btnCancel.setVisibility(bill.isPaid() ? View.VISIBLE : View.GONE);

                if (btnMore != null) {
                    btnMore.setOnClickListener(v -> {
                        if (moreClickListener != null) {
                            BillWithRoomAndTenant wrap = new BillWithRoomAndTenant();
                            wrap.bill = bill;
                            moreClickListener.onBillMoreClick(wrap);
                        }
                    });
                }

                itemView.setOnLongClickListener(v -> {
                    if (longClickListener != null) {
                        BillWithRoomAndTenant wrap = new BillWithRoomAndTenant();
                        wrap.bill = bill;
                        longClickListener.onBillLongClick(wrap);
                        return true;
                    }
                    return false;
                });
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    if (item instanceof BillWithRoomAndTenant) {
                        clickListener.onBillClick(((BillWithRoomAndTenant) item).bill);
                    } else if (item instanceof BillEntity) {
                        clickListener.onBillClick((BillEntity) item);
                    }
                } else if (actionListener != null) {
                    actionListener.onBillClick(item);
                }
            });
        }

        private void setPaidStatus(boolean isPaid, Context context) {
            if (isPaid) {
                status.setText("ĐÃ THANH TOÁN");
                status.setTextColor(Color.parseColor("#16A34A"));
                status.setBackgroundColor(Color.parseColor("#DCFCE7"));
                
                btnPay.setVisibility(View.GONE);
                if (btnCancel != null) btnCancel.setVisibility(View.VISIBLE);
                if (btnPayQR != null) btnPayQR.setVisibility(View.GONE);
            } else {
                status.setText("CHƯA THANH TOÁN");
                status.setTextColor(Color.parseColor("#EF4444"));
                status.setBackgroundColor(Color.parseColor("#FEF2F2"));
                
                btnPay.setText("Thanh toán");
                btnPay.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));
                btnPay.setVisibility(View.VISIBLE);
                if (btnCancel != null) btnCancel.setVisibility(View.GONE);
                if (btnPayQR != null) btnPayQR.setVisibility(View.VISIBLE);
            }
        }
    }
}
