package com.pgigi.pumpkintoolkit.screens.miuix.evaluation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.viewmodel.EvaluationViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixEvaluationMenuScreen(
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
        topBar = {
            SmallTopAppBar(
                title = "学生评教",
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        val cardPadding = PaddingValues(12.dp, 6.dp)
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.menuList.size) { index ->
                    val item = viewModel.menuList[index]
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(cardPadding),
                        onClick = {
                            navigator.push(Route.EvaluationList(item.actionUrl, item.evaluationName))
                        }
                    ) {
                        BasicComponent {
                            Text(text = item.evaluationName)
                            Text(
                                text = item.termName,
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
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
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
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
                    InfiniteProgressIndicator()
                }
            }
        }
    }
}
