package com.blackmidori.expenses.android.ui.workspace

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.blackmidori.expenses.android.AppScreen
import com.blackmidori.expenses.android.MyApplicationTheme
import com.blackmidori.expenses.android.shared.ui.SimpleAppBar
import com.blackmidori.expenses.android.shared.ui.SimpleScaffold
import com.blackmidori.expenses.models.Payer
import com.blackmidori.expenses.repositories.PayerRepository
import com.blackmidori.expenses.stores.payerStorage
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Composable
fun AddPayerScreen(
    navController: NavHostController,
    workspaceId: String,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var name by remember {
        mutableStateOf("")
    }

    SimpleScaffold(topBar = {
        SimpleAppBar(
            navController = navController,
            title = { Text(stringResource(AppScreen.AddPayer.title)) },
        )
    }) {
        Column {
            TextField(value = name, onValueChange = {
                name = it
            })
            Button(onClick = {
                coroutineScope.launch {
                    val TAG = "AddPayerScreen.submit"
                    val payer = Payer("", Instant.DISTANT_PAST, workspaceId,name)
                    val payerResult =
                        PayerRepository(payerStorage()).add(workspaceId, payer)
                    if (payerResult.isFailure) {
                        Log.w(TAG, "Error: " + payerResult.exceptionOrNull())

                        coroutineScope.launch {
                            Toast.makeText(
                                context,
                                "Error: ${payerResult.exceptionOrNull()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        coroutineScope.launch {
                            Toast.makeText(
                                context,
                                "Added",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigateUp()
                        }
                    }
                }
            }) {
                Text("Submit")
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    MyApplicationTheme {
        AddPayerScreen(rememberNavController(),"fake")
    }
}