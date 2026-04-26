package com.example.smartroommanagement.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TenantWithRoom {
    @Embedded
    public TenantEntity tenant;

    @Relation(
        parentColumn = "roomId",
        entityColumn = "id"
    )
    public RoomEntity room;
}
