package sfu.cmpt362.android_ezcredit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import sfu.cmpt362.android_ezcredit.R

@Preview
@Composable
fun AnalyticsScreen() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.analytics),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(R.string.analyticsScreenSubHeading),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            LineGraph(
                dataPoints = listOf(120f, 150.49f),
                labels = listOf("ABC Company", "XYZ Enterprise")
            )
        }
    }
}

@Composable
fun LineGraph(
    dataPoints: List<Float>,
    labels: List<String>,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = dataPoints.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val yLabelWidthDp = 40.dp
    val labelHeightDp = 30.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier.width(yLabelWidthDp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..4) {
                    val value = ((maxValue / 4) * (4 - i)).toInt()
                    Text(
                        text = value.toString(),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val paddingLeft = 80f
                val paddingRight = 200f
                val width = size.width
                val height = size.height
                val graphWidth = width - paddingLeft - paddingRight
                val graphHeight = height

                val stepX = if (dataPoints.size > 1) graphWidth / (dataPoints.size - 1) else graphWidth

                val points = dataPoints.mapIndexed { index, value ->
                    Offset(
                        x = paddingLeft + stepX * index,
                        y = graphHeight - (value / maxValue) * graphHeight
                    )
                }

                drawLine(
                    color = Color.LightGray,
                    start = Offset(paddingLeft, graphHeight),
                    end = Offset(width - paddingRight, graphHeight),
                    strokeWidth = 2f
                )

                points.windowed(2).forEach { (start, end) ->
                    drawLine(
                        start = start,
                        end = end,
                        color = lineColor,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }

                points.forEach {
                    drawCircle(color = lineColor, radius = 8f, center = it)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(labelHeightDp),
            horizontalArrangement = Arrangement.Start
        ) {
            labels.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}