package com.ryouonritsu.ic.common.exception

import com.ryouonritsu.ic.common.enums.ExceptionEnum

/**
 * @author ryouonritsu
 */
class ServiceException : RuntimeException {
    var code: String? = null
    override var message: String? = null

    constructor() : super()
    constructor(exceptionEnum: ExceptionEnum) : this() {
        this.code = exceptionEnum.code
        this.message = exceptionEnum.message
    }

    constructor(exceptionEnum: ExceptionEnum, message: String) : this() {
        this.code = exceptionEnum.code
        this.message = message
    }
}