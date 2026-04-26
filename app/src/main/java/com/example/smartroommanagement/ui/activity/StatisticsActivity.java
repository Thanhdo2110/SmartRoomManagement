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

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setAxisMinimum(0.5f);
        xAxis.setAxisMaximum(12.5f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "T" + (int) value;
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        
        barChart.getAxisRight().setEnabled(false);
        barChart.setAutoScaleMinMaxEnabled(true); // Tự động căn chỉnh trục Y theo giá trị tiền
        
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
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            double[] v = monthlyMap.get(i);
            entries.add(new BarEntry(i, new float[]{(float) v[0], (float) v[1]}));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        dataSet.setColors(Color.parseColor("#10B981"), Color.parseColor("#EF4444"));
        dataSet.setStackLabels(new String[]{"Đã thu", "Chưa thu"});
        dataSet.setDrawValues(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        
        barChart.setData(data);
        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    // Hàm parse mới: cực kỳ mạnh mẽ, tách mọi ký tự đặc biệt
    private int parseMonth(String str) {
        if (str == null) return -1;
        String[] parts = str.split("[^0-9]+"); // Chỉ lấy các cụm số
        for (String p : parts) {
            if (p.isEmpty()) continue;
            int val = Integer.parseInt(p);
            if (val >= 1 && val <= 12 && p.length() <= 2) return val;
        }
        return -1;
    }

    private int parseYear(String str) {
        if (str == null) return -1;
        String[] parts = str.split("[^0-9]+");
        for (String p : parts) {
            if (p.isEmpty()) continue;
            int val = Integer.parseInt(p);
            if (val > 100) return val; // Năm thường là số lớn (2024, 2026...)
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
