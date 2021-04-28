package com.udacity.project4.locationreminders.utils

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

class DataProvider {

    companion object {

        public fun getSampleReminderDTO() : ReminderDTO {
            return ReminderDTO("title",
                    "description",
                    "location",
                    0.0,
                    0.0)
        }

        public fun getSampleReminderData() : ReminderDataItem {
            return ReminderDataItem("title",
                    "description",
                    "location",
                    0.0,
                    0.0)
        }
    }
}