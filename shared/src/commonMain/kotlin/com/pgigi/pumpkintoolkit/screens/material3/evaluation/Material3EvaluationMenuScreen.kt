package com.pgigi.pumpkintoolkit.screens.material3.evaluation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.components.material3.M3GroupSection
import com.pgigi.pumpkintoolkit.components.material3.M3Row
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.viewmodel.EvaluationViewModel
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3EvaluationMenuScreen(
    viewModel: EvaluationViewModel = viewModel(factory = EvaluationViewModel.Factory)
) {
    val navigator = LocalNavigator.current
    var loading by remember { mutableStateOf(!viewModel.menuLoaded) }

    LaunchedEffect(Unit) {
        if (!viewModel.menuLoaded) {
            loading = true
            val list = QZClient.getEvaluationMenuHItems()
            list?.let {
                viewModel.menuList.clear()
                viewModel.menuList.addAll(it)
            }
            viewModel.menuLoaded = true
            loading = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(text = "学生评教", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(viewModel.menuList.size) { index ->
                    val item = viewModel.menuList[index]
                    M3GroupSection {
                        M3Row(
                            title = item.evaluationName,
                            summary = item.termName,
                            onClick = {
                                navigator.push(Route.EvaluationList(item.actionUrl, item.evaluationName))
                            },
                            showDivider = false
                        )
                    }
                }
                if (!loading && viewModel.menuList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无评教数据",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
