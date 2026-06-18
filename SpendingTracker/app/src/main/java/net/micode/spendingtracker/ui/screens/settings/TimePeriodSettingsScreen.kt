package net.micode.spendingtracker.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.micode.spendingtracker.R
import net.micode.spendingtracker.ui.theme.BeigeHeader
import net.micode.spendingtracker.ui.theme.DarkBrownText
import net.micode.spendingtracker.util.SettingsManager
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePeriodSettingsScreen(
    settingsManager: SettingsManager,
    accountName: String,
    onBack: () -> Unit
) {
    var defaultPeriod by remember { mutableStateOf(settingsManager.getDefaultTimePeriod()) }
    var monthStartDay by remember { mutableIntStateOf(settingsManager.getMonthStartDay()) }
    var weekStartDay by remember { mutableIntStateOf(settingsManager.getWeekStartDay()) }

    var showPeriodDialog by remember { mutableStateOf(false) }
    var showMonthDayDialog by remember { mutableStateOf(false) }
    var showWeekDayDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.time_period), color = DarkBrownText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = DarkBrownText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BeigeHeader)
            )
        },
        containerColor = BeigeHeader
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Account name as shown in the image
            Text(
                text = accountName,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )

            // Header with Help Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.default_time_period),
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = { showInfoDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.HelpOutline, contentDescription = "Info", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), 0.5.dp, Color.LightGray)

            SettingsClickableRow(
                title = stringResource(R.string.show_spending),
                value = when(defaultPeriod) {
                    "DAY" -> stringResource(R.string.daily)
                    "WEEK" -> stringResource(R.string.weekly)
                    "YEAR" -> stringResource(R.string.yearly)
                    else -> stringResource(R.string.monthly)
                },
                onClick = { showPeriodDialog = true }
            )

            SettingsClickableRow(
                title = stringResource(R.string.month_start_day),
                value = monthStartDay.toString(),
                onClick = { showMonthDayDialog = true }
            )

            SettingsClickableRow(
                title = stringResource(R.string.week_start_day),
                value = getWeekDayName(weekStartDay),
                onClick = { showWeekDayDialog = true }
            )

            Text(
                text = stringResource(R.string.time_period_desc),
                modifier = Modifier.padding(16.dp),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }

    if (showInfoDialog) {
        TimePeriodInfoDialog(onDismiss = { showInfoDialog = false })
    }

    if (showPeriodDialog) {
        val periods = listOf("DAY", "WEEK", "MONTH", "YEAR")
        SingleSelectDialog(
            title = stringResource(R.string.time_period),
            options = periods,
            optionLabels = listOf(
                stringResource(R.string.daily),
                stringResource(R.string.weekly),
                stringResource(R.string.monthly),
                stringResource(R.string.yearly)
            ),
            selectedOption = defaultPeriod,
            onDismiss = { showPeriodDialog = false },
            onSelect = {
                defaultPeriod = it
                settingsManager.setDefaultTimePeriod(it)
                showPeriodDialog = false
            }
        )
    }

    if (showMonthDayDialog) {
        val days = (1..31).toList()
        SingleSelectDialog(
            title = stringResource(R.string.month_start_day),
            options = days,
            optionLabels = days.map { it.toString() },
            selectedOption = monthStartDay,
            onDismiss = { showMonthDayDialog = false },
            onSelect = {
                monthStartDay = it
                settingsManager.setMonthStartDay(it)
                showMonthDayDialog = false
            }
        )
    }

    if (showWeekDayDialog) {
        val weekDays = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        )
        SingleSelectDialog(
            title = stringResource(R.string.week_start_day),
            options = weekDays,
            optionLabels = weekDays.map { getWeekDayName(it) },
            selectedOption = weekStartDay,
            onDismiss = { showWeekDayDialog = false },
            onSelect = {
                weekStartDay = it
                settingsManager.setWeekStartDay(it)
                showWeekDayDialog = false
            }
        )
    }
}

@Composable
fun TimePeriodInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.info), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.time_period_info_main))
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.show_spending), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.show_spending_info_desc))
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.month_start_day), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.month_start_day_info_desc))
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.week_start_day), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.week_start_day_info_desc))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = DarkBrownText, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    )
}

@Composable
fun <T> SingleSelectDialog(
    title: String,
    options: List<T>,
    optionLabels: List<String>,
    selectedOption: T,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = { onSelect(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = DarkBrownText)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(optionLabels[index], fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = DarkBrownText, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    )
}

@Composable
fun getWeekDayName(day: Int): String {
    return when(day) {
        Calendar.MONDAY -> stringResource(R.string.monday)
        Calendar.TUESDAY -> stringResource(R.string.tuesday)
        Calendar.WEDNESDAY -> stringResource(R.string.wednesday)
        Calendar.THURSDAY -> stringResource(R.string.thursday)
        Calendar.FRIDAY -> stringResource(R.string.friday)
        Calendar.SATURDAY -> stringResource(R.string.saturday)
        Calendar.SUNDAY -> stringResource(R.string.sunday)
        else -> ""
    }
}
