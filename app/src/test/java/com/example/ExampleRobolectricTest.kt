package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.rfexplorer.data.scanner.HardwareScannerEngines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RF Explorer", appName)
  }

  @Test
  fun `verify hardware scanner benchmark libraries`() {
    val libs = HardwareScannerEngines.scanAllLibraries()
    assertTrue(libs.isNotEmpty())
    val fmLibs = libs.filter { it.name.contains("fm", ignoreCase = true) }
    assertTrue(fmLibs.isNotEmpty())
  }

  @Test
  fun `verify activation evaluation paths`() {
    val evals = HardwareScannerEngines.evaluateActivationMethods()
    assertEquals(8, evals.size)
  }
}
