package sfu.cmpt362.android_ezcredit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import kotlin.collections.forEach
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import sfu.cmpt362.android_ezcredit.R
import sfu.cmpt362.android_ezcredit.ui.viewmodel.CalendarScreenViewModel

data class DueDate(
    val date: Int,
)

@Preview
@Composable
fun CalendarScreen(calendarScreenViewModel: CalendarScreenViewModel = viewModel()) {
    val today = calendarScreenViewModel.today
    val currentDate = calendarScreenViewModel.currentDate
    val selectedDate = calendarScreenViewModel.selectedDate


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
                    onDateChange = { newDate -> calendarScreenViewModel.updateCurrentDate(newDate) },
                    onDayClick = { selectedDate -> calendarScreenViewModel.updateSelectedDate(selectedDate)}
                )

                // Display selected date
                if (selectedDate != null) {
                    SelectedDateDisplay(selectedDate, currentDate)
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
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    CalendarCard(
                        modifier = Modifier.weight(2f),
                        currentDate = currentDate,
                        selectedDate = selectedDate,
                        today = today,
                        onDateChange = { newDate ->  calendarScreenViewModel.updateCurrentDate(newDate)},
                        onDayClick = { selectedDate -> calendarScreenViewModel.updateSelectedDate(selectedDate) }
                    )
                }

                // Display selected date
                if (selectedDate != null) {
                    SelectedDateDisplay(selectedDate, currentDate)
                }
            }
        }
    }
}

@Composable
fun Legend(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
    onDateChange: (Calendar) -> Unit,
    onDayClick: (DueDate) -> Unit
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
            Column {
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

                // Calendar Days
                days.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        week.forEach { day ->
                            CalendarDay(
                                day = day,
                                isSelected = selectedDate?.date == day,
                                isToday = day != null &&
                                        day == today.get(Calendar.DAY_OF_MONTH) &&
                                        currentDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                        currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR),
                                onDayClick = {
                                    if (day != null) {
                                        onDayClick(DueDate(day))
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

            Spacer(modifier = Modifier.height(24.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Legend(color = colorResource(id = R.color.green), label = "Paid")
                Legend(color = colorResource(id = R.color.yellow), label = "Pending")
                Legend(color = colorResource(id = R.color.red), label = "Overdue")
            }
        }
    }
}

@Composable
fun CalendarDay(
    day: Int?,
    isSelected: Boolean,
    isToday: Boolean = false,
    onDayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        day == null -> colorResource(id = R.color.gray)
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val borderColor = when {
        day == null -> Color.Transparent
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val borderWidth = if (isSelected) 3.dp else 2.dp

    val textColor = when {
        day == null -> Color.Transparent
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(2.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = day != null) { onDayClick() },

        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.toString(),
                    fontSize = 14.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun SelectedDateDisplay(selectedDate: DueDate, currentDate: Calendar) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Selected date",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(currentDate.time)
            val year = currentDate.get(Calendar.YEAR)
            Text(
                text = "$monthName ${selectedDate.date}, $year",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}