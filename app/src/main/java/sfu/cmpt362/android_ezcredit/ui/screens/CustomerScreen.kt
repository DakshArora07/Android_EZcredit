package sfu.cmpt362.android_ezcredit.ui.screens

import android.R.attr.icon
import android.R.attr.onClick
import android.R.id.icon
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sfu.cmpt362.android_ezcredit.R

@Preview
@Composable
fun CustomerScreen() {

    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(10.dp)
    ) {
        Column {
            Row (modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically)
            {
                Column {
                    Text(
                        text = stringResource(R.string.customers),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.customerScreenSubHeading),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                FloatingActionButton(
                    onClick = {
                        Toast.makeText(context, "Button Clicked!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Customer"
                    )
                }
            }

            DemoCustomers()
        }
    }
}

@Composable
fun DemoCustomers(){

    val demoCustomers = listOf(
        Triple("ABC Company", "abccompany@gmail.com", "85"),
        Triple("XYZ Enterprise", "xyzenterprice@outlook.com","74")
    )

    LazyColumn (
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 20.dp)
    ) {
        items(demoCustomers){ customers->
            CustomerCard(
                name = customers.first,
                email = customers.second,
                score = customers.third,
                onClick = {

                }
            )
        }
    }
}

@Composable
fun CustomerCard(name: String, email: String, score: String, onClick: () -> Unit){

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Customer Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                Text(text = email, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Credit Score: $score",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

}