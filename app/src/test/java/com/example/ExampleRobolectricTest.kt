package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.lessons.LessonsCatalog
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("دليل بقالة العزي", appName)
    }

    @Test
    fun `catalog contains exactly 10 Arabic voiced lessons`() {
        assertEquals(10, LessonsCatalog.lessons.size)
    }
}
