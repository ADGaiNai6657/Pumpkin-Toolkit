package com.pgigi.pumpkintoolkit.screens.material3

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.components.isLiquidGlassSupported
import com.pgigi.pumpkintoolkit.components.material3.M3Card
import com.pgigi.pumpkintoolkit.components.material3.M3FloatingDropdown
import com.pgigi.pumpkintoolkit.utils.QZClient
import com.pgigi.pumpkintoolkit.viewmodel.PlanViewModel
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3PlanScreen(viewModel: PlanViewModel = viewModel(factory = PlanViewModel.Factory)) {
    val navigator = LocalNavigator.current
    val hapticFeedback = LocalHapticFeedback.current
    var loading by remember { mutableStateOf(false) }

    val liquidGlassSupported = isLiquidGlassSupported()
    val useBlur = AppConfig.enableBlurEffect && liquidGlassSupported
    val backdrop = if (useBlur) rememberLayerBackdrop() else null
    val containerColor = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("课程执行计划") },
                navigationIcon = {
                    IconButton(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        navigator.pop()
                    }) {
                        Icon(MiuixIcons.Back, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
//                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
//                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                items(viewModel.planList.size) { index ->
                    AnimatedVisibility(
                        viewModel.selectedTerm == 0 ||
                                viewModel.planList[index].term == viewModel.termNameList[viewModel.selectedTerm]
                    ) {
                        M3PlanCard(
                            modifier = Modifier.padding(vertical = 4.dp),
                            plan = viewModel.planList[index]
                        )
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

            if (viewModel.termNameList.size > 1) {
                M3FloatingDropdown(
                    label = viewModel.termNameList.getOrElse(viewModel.selectedTerm) { "全部学期" },
                    items = viewModel.termNameList.toList(),
                    selectedIndex = viewModel.selectedTerm,
                    onSelect = { viewModel.selectedTerm = it },
                    modifier = Modifier.fillMaxSize(),
                    backdrop = backdrop,
                    containerColor = containerColor
                )
            }
        }
    }

    if (viewModel.planList.isEmpty()) {
        LaunchedEffect(Unit) {
            loading = true
            val planList = QZClient.getCoursePlan()
            if (!planList.isNullOrEmpty()) {
                viewModel.planList.clear()
                viewModel.planList.addAll(planList.reversed())
                val termList = planList.map { it.term }.distinct().sortedDescending()
                viewModel.termNameList.clear()
                viewModel.termNameList.add("全部学期")
                viewModel.termNameList.addAll(termList)
            }
            loading = false
        }
    }
}

@Composable
private fun M3PlanCard(modifier: Modifier = Modifier, plan: com.pgigi.pumpkintoolkit.models.CoursePlan) {
    var expand by remember { mutableStateOf(false) }
    M3Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { expand = !expand })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "学分: ${plan.credit}   总课时: ${plan.hours}课时",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = plan.evaluation,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = plan.attr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        AnimatedVisibility(visible = expand) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "课程编号: ${plan.num}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "开课学期: ${plan.term}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "课程性质: ${plan.nature}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "开课单位: ${plan.institution}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
