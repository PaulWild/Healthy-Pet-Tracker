package com.example.healthypettracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthypettracker.data.local.entity.MedicineSchedule

data class DayOfWeek(
    val bit: Int,
    val shortName: String,
    val fullName: String
)

val daysOfWeek = listOf(
    DayOfWeek(MedicineSchedule.SUNDAY, "S", "Sunday"),
    DayOfWeek(MedicineSchedule.MONDAY, "M", "Monday"),
    DayOfWeek(MedicineSchedule.TUESDAY, "T", "Tuesday"),
    DayOfWeek(MedicineSchedule.WEDNESDAY, "W", "Wednesday"),
    DayOfWeek(MedicineSchedule.THURSDAY, "T", "Thursday"),
    DayOfWeek(MedicineSchedule.FRIDAY, "F", "Friday"),
    DayOfWeek(MedicineSchedule.SATURDAY, "S", "Saturday")
)

@Composable
fun DayOfWeekSelector(
    selectedDays: Int,
    onDaysChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { day ->
            val isSelected = (selectedDays and day.bit) != 0

            FilterChip(
                selected = isSelected,
                onClick = {
                    val newDays = if (isSelected) {
                        selectedDays and day.bit.inv()
                    } else {
                        selectedDays or day.bit
                    }
                    onDaysChanged(newDays)
                },
                label = { Text(day.shortName) },
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

fun getDayNames(daysOfWeek: Int): String {
    if (daysOfWeek == MedicineSchedule.ALL_DAYS) {
        return "Every day"
    }

    val selectedDays = com.example.healthypettracker.ui.components.daysOfWeek
        .filter { (daysOfWeek and it.bit) != 0 }
        .map { it.fullName }

    return when {
        selectedDays.isEmpty() -> "No days selected"
        selectedDays.size <= 3 -> selectedDays.joinToString(", ")
        else -> "${selectedDays.size} days/week"
    }
}
