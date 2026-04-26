package com.example.smartroommanagement.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Objects;

@Entity(tableName = "rooms")
public class RoomEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private double basePrice;
    private String status; // "Trống", "Đã thuê"
    private String description;

    public RoomEntity(String name, double basePrice, String status, String description) {
        this.name = name;
        this.basePrice = basePrice;
        this.status = status;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomEntity that = (RoomEntity) o;
        return id == that.id &&
                Double.compare(that.basePrice, basePrice) == 0 &&
                Objects.equals(name, that.name) &&
                Objects.equals(status, that.status) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, basePrice, status, description);
    }
}
