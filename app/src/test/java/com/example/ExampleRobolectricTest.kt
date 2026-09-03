package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BoardCoordinates
import com.example.model.LudoColor
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
    assertEquals("King Mistry", appName)
  }

  @Test
  fun `verify ludo board coordinates and safe zones`() {
    assertEquals(52, BoardCoordinates.TRACK_COORDINATES.size)
    assertEquals(8, BoardCoordinates.SAFE_TRACK_INDICES.size)
    assertTrue(BoardCoordinates.isSafeSquare(LudoColor.RED, 0))
    assertTrue(BoardCoordinates.isSafeSquare(LudoColor.GREEN, 0))
    assertTrue(BoardCoordinates.isSafeSquare(LudoColor.YELLOW, 0))
    assertTrue(BoardCoordinates.isSafeSquare(LudoColor.BLUE, 0))
  }
}
