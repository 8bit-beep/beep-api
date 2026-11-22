package com.b.beep.domain.room.repository

import com.b.beep.domain.room.domain.entity.RoomEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest(
    properties = [
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ]
)
class RoomBeaconMappingTest(
    @Autowired private val entityManager: TestEntityManager,
) {
    @Test
    fun `실을 생성할 때 지정한 비콘 값을 저장한다`() {
        val uuid = "e2c56db5-dffb-48d2-b060-d0f5a71096e0"
        val room = entityManager.persistFlushFind(
            RoomEntity(name = "1-1", beaconUuid = uuid, beaconMajor = 1, beaconMinor = 3)
        )

        assertEquals(uuid, room.beaconUuid)
        assertEquals(1, room.beaconMajor)
        assertEquals(3, room.beaconMinor)
    }

    @Test
    fun `실 정보 수정은 DB에 별도로 등록한 비콘 값을 보존한다`() {
        val room = entityManager.persistAndFlush(RoomEntity(name = "1-1"))
        val uuid = "e2c56db5-dffb-48d2-b060-d0f5a71096e0"

        entityManager.entityManager.createNativeQuery(
            "UPDATE rooms SET beacon_uuid = :uuid, beacon_major = 1, beacon_minor = 3 WHERE id = :roomId"
        )
            .setParameter("uuid", uuid)
            .setParameter("roomId", room.id)
            .executeUpdate()

        room.name = "1-1 수정"
        entityManager.flush()
        entityManager.clear()

        val updated = entityManager.find(RoomEntity::class.java, room.id)
        assertEquals("1-1 수정", updated.name)
        assertEquals(uuid, updated.beaconUuid)
        assertEquals(1, updated.beaconMajor)
        assertEquals(3, updated.beaconMinor)
    }
}
