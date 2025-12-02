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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.platform.LocalContext
import sfu.cmpt362.android_ezcredit.data.AppDatabase
import sfu.cmpt362.android_ezcredit.data.CompanyContext
import sfu.cmpt362.android_ezcredit.data.repository.UserRepository
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.UserViewModelFactory
import sfu.cmpt362.android_ezcredit.ui.theme.*
import sfu.cmpt362.android_ezcredit.utils.AccessMode
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
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

    val context = LocalContext.current
    val userRepository = remember {
        val database = AppDatabase.getInstance(context)
        UserRepository(database.userDao)
    }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(userRepository)
    )
    var isAdmin by remember { mutableStateOf<Boolean?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Check if current user has access to this screen
    LaunchedEffect(Unit) {
        val currentUserId = CompanyContext.currentUserId
        if (currentUserId != null) {
            try {
                val user = userViewModel.getUserById(currentUserId)
                isAdmin = user.accessLevel == AccessMode.Admin
            } catch (e: Exception) {
                isAdmin = false
            }
        } else {
            isAdmin = false
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (isAdmin == false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Access Denied",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "This screen is only accessible to administrators.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        return
    }
    val today = calendarScreenViewModel.today
    val currentDate = calendarScreenViewModel.currentDate
    val selectedDate = calendarScreenViewModel.selectedDate
    val allInvoices by invoiceViewModel.invoicesLiveData.observeAsState(emptyList())

    // Filter state
    var selectedFilters by remember { mutableStateOf(setOf<InvoiceStatus>()) }

    // Filter invoices based on selected statuses
    val filteredInvoices = remember(allInvoices, selectedFilters) {
        if (selectedFilters.isEmpty()) {
            allInvoices
        } else {
            allInvoices.filter { it.status in selectedFilters }
        }
    }

    val invoicesByDate = remember(filteredInvoices, currentDate) {
        filteredInvoices.filter { invoice ->
            val dueDate = invoice.dueDate
            dueDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH) &&
                    dueDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
        }.groupBy { invoice ->
            invoice.dueDate.get(Calendar.DAY_OF_MONTH)
        }
    }

    val selectedInvoices = remember(selectedDate, filteredInvoices) {
        if (selectedDate != null) {
            filteredInvoices.filter { invoice ->
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
            CalendarScreenVerticalLayout(
                currentDate = currentDate,
                selectedDate = selectedDate,
                today = today,
                invoicesByDate = invoicesByDate,
                selectedInvoices = selectedInvoices,
                selectedFilters = selectedFilters,
                onFilterChange = { status ->
                    selectedFilters = if (status in selectedFilters) {
                        selectedFilters - status
                    } else {
                        selectedFilters + status
                    }
                },
                onDateChange = calendarScreenViewModel::updateCurrentDate,
                onDayClick = { day ->
                    calendarScreenViewModel.updateSelectedDate(
                        DueDate(day, currentDate.get(Calendar.MONTH), currentDate.get(Calendar.YEAR))
                    )
                },
                onNavigateToInvoice = onNavigateToInvoice
            )
        } else {
            CalendarScreenHorizontalLayout(
                currentDate = currentDate,
                selectedDate = selectedDate,
                today = today,
                invoicesByDate = invoicesByDate,
                selectedInvoices = selectedInvoices,
                selectedFilters = selectedFilters,
                onFilterChange = { status ->
                    selectedFilters = if (status in selectedFilters) {
                        selectedFilters - status
                    } else {
                        selectedFilters + status
                    }
                },
                onDateChange = calendarScreenViewModel::updateCurrentDate,
                onDayClick = { day ->
                    calendarScreenViewModel.updateSelectedDate(
                        DueDate(day, currentDate.get(Calendar.MONTH), currentDate.get(Calendar.YEAR))
                    )
                },
                onNavigateToInvoice = onNavigateToInvoice
            )
        }
    }
}

@Composable
private fun CalendarScreenVerticalLayout(
    currentDate: Calendar,
    selectedDate: DueDate?,
    today: Calendar,
    invoicesByDate: Map<Int, List<Invoice>>,
    selectedInvoices: List<Invoice>,
    selectedFilters: Set<InvoiceStatus>,
    onFilterChange: (InvoiceStatus) -> Unit,
    onDateChange: (Calendar) -> Unit,
    onDayClick: (Int) -> Unit,
    onNavigateToInvoice: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header()
        StatusFilterRow(
            selectedFilters = selectedFilters,
            onFilterChange = onFilterChange
        )
        CalendarCard(
            currentDate = currentDate,
            selectedDate = selectedDate,
            today = today,
            invoicesByDate = invoicesByDate,
            onDateChange = onDateChange,
            onDayClick = onDayClick
        )
        if (selectedDate != null) {
            InvoiceDetailsCard(
                selectedDate = selectedDate,
                invoices = selectedInvoices,
                onInvoiceClick = onNavigateToInvoice
            )
        }
    }
}

@Composable
private fun CalendarScreenHorizontalLayout(
    currentDate: Calendar,
    selectedDate: DueDate?,
    today: Calendar,
    invoicesByDate: Map<Int, List<Invoice>>,
    selectedInvoices: List<Invoice>,
    selectedFilters: Set<InvoiceStatus>,
    onFilterChange: (InvoiceStatus) -> Unit,
    onDateChange: (Calendar) -> Unit,
    onDayClick: (Int) -> Unit,
    onNavigateToInvoice: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Header()
        StatusFilterRow(
            selectedFilters = selectedFilters,
            onFilterChange = onFilterChange
        )
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            CalendarCard(
                modifier = Modifier.weight(2f),
                currentDate = currentDate,
                selectedDate = selectedDate,
                today = today,
                invoicesByDate = invoicesByDate,
                onDateChange = onDateChange,
                onDayClick = onDayClick
            )
            InvoiceDetailsCard(
                modifier = Modifier.weight(1f),
                selectedDate = selectedDate,
                invoices = selectedInvoices,
                onInvoiceClick = onNavigateToInvoice
            )
        }
    }
}

@Composable
fun StatusFilterRow(
    selectedFilters: Set<InvoiceStatus>,
    onFilterChange: (InvoiceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Filter by Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    status = InvoiceStatus.Paid,
                    isSelected = InvoiceStatus.Paid in selectedFilters,
                    onClick = { onFilterChange(InvoiceStatus.Paid) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    status = InvoiceStatus.Unpaid,
                    isSelected = InvoiceStatus.Unpaid in selectedFilters,
                    onClick = { onFilterChange(InvoiceStatus.Unpaid) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    status = InvoiceStatus.PastDue,
                    isSelected = InvoiceStatus.PastDue in selectedFilters,
                    onClick = { onFilterChange(InvoiceStatus.PastDue) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    status = InvoiceStatus.LatePayment,
                    isSelected = InvoiceStatus.LatePayment in selectedFilters,
                    onClick = { onFilterChange(InvoiceStatus.LatePayment) },
                    displayText = "Late",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    status: InvoiceStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    displayText: String? = null,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        when (status) {
            InvoiceStatus.Paid -> Green
            InvoiceStatus.Unpaid -> Amber
            InvoiceStatus.PastDue -> Red
            InvoiceStatus.LatePayment -> Red
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText ?: when (status) {
                    InvoiceStatus.PastDue -> "Past Due"
                    else -> status.name
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )
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
        modifier = modifier.fillMaxWidth().heightIn(min = 200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    invoices.forEach { invoice ->
                        InvoiceItem(
                            invoice = invoice,
                            isHorizontal = false,
                            onClick = { onInvoiceClick(invoice.id) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
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
    isHorizontal: Boolean = false,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val iconSize = if (isHorizontal) 24.dp else 32.dp
    val paddingBetween = if (isHorizontal) 2.dp else 12.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (isHorizontal) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(paddingBetween)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Invoice Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(iconSize)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Invoice #${invoice.invoiceNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Amount: ${String.format("%.2f", invoice.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Due: ${dateFormat.format(invoice.dueDate.time)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                InvoiceStatusBadge(
                    status = invoice.status,
                    modifier = Modifier.padding(start = iconSize + paddingBetween)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = "Invoice Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(iconSize)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = paddingBetween)
                ) {
                    Text(
                        text = "Invoice #${invoice.invoiceNumber}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Amount: ${String.format("%.2f", invoice.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Due: ${dateFormat.format(invoice.dueDate.time)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                InvoiceStatusBadge(status = invoice.status)
            }
        }
    }
}

@Composable
private fun InvoiceStatusBadge(status: InvoiceStatus, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = when (status) {
            InvoiceStatus.Paid -> MaterialTheme.colorScheme.primaryContainer
            InvoiceStatus.Unpaid -> MaterialTheme.colorScheme.secondaryContainer
            InvoiceStatus.PastDue -> MaterialTheme.colorScheme.errorContainer
            InvoiceStatus.LatePayment-> MaterialTheme.colorScheme.errorContainer
        },
        modifier = modifier
    ) {
        Text(
            text = when (status) {
                InvoiceStatus.PastDue -> "Past Due"
                else -> status.name
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = when (status) {
                InvoiceStatus.Paid -> MaterialTheme.colorScheme.onPrimaryContainer
                InvoiceStatus.Unpaid-> MaterialTheme.colorScheme.onSecondaryContainer
                InvoiceStatus.PastDue -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
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

    val days = remember(currentDate) {
        mutableListOf<Int?>().apply {
            repeat(firstDayOfWeek) { add(null) }
            repeat(daysInMonth) { add(it + 1) }
        }
    }

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentDate.time)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

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

            CalendarGrid(
                days = days,
                currentDate = currentDate,
                selectedDate = selectedDate,
                today = today,
                invoicesByDate = invoicesByDate,
                onDayClick = onDayClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Legend(color = Green, label = InvoiceStatus.Paid.name)
                Legend(color = Amber, label = InvoiceStatus.Unpaid.name)
                Legend(color = Red, label = InvoiceStatus.PastDue.name)
                Legend(color = Red, label = "Late")
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<Int?>,
    currentDate: Calendar,
    selectedDate: DueDate?,
    today: Calendar,
    invoicesByDate: Map<Int, List<Invoice>>,
    onDayClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
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
        day == null -> WhiteSmoke
        isToday -> MaterialTheme.colorScheme.primaryContainer
        invoices.isNotEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        else -> Color.Transparent
    }

    val borderColor = when {
        day == null -> Color.Transparent
        isSelected -> MaterialTheme.colorScheme.primary
        else -> LightGray
    }

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
            .border(
                if (isSelected) 2.dp else 1.dp,
                borderColor,
                RoundedCornerShape(8.dp)
            ),
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

                if (invoices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(0.dp))

                    val mostSignificantColor = when {
                        invoices.any { it.status == InvoiceStatus.PastDue || it.status == InvoiceStatus.LatePayment } -> Red
                        invoices.any { it.status == InvoiceStatus.Unpaid } -> Amber
                        else -> Green
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(mostSignificantColor)
                    )
                }
            }
        }
    }
}