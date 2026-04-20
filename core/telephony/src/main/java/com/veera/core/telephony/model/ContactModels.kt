package com.veera.core.telephony.model

data class Contact(
    val id: String,
    val name: String,
    val number: String,
    val photoUri: String? = null,
    val accountName: String? = null,
    val accountType: String? = null,
    val initial: String = name.take(1).uppercase()
)

data class ContactAccount(
    val name: String,
    val type: String
)

enum class FilterType {
    ALL, PHONE, SIM, EMAIL
}
