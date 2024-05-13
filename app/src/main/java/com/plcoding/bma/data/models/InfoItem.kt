package com.plcoding.bma.data.models

data class InfoItem(
    val name: String,
    val resId: Int,
    val url: String,
    val type: InfoType
) {
    enum class InfoType {
        LINK,
        EMAIL
    }
}
