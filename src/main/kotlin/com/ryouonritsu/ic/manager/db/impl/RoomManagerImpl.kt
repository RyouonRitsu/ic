package com.ryouonritsu.ic.manager.db.impl

import com.ryouonritsu.ic.manager.db.RoomManager
import com.ryouonritsu.ic.repository.RoomRepository
import org.springframework.stereotype.Component

/**
 * @author PaulManstein
 */
@Component
class RoomManagerImpl(
    private val roomRepository: RoomRepository
) : RoomManager