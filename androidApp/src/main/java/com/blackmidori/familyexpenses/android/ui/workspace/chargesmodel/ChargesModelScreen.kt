package com.blackmidori.familyexpenses.android.ui.workspace.chargesmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.blackmidori.familyexpenses.android.AppScreen
import com.blackmidori.familyexpenses.android.MyApplicationTheme
import com.blackmidori.familyexpenses.android.R
import com.blackmidori.familyexpenses.android.core.HttpClientJavaImpl
import com.blackmidori.familyexpenses.android.shared.ui.SimpleAppBar
import com.blackmidori.familyexpenses.android.shared.ui.SimpleScaffold
import com.blackmidori.familyexpenses.models.ChargeAssociation
import com.blackmidori.familyexpenses.models.ChargesModel
import com.blackmidori.familyexpenses.repositories.ChargeAssociationRepository
import com.blackmidori.familyexpenses.repositories.ChargesModelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChargesModelScreen(
    navController: NavHostController,
    chargesModelId: String,
    workspaceId: String,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var name by remember {
        mutableStateOf("")
    }
    var list by remember {
        mutableStateOf(arrayOf<ChargeAssociation>())
    }
    LaunchedEffect(key1 = null) {
        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show()
        fetchChargesModelAsync(chargesModelId, coroutineScope, context) {
            name = it.name
        }
        fetchChargeAssociationsAsync(chargesModelId, coroutineScope, context) {
            list = it
        }
    }

    val onAddClick = {
        navController.navigate(
            AppScreen.AddChargeAssociation.route.replace(
                "{chargesModelId}", chargesModelId
            ).replace(
                "{workspaceId}", workspaceId
            )
        )
    }
    val onOpenClick: (id: String) -> Unit = {
        navController.navigate(
            AppScreen.ChargeAssociation.route.replace(
                "{id}", it
            ).replace(
                "{workspaceId}", workspaceId
            )
        )
    }
    val onUpdateClick: (id: String) -> Unit = {
        navController.navigate(
            AppScreen.UpdateChargeAssociation.route.replace(
                "{id}", it
            ).replace(
                "{workspaceId}", workspaceId
            )
        )
    }

    val onCalculateClick={
        navController.navigate(
            AppScreen.Calculation.route.replace(
                "{chargesModelId}", chargesModelId
            ).replace(
                "{workspaceId}", workspaceId
            )
        )
    }

    SimpleScaffold(
        topBar = {
            SimpleAppBar(
                navController = navController,
                title = { Text(stringResource(AppScreen.ChargesModel.title) + " - $name") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_charge_association)
                )
            }
        }
    ) {
        LazyColumn {
            item {
                Button(
                    modifier = Modifier.width(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White
                    ),
                    onClick = onCalculateClick
                ) {
                    Text("Calculate charges for expenses")
                }
            }
            item { Text("Charge Association Count: ${list.size}") }
            for (item in list) {
                item {
                    ListItem(
                        modifier = Modifier.clickable {
                            onOpenClick(item.id)
                        },
                        headlineContent = { Text(item.name) },
                        supportingContent = { Text(item.creationDateTime.toString()) },
                        trailingContent = {
                            IconButton({
                                onUpdateClick(item.id)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = stringResource(R.string.update_charge_association)
                                )
                            }

                        },
                    )
                }
            }
        }
    }
}

private fun fetchChargesModelAsync(
    chargesModelId: String,
    coroutineScope: CoroutineScope,
    context: Context,
    onSuccess: (ChargesModel) -> Unit
) {
    val TAG = "ChargesModelScreen.fetchChargesModelAsync"
    Thread {
        val chargesModelResult =
            ChargesModelRepository(httpClient = HttpClientJavaImpl()).getOne(chargesModelId)
        if (chargesModelResult.isFailure) {
            Log.w(TAG, "Error: " + chargesModelResult.exceptionOrNull())
            coroutineScope.launch {
                Toast.makeText(
                    context,
                    "Error: ${chargesModelResult.exceptionOrNull()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return@Thread;
        }
        coroutineScope.launch {
            Toast.makeText(context, "Loaded", Toast.LENGTH_SHORT)
                .show()
        }
        onSuccess(chargesModelResult.getOrNull()!!)
    }.start()
}

private fun fetchChargeAssociationsAsync(
    chargesModelId: String,
    coroutineScope: CoroutineScope,
    context: Context,
    onSuccess: (Array<ChargeAssociation>) -> Unit
) {
    val TAG = "ChargesModelScreen.fetchChargesModelsAsync"
    Thread {
        val chargeAssociationResult =
            ChargeAssociationRepository(httpClient = HttpClientJavaImpl()).getPagedList(
                chargesModelId
            )
        if (chargeAssociationResult.isFailure) {
            Log.w(TAG, "Error: " + chargeAssociationResult.exceptionOrNull())
            coroutineScope.launch {
                Toast.makeText(
                    context,
                    "Error: ${chargeAssociationResult.exceptionOrNull()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return@Thread;
        }
        coroutineScope.launch {
            Toast.makeText(context, "List Updated", Toast.LENGTH_SHORT).show()
        }
        onSuccess(chargeAssociationResult.getOrNull()!!.results)
    }.start()
}

@Preview
@Composable
private fun Preview() {
    MyApplicationTheme {
        ChargesModelScreen(rememberNavController(), "fake","")
    }
}