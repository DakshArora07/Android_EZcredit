package sfu.cmpt362.android_ezcredit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import sfu.cmpt362.android_ezcredit.ui.viewmodel.CalendarScreenViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DueDate(
    val date: Int,
    val month: Int,
    val year: Int
)

@Composable
fun CalendarScreen(
    calendarScreenViewModel: CalendarScreenViewModel = viewModel(),
    invoiceViewModel: InvoiceViewModel,
    onNavigateToInvoice: (Long) -> Unit = {}
) {
    val today = calendarScreenViewModel.today
    val currentDate = calendarScreenViewModel.currentDate
    val selectedDate = calendarScreenViewModel.selectedDate

    val allInvoices by invoiceViewModel.invoicesLiveData.observeAsState(emptyList())

    // Group invoices by date for current month/year
    val invoicesByDate = remember(allInvoices, currentDate) {
        allInvoices.filter { invoice ->
            val dueDate = invoice.dueDate
            dueDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                    dueDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
        }.groupBy { invoice ->
            invoice.dueDate.get(Calendar.DAY_OF_MONTH)
        }
    }

    // Get invoices for selected date
    val selectedInvoices = remember(selectedDate, allInvoices) {
        if (selectedDate != null) {
            allInvoices.filter { invoice ->
                val dueDate = invoice.dueDate
                dueDate.get(Calendar.DAY_OF_MONTH) == selectedDate.date &&
                        dueDate.get(Calendar.MONTH) == selectedDate.month &&
                        dueDate.get(Calendar.YEAR) == selectedDate.year
            }
        } else {
            emptyList()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isVertical = maxWidth < 600.dp

        if (isVertical) {
            // Vertical Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Header()
                CalendarCard(
                    currentDate = currentDate,
                    selectedDate = selectedDate,
                    today = today,
                    invoicesByDate = invoicesByDate,
                    onDateChange = { newDate -> calendarScreenViewModel.updateCurrentDate(newDate) },
                    onDayClick = { day ->
                        calendarScreenViewModel.updateSelectedDate(
                            DueDate(
                                day,
                                currentDate.get(Calendar.MONTH),
                                currentDate.get(Calendar.YEAR)
                            )
                        )
                    }
                )

                // Invoice details below calendar
                if (selectedDate != null) {
                    InvoiceDetailsCard(
                        selectedDate = selectedDate,
                        invoices = selectedInvoices,
                        onInvoiceClick = onNavigateToInvoice
                    )
                }
            }
        } else {
            // Horizontal Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Header()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    CalendarCard(
                        modifier = Modifier.weight(2f),
                        currentDate = currentDate,
                        selectedDate = selectedDate,
                        today = today,
                        invoicesByDate = invoicesByDate,
                        onDateChange = { newDate -> calendarScreenViewModel.updateCurrentDate(newDate) },
                        onDayClick = { day ->
                            calendarScreenViewModel.updateSelectedDate(
                                DueDate(
                                    day,
                                    currentDate.get(Calendar.MONTH),
                                    currentDate.get(Calendar.YEAR)
                                )
                            )
                        }
                    )

                    // Invoice details beside calendar
                    InvoiceDetailsCard(
                        modifier = Modifier.weight(1f),
                        selectedDate = selectedDate,
                        invoices = selectedInvoices,
                        onInvoiceClick = onNavigateToInvoice
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailsCard(
    modifier: Modifier = Modifier,
    selectedDate: DueDate?,
    invoices: List<Invoice>,
    onInvoiceClick: (Long) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = if (selectedDate != null) {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, selectedDate.year)
                        set(Calendar.MONTH, selectedDate.month)
                        set(Calendar.DAY_OF_MONTH, selectedDate.date)
                    }
                    val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
                    "$monthName ${selectedDate.date}, ${selectedDate.year}"
                } else "Select a Date",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (invoices.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    invoices.forEach { invoice ->
                        InvoiceItem(
                            invoice = invoice,
                            onClick = { onInvoiceClick(invoice.id) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedDate != null)
                            "No invoices due on this date"
                        else
                            "Click on a date with invoices to view details",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceItem(
    invoice: Invoice,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "Invoice Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "Invoice #${invoice.invoiceNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Amount: $${String.format("%.2f", invoice.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Due: ${dateFormat.format(invoice.dueDate.time)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (invoice.status) {
                    "Paid" -> MaterialTheme.colorScheme.primaryContainer
                    "Unpaid" -> MaterialTheme.colorScheme.secondaryContainer
                    "PastDue" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = when (invoice.status) {
                        "PastDue" -> "Past Due"
                        else -> invoice.status
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = when (invoice.status) {
                        "Paid" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "Unpaid" -> MaterialTheme.colorScheme.onSecondaryContainer
                        "PastDue" -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun Legend(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun Header() {
    Column {
        Text(
            text = stringResource(R.string.calendar),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.calendarSubHeading),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CalendarCard(
    modifier: Modifier = Modifier,
    currentDate: Calendar,
    selectedDate: DueDate?,
    today: Calendar,
    invoicesByDate: Map<Int, List<Invoice>>,
    onDateChange: (Calendar) -> Unit,
    onDayClick: (Int) -> Unit
) {
    val daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (currentDate.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }.get(Calendar.DAY_OF_WEEK) - 1

    val days = mutableListOf<Int?>()
    repeat(firstDayOfWeek) { days.add(null) }
    repeat(daysInMonth) { days.add(it + 1) }

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentDate.time)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            onDateChange((currentDate.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            })
                        },
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("◀")
                    }
                    OutlinedButton(
                        onClick = {
                            onDateChange((currentDate.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            })
                        },
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("▶")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Grid
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                // Day Headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Days with spacing between rows
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            week.forEach { day ->
                                val dayInvoices = day?.let { invoicesByDate[it] } ?: emptyList()
                                CalendarDay(
                                    day = day,
                                    invoices = dayInvoices,
                                    isSelected = selectedDate?.date == day &&
                                            selectedDate?.month == currentDate.get(Calendar.MONTH) &&
                                            selectedDate.year == currentDate.get(Calendar.YEAR),
                                    isToday = day != null &&
                                            day == today.get(Calendar.DAY_OF_MONTH) &&
                                            currentDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                            currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR),
                                    onDayClick = {
                                        if (day != null) {
                                            onDayClick(day)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(7 - week.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Legend(color = Color(0xFF22C55E), label = "Paid")
                Legend(color = Color(0xFFEAB308), label = "Pending")
                Legend(color = Color(0xFFEF4444), label = "Overdue")
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int?,
    invoices: List<Invoice>,
    isSelected: Boolean,
    isToday: Boolean = false,
    onDayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        day == null -> Color(0xFFF5F5F5)
        isToday -> MaterialTheme.colorScheme.primaryContainer
        invoices.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else -> Color.Transparent
    }

    val borderColor = when {
        day == null -> Color.Transparent
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color(0xFFE0E0E0)
    }

    val borderWidth = if (isSelected) 2.dp else 1.dp

    val textColor = when {
        day == null -> Color.Transparent
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(enabled = day != null) { onDayClick() }
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = day.toString(),
                    fontSize = 12.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                    color = textColor
                )

                // Show status dots for invoices
                if (invoices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(0.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusColors = invoices.map { invoice ->
                            when (invoice.status) {
                                "Paid" -> Color(0xFF22C55E)
                                "Unpaid" -> Color(0xFFEAB308)
                                "PastDue" -> Color(0xFFEF4444)
                                else -> Color.Gray
                            }
                        }.distinct().take(3)

                        statusColors.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            if (index < statusColors.size - 1) {
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}