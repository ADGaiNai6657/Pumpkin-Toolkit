package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.models.OssLicense
import com.pgigi.pumpkintoolkit.models.getLicenseUrl
import com.pgigi.pumpkintoolkit.utils.ResourceUtils
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OssLicenseDetailScreen(license: OssLicense) {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current

    val licenseText = remember(license) {
        license.file?.let { ResourceUtils.readText(it) }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = license.title,
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            item {
                Text(
                    text = license.licence,
                    color = MiuixTheme.colorScheme.primary,
                    fontSize = MiuixTheme.textStyles.title2.fontSize,
                    modifier = Modifier.clickable {
                        getLicenseUrl(license.licence)?.let { uriHandler.openUri(it) }
                    }
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = license.author,
//                    color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = license.link,
                    color = MiuixTheme.colorScheme.primary,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                    modifier = Modifier.clickable {
                        uriHandler.openUri(license.link)
                    }
                )
                Spacer(modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MiuixTheme.colorScheme.onSurfaceVariantActions.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = licenseText ?: "无法加载许可证文本",
//                    color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    fontSize = MiuixTheme.textStyles.body2.fontSize,
                )
            }
        }
    }
}
