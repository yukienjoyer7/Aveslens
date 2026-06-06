package com.example.aveslens.ui.navigation

object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val FORM = "form?observationId={observationId}"
    const val DETAIL = "detail/{observationId}"
    const val PROFILE = "profile"
    const val EXPLORER = "explorer"

    fun form(observationId: String? = null): String =
        if (observationId != null) "form?observationId=$observationId" else "form"

    fun detail(observationId: String): String = "detail/$observationId"
}
