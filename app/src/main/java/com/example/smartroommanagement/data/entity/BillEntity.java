package com.example.smartroommanagement.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "bills",
    foreignKeys = @ForeignKey(
        entity = RoomEntity.class,
        parentColumns = "id",
        childColumns = "roomId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("roomId")}
)
public class BillEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int roomId;
    private String tenantName; // Snapshot của tên khách thuê tại thời điểm tạo hóa đơn
    private String monthYear; // e.g., "05/2024"
    private double electricityUsage;
    private double waterUsage;
    private double totalAmount;
    private boolean isPaid;
    
    // Thêm các trường tiện ích mới
    private double laundryFee;    // Phí máy giặt
    private double trashFee;      // Phí rác
    private double wifiFee;       // Phí wifi
    private double otherFee;      // Phí khác
    private String otherFeeNote;  // Chú thích cho phí khác
    
    private int userId; // ID của chủ trọ sở hữu hóa đơn này

    public BillEntity(int roomId, String monthYear, double electricityUsage, double waterUsage, double totalAmount, boolean isPaid, int userId) {
        this.roomId = roomId;
        this.monthYear = monthYear;
        this.electricityUsage = electricityUsage;
        this.waterUsage = waterUsage;
        this.totalAmount = totalAmount;
        this.isPaid = isPaid;
        this.userId = userId;
    }

    @Ignore
    public BillEntity(BillEntity other) {
        this.id = other.id;
        this.roomId = other.roomId;
        this.tenantName = other.tenantName;
        this.monthYear = other.monthYear;
        this.electricityUsage = other.electricityUsage;
        this.waterUsage = other.waterUsage;
        this.totalAmount = other.totalAmount;
        this.isPaid = other.isPaid;
        this.laundryFee = other.laundryFee;
        this.trashFee = other.trashFee;
        this.wifiFee = other.wifiFee;
        this.otherFee = other.otherFee;
        this.otherFeeNote = other.otherFeeNote;
        this.userId = other.userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public double getElectricityUsage() { return electricityUsage; }
    public void setElectricityUsage(double electricityUsage) { this.electricityUsage = electricityUsage; }

    public double getWaterUsage() { return waterUsage; }
    public void setWaterUsage(double waterUsage) { this.waterUsage = waterUsage; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public double getLaundryFee() { return laundryFee; }
    public void setLaundryFee(double laundryFee) { this.laundryFee = laundryFee; }

    public double getTrashFee() { return trashFee; }
    public void setTrashFee(double trashFee) { this.trashFee = trashFee; }

    public double getWifiFee() { return wifiFee; }
    public void setWifiFee(double wifiFee) { this.wifiFee = wifiFee; }

    public double getOtherFee() { return otherFee; }
    public void setOtherFee(double otherFee) { this.otherFee = otherFee; }

    public String getOtherFeeNote() { return otherFeeNote; }
    public void setOtherFeeNote(String otherFeeNote) { this.otherFeeNote = otherFeeNote; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
