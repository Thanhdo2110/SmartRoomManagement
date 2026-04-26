package com.example.smartroommanagement.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class BillWithRoomAndTenant {
    @Embedded
    public BillEntity bill;

    @Relation(
        parentColumn = "roomId",
        entityColumn = "id"
    )
    public RoomEntity room;

    @Relation(
        parentColumn = "roomId",
        entityColumn = "roomId"
    )
    public TenantEntity tenant;
}
