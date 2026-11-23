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
    @Autowired private val roomRepository: RoomRepository,
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
    fun `등록된 비콘 값을 조회한 후 실 이름을 수정해도 비콘 값이 유지된다`() {
        val roomId = entityManager.persistAndFlush(RoomEntity(name = "1-1")).id
        val uuid = "e2c56db5-dffb-48d2-b060-d0f5a71096e0"

        entityManager.entityManager.createNativeQuery(
            "UPDATE rooms SET beacon_uuid = :uuid, beacon_major = 1, beacon_minor = 3 WHERE id = :roomId"
        )
            .setParameter("uuid", uuid)
            .setParameter("roomId", roomId)
            .executeUpdate()

        entityManager.clear()
        val room = entityManager.find(RoomEntity::class.java, roomId)
        room.name = "1-1 수정"
        entityManager.flush()
        entityManager.clear()

        val updated = entityManager.find(RoomEntity::class.java, roomId)
        assertEquals("1-1 수정", updated.name)
        assertEquals(uuid, updated.beaconUuid)
        assertEquals(1, updated.beaconMajor)
        assertEquals(3, updated.beaconMinor)
    }

    @Test
    fun `반 조회에서 삭제된 실과 학년 또는 반이 없는 실을 제외한다`() {
        listOf(
            RoomEntity(name = "1-1", grade = 1, classNumber = 1),
            RoomEntity(name = "1-2", grade = 1, classNumber = 2, isDeleted = true),
            RoomEntity(name = "학년 공용실", grade = 1),
            RoomEntity(name = "학년 미지정실", classNumber = 3),
        ).forEach { entityManager.persist(it) }

        val rooms = roomRepository.findAllByGradeIsNotNullAndClassNumberIsNotNullAndIsDeletedFalse()

        assertEquals(listOf("1-1"), rooms.map { it.name })
    }
}
