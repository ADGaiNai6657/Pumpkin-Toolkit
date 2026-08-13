package com.pgigi.pumpkintoolkit.screens.miuix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pgigi.pumpkintoolkit.AppConfig
import com.pgigi.pumpkintoolkit.LocalNavigator
import com.pgigi.pumpkintoolkit.Route
import com.pgigi.pumpkintoolkit.utils.QZClient
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonColors
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixLoginScreen(){
    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val client = QZClient
    val snackbarHostState = remember{ SnackbarHostState() }
    var logging by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        /*topBar = {
            SmallTopAppBar(
                title = "登录",
                navigationIcon = {
                    IconButton(onClick = {
                        navigator.pop()
                    }){
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                        )
                    }
                }
            )
        }*/
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MiuixTheme.colorScheme.background)
        ) {
            Card(Modifier.align(Alignment.Center)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    var username by remember { mutableStateOf(AppConfig.username) }
                    Text(
                        text = "教务系统登录",
                        fontSize = MiuixTheme.textStyles.title1.fontSize,
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "学号",
                        leadingIcon = {
                            Icon(
                                imageVector = MiuixIcons.Contacts,
                                contentDescription = "学号",
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        },
                        modifier = Modifier.padding(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    var password by remember { mutableStateOf(AppConfig.password) }
                    var passwordVisible by remember { mutableStateOf(false) }

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "密码",
                        leadingIcon = {
                            Icon(
                                imageVector = MiuixIcons.Lock,
                                contentDescription = "密码",
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        },
                        modifier = Modifier.padding(8.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) MiuixIcons.Show else MiuixIcons.Hide,
                                    tint = if (passwordVisible) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer,
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                                )
                            }
                        }
                    )
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (username.trim().isEmpty() || password.trim()
                                        .isEmpty()
                                ) {
                                    snackbarHostState.showSnackbar(
                                        message = "请填写用户名和密码",
                                        withDismissAction =  true
                                    )
                                    return@launch
                                }
                                logging = true
                                client.username = username
                                client.password = password
                                client.login(
                                    onSuccess = {
                                        AppConfig.username = username
                                        AppConfig.password = password
                                        AppConfig.save()
                                        coroutineScope.launch{
                                            snackbarHostState.showSnackbar(
                                                message = "登录成功",
                                                withDismissAction = true
                                            )
                                        }
                                        logging = false
                                        navigator.replace(Route.Home)
                                    },
                                    onFailure = {
                                        coroutineScope.launch{
                                            snackbarHostState.showSnackbar(
                                                message = it,
                                                withDismissAction = true
                                            )
                                        }
                                        logging = false
                                    }
                                )
                            }
                        },
                        enabled = !logging,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        colors = ButtonColors(
                            color = MiuixTheme.colorScheme.primary,
                            disabledColor = MiuixTheme.colorScheme.disabledPrimary,
                            contentColor = MiuixTheme.colorScheme.onPrimary,
                            disabledContentColor = MiuixTheme.colorScheme.onPrimary,
                        )
                    ) {
                        if (!logging) {
                            Text(
                                text = "登录",
                                color = MiuixTheme.colorScheme.onPrimary
                            )
                        } else {
                            InfiniteProgressIndicator(
                                modifier = Modifier.size(
                                    22.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}