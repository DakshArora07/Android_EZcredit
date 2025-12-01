package sfu.cmpt362.android_ezcredit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sfu.cmpt362.android_ezcredit.data.entity.Customer
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.data.viewmodel.CustomerViewModel
import sfu.cmpt362.android_ezcredit.data.viewmodel.InvoiceViewModel
import sfu.cmpt362.android_ezcredit.ui.theme.*
import sfu.cmpt362.android_ezcredit.utils.InvoiceStatus
import java.text.NumberFormat
import java.util.*

data class MonthlyRevenue(
    val month: String,
    val amount: Double,
    val maxAmount: Double
)

data class CustomerRevenue(
    val name: String,
    val amount: Double,
    val maxAmount: Double
)

data class AnalyticsData(
    val debtRecovered: Double,
    val debtOutstanding: Double,
    val invoicesSent: Int,
    val monthlyRevenue: List<MonthlyRevenue>,
    val monthlyBreakdown: MonthlyBreakdown,
    val topCustomersByRevenue: List<CustomerRevenue>,
)

data class MonthlyBreakdown(
    val revenue: Double,
    val invoices: Int,
    val outstanding: Double,
    val periodLabel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    invoiceViewModel: InvoiceViewModel,
    customerViewModel: CustomerViewModel,
    modifier: Modifier = Modifier
) {
    val invoices by invoiceViewModel.invoicesLiveData.observeAsState(emptyList())
    val customers by customerViewModel.customersLiveData.observeAsState(emptyList())
    var selectedPeriod by remember { mutableStateOf("Month") }

    val analyticsData = remember(invoices, customers, selectedPeriod) {
        calculateAnalytics(invoices, customers, selectedPeriod)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isVertical = maxWidth < 800.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteSmoke)
                .verticalScroll(rememberScrollState())
        ) {
            AnalyticsHeader()

            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodChange = { selectedPeriod = it }
            )

            if (isVertical) {
                AnalyticsContentVertical(analyticsData)
            } else {
                AnalyticsContentHorizontal(analyticsData)
            }
        }
    }
}

@Composable
private fun AnalyticsContentVertical(analyticsData: AnalyticsData) {
    Spacer(modifier = Modifier.height(12.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            title = "Debt Recovered",
            value = formatCurrency(analyticsData.debtRecovered),
            modifier = Modifier.fillMaxWidth()
        )
        MetricCard(
            title = "Debt Outstanding",
            value = formatCurrency(analyticsData.debtOutstanding),
            modifier = Modifier.fillMaxWidth()
        )
        MetricCard(
            title = "Invoices Sent",
            value = analyticsData.invoicesSent.toString(),
            modifier = Modifier.fillMaxWidth()
        )
    }

    RevenueTrendCard(
        monthlyRevenue = analyticsData.monthlyRevenue,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MonthlyBreakdownCard(breakdown = analyticsData.monthlyBreakdown)
        TopCustomersByRevenueCard(topCustomers = analyticsData.topCustomersByRevenue)
    }
}

@Composable
private fun AnalyticsContentHorizontal(analyticsData: AnalyticsData) {
    Spacer(modifier = Modifier.height(12.dp))

    MetricCardsRow(analyticsData = analyticsData)

    RevenueTrendCard(
        monthlyRevenue = analyticsData.monthlyRevenue,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MonthlyBreakdownCard(
            breakdown = analyticsData.monthlyBreakdown,
            modifier = Modifier.weight(1f)
        )

        TopCustomersByRevenueCard(
            topCustomers = analyticsData.topCustomersByRevenue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AnalyticsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Business performance overview",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 18.dp)
            .padding(bottom = 24.dp)
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        listOf("Week", "Month", "Quarter").forEachIndexed { index, period ->
            if (index > 0) Spacer(modifier = Modifier.width(10.dp))
            PeriodButton(
                text = period,
                isSelected = selectedPeriod == period,
                onClick = { onPeriodChange(period) }
            )
        }
    }
}

@Composable
private fun PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 11.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun MetricCardsRow(analyticsData: AnalyticsData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        MetricCard(
            title = "Debt Recovered",
            value = formatCurrency(analyticsData.debtRecovered),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "Debt Outstanding",
            value = formatCurrency(analyticsData.debtOutstanding),
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            title = "Invoices Sent",
            value = analyticsData.invoicesSent.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Grey
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun RevenueTrendCard(
    monthlyRevenue: List<MonthlyRevenue>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Revenue Trend",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (monthlyRevenue.isEmpty()) {
                Text(
                    text = "No revenue data available",
                    fontSize = 16.sp,
                    color = Grey
                )
            } else {
                monthlyRevenue.forEach { monthData ->
                    RevenueBar(
                        month = monthData.month,
                        amount = monthData.amount,
                        maxAmount = monthData.maxAmount
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RevenueBar(
    month: String,
    amount: Double,
    maxAmount: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = month,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.width(50.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        ProgressBar(
            progress = if (maxAmount > 0) (amount / maxAmount).toFloat() else 0f,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = formatCurrency(amount),
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(100.dp)
        )
    }
}

@Composable
fun MonthlyBreakdownCard(
    breakdown: MonthlyBreakdown,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "${breakdown.periodLabel} Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            BreakdownItem(
                label = "Revenue",
                value = formatCurrency(breakdown.revenue),
                progress = 1f,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            BreakdownItem(
                label = "Outstanding",
                value = formatCurrency(breakdown.outstanding),
                progress = if (breakdown.revenue > 0)
                    (breakdown.outstanding.toFloat() / breakdown.revenue.toFloat()).coerceIn(0f, 1f)
                else 0f,
                color = Yellow
            )

            Spacer(modifier = Modifier.height(20.dp))

            BreakdownItem(
                label = "Invoices",
                value = "${breakdown.invoices}",
                progress = if (breakdown.revenue > 0)
                    (breakdown.invoices.toFloat() * 100f / breakdown.revenue.toFloat()).coerceIn(0f, 1f)
                else 0f,
                color = Green
            )
        }
    }
}

@Composable
private fun BreakdownItem(
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProgressBar(
            progress = progress,
            color = color,
            height = 8.dp
        )
    }
}

@Composable
fun TopCustomersByRevenueCard(
    topCustomers: List<CustomerRevenue>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Top Customers by Revenue",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (topCustomers.isEmpty()) {
                Text(
                    text = "No customer data available",
                    fontSize = 14.sp,
                    color = Grey
                )
            } else {
                topCustomers.forEachIndexed { index, customer ->
                    CustomerRevenueItem(
                        name = customer.name,
                        amount = customer.amount,
                        progress = customer.amount / customer.maxAmount,
                        colorIndex = index
                    )
                    if (index < topCustomers.size - 1) {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerRevenueItem(
    name: String,
    amount: Double,
    progress: Double,
    colorIndex: Int
) {
    val color = when(colorIndex) {
        0 -> MaterialTheme.colorScheme.primary
        1 -> Green
        else -> Purple
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
            Text(
                text = formatCurrency(amount),
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ProgressBar(
            progress = progress.toFloat(),
            color = color,
            height = 8.dp
        )
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 16.dp
) {
    Box(modifier = modifier.fillMaxWidth().height(height)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(VeryLightGray)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}


fun calculateAnalytics(invoices: List<Invoice>, customers: List<Customer>, period: String): AnalyticsData {
    val debtRecovered = invoices
        .filter { it.status == InvoiceStatus.Paid || it.status == InvoiceStatus.LatePayment }
        .sumOf { it.amount }

    val debtOutstanding = invoices
        .filter { it.status == InvoiceStatus.Unpaid || it.status == InvoiceStatus.PastDue }
        .sumOf { it.amount }

    val invoicesSent = invoices.size

    val monthlyRevenue = calculatePeriodRevenue(invoices, period)
    val monthlyBreakdown = calculatePeriodBreakdown(invoices, period)
    val topCustomersByRevenue = calculateTopCustomersByRevenue(invoices, customers)

    return AnalyticsData(
        debtRecovered = debtRecovered,
        debtOutstanding = debtOutstanding,
        invoicesSent = invoicesSent,
        monthlyRevenue = monthlyRevenue,
        monthlyBreakdown = monthlyBreakdown,
        topCustomersByRevenue = topCustomersByRevenue,
    )
}

private fun calculatePeriodRevenue(invoices: List<Invoice>, period: String): List<MonthlyRevenue> {
    val revenueMap = mutableMapOf<String, Double>()

    invoices.forEach { invoice ->
        val key = when (period) {
            "Week" -> getWeekKey(invoice.invDate)
            "Quarter" -> getQuarterKey(invoice.invDate)
            else -> getMonthKey(invoice.invDate)
        }
        revenueMap[key] = revenueMap.getOrDefault(key, 0.0) + invoice.amount
    }

    val maxRevenue = revenueMap.values.maxOrNull() ?: 1.0

    val sortedEntries = when (period) {
        "Week" -> revenueMap.entries.sortedBy { parseWeekKey(it.key) }.takeLast(8)
        "Quarter" -> revenueMap.entries.sortedBy { parseQuarterKey(it.key) }.takeLast(4)
        else -> revenueMap.entries.sortedBy { parseMonthKey(it.key) }.takeLast(6)
    }

    return sortedEntries.map { (key, amount) ->
        val label = when (period) {
            "Week" -> formatWeekLabel(key)
            "Quarter" -> formatQuarterLabel(key)
            else -> formatMonthLabel(key)
        }
        MonthlyRevenue(
            month = label,
            amount = amount,
            maxAmount = maxRevenue
        )
    }
}

private fun calculatePeriodBreakdown(invoices: List<Invoice>, period: String): MonthlyBreakdown {
    val currentCalendar = Calendar.getInstance()

    val periodInvoices = invoices.filter { invoice ->
        when (period) {
            "Week" -> isSameWeek(invoice.invDate, currentCalendar)
            "Quarter" -> isSameQuarter(invoice.invDate, currentCalendar)
            else -> isSameMonth(invoice.invDate, currentCalendar)
        }
    }

    val periodLabel = when (period) {
        "Week" -> "Weekly"
        "Quarter" -> "Quarterly"
        else -> "Monthly"
    }

    return MonthlyBreakdown(
        revenue = periodInvoices.sumOf { it.amount },
        invoices = periodInvoices.size,
        outstanding = periodInvoices
            .filter { it.status == InvoiceStatus.Unpaid || it.status == InvoiceStatus.PastDue }
            .sumOf { it.amount },
        periodLabel = periodLabel
    )
}

private fun calculateTopCustomersByRevenue(
    invoices: List<Invoice>,
    customers: List<Customer>
): List<CustomerRevenue> {
    val customerMap = customers.associateBy { it.id }
    val customerRevenueMap = mutableMapOf<String, Double>()

    invoices.forEach { invoice ->
        val customerName = customerMap[invoice.customerId]?.name ?: "Unknown"
        customerRevenueMap[customerName] = customerRevenueMap.getOrDefault(customerName, 0.0) + invoice.amount
    }

    val maxCustomerRevenue = customerRevenueMap.values.maxOrNull() ?: 1.0

    return customerRevenueMap.entries
        .sortedByDescending { it.value }
        .take(3)
        .map { (name, amount) ->
            CustomerRevenue(
                name = name,
                amount = amount,
                maxAmount = maxCustomerRevenue
            )
        }
}

private fun getMonthKey(calendar: android.icu.util.Calendar): String {
    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
}

private fun getWeekKey(calendar: android.icu.util.Calendar): String {
    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.WEEK_OF_YEAR)}"
}

private fun getQuarterKey(calendar: android.icu.util.Calendar): String {
    val quarter = calendar.get(Calendar.MONTH) / 3 + 1
    return "${calendar.get(Calendar.YEAR)}-Q$quarter"
}

private fun parseMonthKey(key: String): Int {
    val parts = key.split("-")
    return parts[0].toInt() * 12 + parts[1].toInt()
}

private fun parseWeekKey(key: String): Int {
    val parts = key.split("-")
    return parts[0].toInt() * 52 + parts[1].toInt()
}

private fun parseQuarterKey(key: String): Int {
    val parts = key.split("-Q")
    return parts[0].toInt() * 4 + parts[1].toInt()
}

private fun formatMonthLabel(key: String): String {
    val parts = key.split("-")
    return java.text.DateFormatSymbols().shortMonths[parts[1].toInt()]
}

private fun formatWeekLabel(key: String): String {
    val parts = key.split("-")
    return "W${parts[1]}"
}

private fun formatQuarterLabel(key: String): String {
    return key.split("-")[1]
}

private fun isSameMonth(calendar1: android.icu.util.Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
}

private fun isSameWeek(calendar1: android.icu.util.Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR)
}

private fun isSameQuarter(calendar1: android.icu.util.Calendar, calendar2: Calendar): Boolean {
    val quarter1 = calendar1.get(Calendar.MONTH) / 3
    val quarter2 = calendar2.get(Calendar.MONTH) / 3
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && quarter1 == quarter2
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.maximumFractionDigits = 0
    return format.format(amount)
}