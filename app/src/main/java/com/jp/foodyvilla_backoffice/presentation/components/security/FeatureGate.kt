package com.jp.foodyvilla_backoffice.presentation.components.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.foodyvilla_backoffice.domain.repository.AuthRepository
import com.jp.foodyvilla_backoffice.domain.security.AppFeature
import com.jp.foodyvilla_backoffice.domain.security.UserSession
import org.koin.compose.koinInject

@Composable
fun FeatureGate(
    feature: AppFeature,
    modifier: Modifier = Modifier,
    session: UserSession? = null,
    authRepository: AuthRepository = koinInject(),
    deniedContent: @Composable (AppFeature) -> Unit = { AccessDeniedView(it) },
    content: @Composable () -> Unit
) {
    val repositorySession by authRepository.currentSession.collectAsStateWithLifecycle()
    val activeSession = session ?: repositorySession

    if (activeSession != null && feature.isAccessibleBy(activeSession)) {
        content()
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            deniedContent(feature)
        }
    }
}

@Composable
fun AccessDeniedView(
    feature: AppFeature,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(24.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "Access denied",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "You do not have permission to open ${feature.title}.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
