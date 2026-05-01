package com.example.smartroommanagement.ui.activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartroommanagement.R;
import com.example.smartroommanagement.data.entity.BillWithRoomAndTenant;
import com.example.smartroommanagement.ui.viewmodel.BillViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsDebug";
    private TextView textTotalRevenue, textPaidRevenue, textUnpaidRevenue, textChartTitle;
    private BarChart barChart;
    private BillViewModel billViewModel;
    private RecyclerView recyclerMonthly;
    private MonthlyAdapter monthlyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        setupToolbar();
        initViews();
        setupChartBase();
        
        billViewModel = new ViewModelProvider(this).get(BillViewModel.class);
        observeStatistics();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        textTotalRevenue = findViewById(R.id.text_total_revenue);
        textPaidRevenue = findViewById(R.id.text_paid_revenue);
        textUnpaidRevenue = findViewById(R.id.text_unpaid_revenue);
        textChartTitle = findViewById(R.id.text_chart_title);
        barChart = findViewById(R.id.bar_chart_revenue);
        recyclerMonthly = findViewById(R.id.recycler_monthly_stats);
        
        recyclerMonthly.setLayoutManager(new LinearLayoutManager(this));
        monthlyAdapter = new MonthlyAdapter();
        recyclerMonthly.setAdapter(monthlyAdapter);
    }

    private void setupChartBase() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setNoDataText("Đang phân tích dữ liệu...");
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int month = (int) value;
                if (month >= 1 && month <= 12) return "T" + month;
                return "";
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        
        barChart.getAxisRight().setEnabled(false);
        barChart.setAutoScaleMinMaxEnabled(true);
        
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void observeStatistics() {
        billViewModel.getAllBillsWithDetails().observe(this, bills -> {
            if (bills != null) {
                Log.d(TAG, "== NHẬN DỮ LIỆU: " + bills.size() + " hóa đơn ==");
                processData(bills);
            }
        });
    }

    private void processData(List<BillWithRoomAndTenant> allBills) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Set<Integer> yearsInDb = new HashSet<>();
        
        for (BillWithRoomAndTenant b : allBills) {
            int y = parseYear(b.bill.getMonthYear());
            if (y > 0) yearsInDb.add(y);
        }

        int targetYear = currentYear;
        if (!yearsInDb.isEmpty() && !yearsInDb.contains(currentYear)) {
            targetYear = Collections.max(yearsInDb);
        }
        
        Log.d(TAG, "Năm mục tiêu hiển thị: " + targetYear);
        textChartTitle.setText("Thống kê doanh thu năm " + targetYear);

        double yearTotal = 0, yearPaid = 0;
        Map<Integer, double[]> monthlyMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) monthlyMap.put(i, new double[]{0.0, 0.0});

        for (BillWithRoomAndTenant item : allBills) {
            String rawDate = item.bill.getMonthYear();
            int m = parseMonth(rawDate);
            int y = parseYear(rawDate);
            double amount = item.bill.getTotalAmount();

            Log.d(TAG, String.format("Hóa đơn: %s | Tiền: %.0f | Parsed: T%d/N%d", rawDate, amount, m, y));

            if (y == targetYear) {
                yearTotal += amount;
                if (item.bill.isPaid()) yearPaid += amount;

                if (m >= 1 && m <= 12) {
                    double[] vals = monthlyMap.get(m);
                    if (item.bill.isPaid()) vals[0] += amount;
                    else vals[1] += amount;
                } else {
                    Log.w(TAG, "Tháng không hợp lệ (m=" + m + ") cho chuỗi: " + rawDate);
                }
            }
        }

        Log.d(TAG, "KẾT QUẢ: Tổng=" + yearTotal + " | Đã thu=" + yearPaid);

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textTotalRevenue.setText(fmt.format(yearTotal));
        textPaidRevenue.setText(fmt.format(yearPaid));
        textUnpaidRevenue.setText(fmt.format(yearTotal - yearPaid));

        updateBarChart(monthlyMap);
        monthlyAdapter.setData(monthlyMap, targetYear);
    }

    private void updateBarChart(Map<Integer, double[]> monthlyMap) {
        List<BarEntry> paidEntries = new ArrayList<>();
        List<BarEntry> unpaidEntries = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            double[] v = monthlyMap.get(i);
            paidEntries.add(new BarEntry(i, (float) v[0]));
            unpaidEntries.add(new BarEntry(i, (float) v[1]));
        }

        BarDataSet paidDataSet = new BarDataSet(paidEntries, "Đã thu");
        paidDataSet.setColor(Color.parseColor("#10B981"));
        paidDataSet.setDrawValues(false);

        BarDataSet unpaidDataSet = new BarDataSet(unpaidEntries, "Chưa thu");
        unpaidDataSet.setColor(Color.parseColor("#EF4444"));
        unpaidDataSet.setDrawValues(false);

        float groupSpace = 0.2f;
        float barSpace = 0.02f;
        float barWidth = 0.38f;
        // (barWidth + barSpace) * 2 + groupSpace = 1.0

        BarData data = new BarData(paidDataSet, unpaidDataSet);
        data.setBarWidth(barWidth);

        barChart.setData(data);
        barChart.getXAxis().setAxisMinimum(1f);
        barChart.getXAxis().setAxisMaximum(13f);
        barChart.getXAxis().setLabelCount(12);
        barChart.groupBars(1f, groupSpace, barSpace);
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    // Parse tháng từ chuỗi có format dd/MM/yyyy hoặc MM/yyyy
    private int parseMonth(String str) {
        if (str == null) return -1;
        String[] parts = str.split("[^0-9]+"); // Tách các cụm số
        List<String> nums = new ArrayList<>();
        for (String p : parts) {
            if (!p.isEmpty()) nums.add(p);
        }
        if (nums.size() >= 3) {
            // Format dd/MM/yyyy -> lấy phần tử thứ 2 (index 1) là tháng
            try {
                int val = Integer.parseInt(nums.get(1));
                if (val >= 1 && val <= 12) return val;
            } catch (NumberFormatException e) { /* ignore */ }
        } else if (nums.size() == 2) {
            // Format MM/yyyy -> lấy phần tử đầu tiên là tháng
            try {
                int val = Integer.parseInt(nums.get(0));
                if (val >= 1 && val <= 12) return val;
            } catch (NumberFormatException e) { /* ignore */ }
        }
        return -1;
    }

    // Parse năm từ chuỗi có format dd/MM/yyyy hoặc MM/yyyy
    private int parseYear(String str) {
        if (str == null) return -1;
        String[] parts = str.split("[^0-9]+");
        List<String> nums = new ArrayList<>();
        for (String p : parts) {
            if (!p.isEmpty()) nums.add(p);
        }
        if (nums.size() >= 3) {
            // Format dd/MM/yyyy -> lấy phần tử thứ 3 (index 2) là năm
            try {
                int val = Integer.parseInt(nums.get(2));
                if (val > 100) return val;
            } catch (NumberFormatException e) { /* ignore */ }
        } else if (nums.size() == 2) {
            // Format MM/yyyy -> lấy phần tử thứ 2 là năm
            try {
                int val = Integer.parseInt(nums.get(1));
                if (val > 100) return val;
            } catch (NumberFormatException e) { /* ignore */ }
        }
        // Fallback: tìm số > 100 bất kỳ
        for (String p : parts) {
            if (p.isEmpty()) continue;
            try {
                int val = Integer.parseInt(p);
                if (val > 100) return val;
            } catch (NumberFormatException e) { /* ignore */ }
        }
        return -1;
    }

    static class MonthlyAdapter extends RecyclerView.Adapter<MonthlyAdapter.VH> {
        private List<Integer> months = new ArrayList<>();
        private Map<Integer, double[]> data = new HashMap<>();
        private int year;

        public void setData(Map<Integer, double[]> data, int year) {
            this.data = data;
            this.year = year;
            this.months.clear();
            for (int i = 12; i >= 1; i--) {
                double[] v = data.get(i);
                if (v != null && (v[0] > 0 || v[1] > 0)) {
                    months.add(i);
                }
            }
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = LayoutInflater.from(p.getContext()).inflate(android.R.layout.simple_list_item_2, p, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int p) {
            int m = months.get(p);
            double[] v = data.get(m);
            NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            h.t1.setText("Tháng " + m + "/" + year);
            h.t1.setTypeface(null, Typeface.BOLD);
            h.t2.setText("Đã thu: " + fmt.format(v[0]) + " | Chưa thu: " + fmt.format(v[1]));
            h.t1.setTextColor(Color.BLACK);
            h.t2.setTextColor(Color.DKGRAY);
        }

        @Override public int getItemCount() { return months.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView t1, t2;
            VH(View v) { super(v); t1 = v.findViewById(android.R.id.text1); t2 = v.findViewById(android.R.id.text2); }
        }
    }
}
