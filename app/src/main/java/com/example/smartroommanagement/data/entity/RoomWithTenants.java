package com.example.smartroommanagement.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class RoomWithTenants {
    @Embedded
    public RoomEntity room;

    @Relation(
        parentColumn = "id",
        entityColumn = "roomId"
    )
    public List<TenantEntity> tenants;
}
