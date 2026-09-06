package com.eloverde.admin.domain

enum class ReservationStatus(val wireValue: String) {
    PENDING("Pendente contato"),
    RESERVED("Reservado"),
    PAID("Quitado"),
    VISIT("Visita");

    val label: String get() = wireValue
    val blocksDate: Boolean get() = this == RESERVED || this == PAID

    companion object {
        fun from(value: String?): ReservationStatus =
            entries.firstOrNull {
                it.wireValue.equals(value, ignoreCase = true) ||
                    it.name.equals(value, ignoreCase = true)
            } ?: PENDING
    }
}
