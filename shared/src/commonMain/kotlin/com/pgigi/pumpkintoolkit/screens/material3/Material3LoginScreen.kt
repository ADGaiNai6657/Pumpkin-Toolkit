package com.pgigi.pumpkintoolkit.screens.material3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.components.material3.M3TextField
import com.pgigi.pumpkintoolkit.utils.QZClient
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Show

@Composable
fun Material3LoginScreen() {
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val client = QZClient
    val snackbarHostState = remember { SnackbarHostState() }
    var logging by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    var username by remember { mutableStateOf(AppConfig.username) }
    var password by remember { mutableStateOf(AppConfig.password) }
    var passwordVisible by remember { mutableStateOf(false) }

    suspend fun doLogin() {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            snackbarHostState.showSnackbar(
                message = "请填写用户名和密码",
                withDismissAction = true
            )
            return
        }
        logging = true
        client.username = username
        client.password = password
        client.login(
            onSuccess = {
                AppConfig.username = username
                AppConfig.password = password
                AppConfig.save()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "登录成功",
                        withDismissAction = true
                    )
                }
                logging = false
                navigator.pop()
            },
            onFailure = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = it,
                        withDismissAction = true
                    )
                }
                logging = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "教务系统登录",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                M3TextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "学号",
                    leadingIcon = {
                        Icon(
                            MiuixIcons.Contacts,
                            contentDescription = "学号",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.padding(bottom = 12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                    )
                )

                M3TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "密码",
                    leadingIcon = {
                        Icon(
                            MiuixIcons.Lock,
                            contentDescription = "密码",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(
                                imageVector = if (passwordVisible) MiuixIcons.Show else MiuixIcons.Hide,
                                contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                tint = if (passwordVisible) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            coroutineScope.launch {
                                doLogin()
                            }
                        }
                    ),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            doLogin()
                        }
                    },
                    enabled = !logging,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!logging) {
                        Text("登录")
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}
