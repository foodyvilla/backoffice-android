package com.jp.foodyvilla_backoffice.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jp.foodyvilla_backoffice.domain.security.AppFeature
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import com.jp.foodyvilla_backoffice.presentation.components.security.AccessDeniedView

inline fun <reified T : Any> NavGraphBuilder.guardedComposable(
    feature: AppFeature,
    session: UserSession?,
    crossinline content: @Composable (NavBackStackEntry) -> Unit
) {
    composable<T> { backStackEntry ->
        if (session != null && feature.isAccessibleBy(session)) {
            content(backStackEntry)
        } else {
            AccessDeniedView(feature = feature)
        }
    }
}
