package com.mgnegypt.previewmd.ui.navigation

import kotlinx.serialization.Serializable

/** First-launch welcome. Shown only while onboarding is not done. */
@Serializable
object WelcomeRoute

@Serializable
object HomeRoute

/** [uri] is the content URI of the file, as a string. */
@Serializable
data class ViewerRoute(val uri: String)

@Serializable
data class EditorRoute(val uri: String)

@Serializable
object SettingsRoute

@Serializable
object AboutRoute
