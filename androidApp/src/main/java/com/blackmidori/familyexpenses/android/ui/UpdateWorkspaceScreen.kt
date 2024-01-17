package com.blackmidori.familyexpenses.android.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.blackmidori.familyexpenses.android.AppScreen
import com.blackmidori.familyexpenses.android.MyApplicationTheme
import com.blackmidori.familyexpenses.android.core.HttpClientJavaImpl
import com.blackmidori.familyexpenses.android.shared.ui.SimpleAppBar
import com.blackmidori.familyexpenses.android.shared.ui.SimpleScaffold
import com.blackmidori.familyexpenses.models.Workspace
import com.blackmidori.familyexpenses.repositories.WorkspaceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Composable
fun UpdateWorkspaceScreen(
    navController: NavHostController,
    workspaceId:String,
    onSuccess: ()->Unit,
){
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var name by remember {
        mutableStateOf("")
    }

    LaunchedEffect(key1 = null) {
        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show()
        fetchWorkspaceAsync(workspaceId, coroutineScope, context) {
            name = it.name
        }
    }
    SimpleScaffold(topBar = {
        SimpleAppBar(
            navController = navController,
            title = { Text(stringResource(AppScreen.UpdateWorkspace.title)) },
        )
    }) {
        Column {
            TextField(value = name, onValueChange = {
                name = it
            })
            Button(onClick = {
                Thread {
                    val workspace = Workspace(workspaceId, Instant.DISTANT_PAST, name)
                    val workspaceResult =
                        WorkspaceRepository(httpClient = HttpClientJavaImpl()).update(workspace)
                    if (workspaceResult.isFailure) {
                        Log.w(
                            "UpdateWorkspaceScreen",
                            "Error: " + workspaceResult.exceptionOrNull()
                        )

                        coroutineScope.launch {
                            Toast.makeText(
                                context,
                                "Error: ${workspaceResult.exceptionOrNull()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        coroutineScope.launch {
                            Toast.makeText(
                                context,
                                "Updated",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigateUp()
                            onSuccess()
                        }
                    }
                }.start()
            }) {
                Text("Submit")
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3737)),
                onClick = {
                Thread {
                    val workspace = Workspace(workspaceId, Instant.DISTANT_PAST, name)
                    val deleteResult =
                        WorkspaceRepository(httpClient = HttpClientJavaImpl()).delete(workspace)
                    if (deleteResult.isFailure) {
                        Log.w("DeleteWorkspaceScreen", "Error: " + deleteResult.exceptionOrNull())

                        coroutineScope.launch {
                            Toast.makeText(
                                context,
                                "Error: ${deleteResult.exceptionOrNull()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        coroutineScope.launch {
                            Toast.makeText(
                                context,
                                "Deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigateUp()
                            onSuccess()
                        }
                    }
                }.start()
            }) {
                Text("Delete")
            }
        }
    }
}

private fun fetchWorkspaceAsync(
    workspaceId: String,
    coroutineScope: CoroutineScope,
    context: Context,
    onSuccess: (Workspace) -> Unit
) {
    val TAG = "fetchWorkspacesAsync"
    Thread {
        val workspaceResult =
            WorkspaceRepository(httpClient = HttpClientJavaImpl()).getOne(workspaceId)
        if (workspaceResult.isFailure) {
            Log.w(TAG, "Error: " + workspaceResult.exceptionOrNull())
            coroutineScope.launch {
                Toast.makeText(
                    context,
                    "Error: ${workspaceResult.exceptionOrNull()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return@Thread;
        }
        coroutineScope.launch {
            Toast.makeText(context, "Loaded", Toast.LENGTH_SHORT)
                .show()
        }
        onSuccess(workspaceResult.getOrNull()!!)
    }.start()
}

@Preview
@Composable
private fun Preview() {
    MyApplicationTheme {
        UpdateWorkspaceScreen(rememberNavController(),workspaceId = "fake",{})
    }
}