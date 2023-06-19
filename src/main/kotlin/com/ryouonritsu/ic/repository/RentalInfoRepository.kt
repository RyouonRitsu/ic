package com.ryouonritsu.ic.repository


import com.ryouonritsu.ic.entity.RentalInfo
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

@Repository
interface RentalInfoRepository : JpaRepositoryImplementation<RentalInfo, Long> {

}