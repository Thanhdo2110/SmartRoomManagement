package com.example.smartroommanagement.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Objects;

@Entity(
    tableName = "tenants",
    foreignKeys = @ForeignKey(
        entity = RoomEntity.class,
        parentColumns = "id",
        childColumns = "roomId",
        onDelete = ForeignKey.SET_NULL
    ),
    indices = {@Index("roomId")}
)
public class TenantEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String phone;
    private String identityCard; // CCCD
    private String birthDate;    // Ngày sinh
    private String hometown;     // Quê quán
    private Integer roomId;
    private String startDate;    // Ngày bắt đầu thuê
    private double deposit;      // Tiền đặt cọc
    private Integer contractTerm; // Thời hạn hợp đồng (tháng)
    private int userId; // ID của chủ trọ sở hữu khách thuê này

    public TenantEntity(String name, String phone, String identityCard, String birthDate, String hometown, Integer roomId, String startDate, double deposit, int userId) {
        this.name = name;
        this.phone = phone;
        this.identityCard = identityCard;
        this.birthDate = birthDate;
        this.hometown = hometown;
        this.roomId = roomId;
        this.startDate = startDate;
        this.deposit = deposit;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getIdentityCard() { return identityCard; }
    public void setIdentityCard(String identityCard) { this.identityCard = identityCard; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getHometown() { return hometown; }
    public void setHometown(String hometown) { this.hometown = hometown; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }

    public Integer getContractTerm() { return contractTerm; }
    public void setContractTerm(Integer contractTerm) { this.contractTerm = contractTerm; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantEntity that = (TenantEntity) o;
        return id == that.id &&
                userId == that.userId &&
                Double.compare(that.deposit, deposit) == 0 &&
                Objects.equals(name, that.name) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(identityCard, that.identityCard) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(contractTerm, that.contractTerm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, phone, identityCard, startDate, deposit, contractTerm, userId);
    }
}
