package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.User
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
    assertEquals("BoxHunter", appName)
  }

  @Test
  fun `verify initial user has 50 starting coins`() {
    val user = User(id = "test_user_1", displayName = "HunterPro")
    assertEquals(50L, user.coins)
    assertEquals(0L, user.level)
    assertTrue(user.ownedSkins.contains("skin_cyber"))
  }
}
