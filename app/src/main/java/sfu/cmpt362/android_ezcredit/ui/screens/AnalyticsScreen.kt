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

data class CustomerCreditScore(
    val name: String,
    val creditScore: Int,
    val maxScore: Int
)

data class AnalyticsData(
    val debtRecovered: Double,
    val debtOutstanding: Double,
    val invoicesSent: Int,
    val monthlyRevenue: List<MonthlyRevenue>,
    val monthlyBreakdown: MonthlyBreakdown,
    val topCustomersByRevenue: List<CustomerRevenue>,
    val topCustomersByCreditScore: List<CustomerCreditScore>
)

data class MonthlyBreakdown(
    val revenue: Double,
    val invoices: Int,
    val outstanding: Double
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

    val analyticsData = remember(invoices, customers) {
        calculateAnalytics(invoices, customers)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isVertical = maxWidth < 800.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
        ) {
            AnalyticsHeader()

            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodChange = { selectedPeriod = it }
            )

            if (isVertical) {
                // Vertical layout: Column for metric cards
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
            } else {
                // Horizontal layout: Row for metric cards
                MetricCardsRow(analyticsData = analyticsData)
            }

            RevenueTrendCard(monthlyRevenue = analyticsData.monthlyRevenue)

            if (isVertical) {
                // Vertical layout: stack cards vertically
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MonthlyBreakdownCard(
                        breakdown = analyticsData.monthlyBreakdown
                    )

                    TopCustomersByRevenueCard(
                        topCustomers = analyticsData.topCustomersByRevenue
                    )
                }
            } else {
                // Horizontal layout: side by side
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
        }
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
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Business performance overview",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
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
        PeriodButton(
            text = "Week",
            isSelected = selectedPeriod == "Week",
            onClick = { onPeriodChange("Week") }
        )
        Spacer(modifier = Modifier.width(10.dp))
        PeriodButton(
            text = "Month",
            isSelected = selectedPeriod == "Month",
            onClick = { onPeriodChange("Month") }
        )
        Spacer(modifier = Modifier.width(10.dp))
        PeriodButton(
            text = "Quarter",
            isSelected = selectedPeriod == "Quarter",
            onClick = { onPeriodChange("Quarter") }
        )
    }
}

@Composable
fun PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF4F46E5) else Color.White)
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
fun MetricCardsRow(analyticsData: AnalyticsData) {
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
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F46E5)
            )
        }
    }
}

@Composable
fun RevenueTrendCard(monthlyRevenue: List<MonthlyRevenue>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
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
                    color = Color.Gray
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
fun RevenueBar(
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

        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5E7EB))
            )

            val progress = if (maxAmount > 0) (amount / maxAmount).toFloat() else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4F46E5))
            )
        }

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
                text = "Monthly Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            BreakdownItem(
                label = "Revenue",
                value = formatCurrency(breakdown.revenue),
                progress = 1f,
                color = Color(0xFF4F46E5)
            )

            Spacer(modifier = Modifier.height(20.dp))

            BreakdownItem(
                label = "Outstanding",
                value = formatCurrency(breakdown.outstanding),
                progress = if (breakdown.revenue > 0)
                    (breakdown.outstanding.toFloat() / breakdown.revenue.toFloat()).coerceIn(0f, 1f)
                else 0f,
                color = Color(0xFFFBBF24)
            )

            Spacer(modifier = Modifier.height(20.dp))

            BreakdownItem(
                label = "Invoices",
                value = "${breakdown.invoices}",
                progress = if (breakdown.revenue > 0)
                    (breakdown.invoices.toFloat() * 100f / breakdown.revenue.toFloat()).coerceIn(0f, 1f)
                else 0f,
                color = Color(0xFF10B981)
            )
        }
    }
}

@Composable
fun BreakdownItem(
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

        Box(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE5E7EB))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
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
                    color = Color.Gray
                )
            } else {
                topCustomers.forEachIndexed { index, customer ->
                    val color = when(index) {
                        0 -> Color(0xFF4F46E5)
                        1 -> Color(0xFF10B981)
                        else -> Color(0xFF8B5CF6)
                    }
                    CustomerRevenueItem(
                        name = customer.name,
                        amount = customer.amount,
                        progress = customer.amount / customer.maxAmount,
                        color = color
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
fun CustomerRevenueItem(
    name: String,
    amount: Double,
    progress: Double,
    color: Color
) {
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

        Box(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE5E7EB))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.toFloat().coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

fun calculateAnalytics(invoices: List<Invoice>, customers: List<Customer>): AnalyticsData {
    val debtRecovered = invoices.filter { it.status.equals("Paid", ignoreCase = true) }.sumOf { it.amount }
    val debtOutstanding = invoices.filter { !it.status.equals("Paid", ignoreCase = true) }.sumOf { it.amount }
    val invoicesSent = invoices.size

    val monthlyRevenueMap = mutableMapOf<String, Double>()
    invoices.forEach { invoice ->
        val monthKey = "${invoice.invDate.get(java.util.Calendar.YEAR)}-${invoice.invDate.get(java.util.Calendar.MONTH)}"
        monthlyRevenueMap[monthKey] = monthlyRevenueMap.getOrDefault(monthKey, 0.0) + invoice.amount
    }

    val maxMonthlyRevenue = monthlyRevenueMap.values.maxOrNull() ?: 1.0
    val sortedMonths = monthlyRevenueMap.entries.sortedBy { entry ->
        val parts = entry.key.split("-")
        parts[0].toInt() * 12 + parts[1].toInt()
    }.takeLast(6)

    val monthlyRevenue = sortedMonths.map { (key, amount) ->
        val parts = key.split("-")
        val month = java.text.DateFormatSymbols().shortMonths[parts[1].toInt()]
        MonthlyRevenue(
            month = month,
            amount = amount,
            maxAmount = maxMonthlyRevenue
        )
    }

    val currentCalendar = java.util.Calendar.getInstance()
    val currentYear = currentCalendar.get(java.util.Calendar.YEAR)
    val currentMonth = currentCalendar.get(java.util.Calendar.MONTH)

    val currentMonthInvoices = invoices.filter { invoice ->
        invoice.invDate.get(java.util.Calendar.YEAR) == currentYear &&
                invoice.invDate.get(java.util.Calendar.MONTH) == currentMonth
    }

    val monthlyBreakdown = MonthlyBreakdown(
        revenue = currentMonthInvoices.sumOf { it.amount },
        invoices = currentMonthInvoices.size,
        outstanding = currentMonthInvoices.filter { !it.status.equals("Paid", ignoreCase = true) }.sumOf { it.amount }
    )

    val customerMap = customers.associateBy { it.id }

    val customerRevenueMap = mutableMapOf<String, Double>()
    invoices.forEach { invoice ->
        val customerName = customerMap[invoice.customerID]?.name ?: "Unknown"
        customerRevenueMap[customerName] = customerRevenueMap.getOrDefault(customerName, 0.0) + invoice.amount
    }

    val maxCustomerRevenue = customerRevenueMap.values.maxOrNull() ?: 1.0
    val topCustomersByRevenue = customerRevenueMap.entries
        .sortedByDescending { it.value }
        .take(3)
        .map { (name, amount) ->
            CustomerRevenue(
                name = name,
                amount = amount,
                maxAmount = maxCustomerRevenue
            )
        }

    val maxCreditScore = customers.maxOfOrNull { it.creditScore } ?: 850
    val topCustomersByCreditScore = customers
        .sortedByDescending { it.creditScore }
        .take(3)
        .map { customer ->
            CustomerCreditScore(
                name = customer.name,
                creditScore = customer.creditScore,
                maxScore = maxCreditScore
            )
        }

    return AnalyticsData(
        debtRecovered = debtRecovered,
        debtOutstanding = debtOutstanding,
        invoicesSent = invoicesSent,
        monthlyRevenue = monthlyRevenue,
        monthlyBreakdown = monthlyBreakdown,
        topCustomersByRevenue = topCustomersByRevenue,
        topCustomersByCreditScore = topCustomersByCreditScore
    )
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.maximumFractionDigits = 0
    return format.format(amount)
}