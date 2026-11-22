package com.b.beep.domain.room.service

import com.b.beep.domain.room.controller.dto.response.BeaconResponse
import com.b.beep.domain.room.domain.entity.RoomEntity
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BeaconService {
    fun getBeacon(room: RoomEntity): BeaconResponse? {
        val uuid = room.beaconUuid ?: return null
        val major = room.beaconMajor ?: return null
        val minor = room.beaconMinor ?: return null
        if (major !in COMPONENT_RANGE || minor !in COMPONENT_RANGE) return null

        val canonicalUuid = try {
            UUID.fromString(uuid).toString()
        } catch (_: IllegalArgumentException) {
            return null
        }
        if (!canonicalUuid.equals(uuid, ignoreCase = true)) return null

        return BeaconResponse(uuid = canonicalUuid, major = major, minor = minor)
    }

    private companion object {
        val COMPONENT_RANGE = 0..65_535
    }
}
