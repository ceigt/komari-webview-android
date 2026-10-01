package io.github.ceigt.komari

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebViewRegressionTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var server: ServerSocket
    private lateinit var serverThread: Thread
    private lateinit var baseUrl: String

    @Before
    fun setUp() {
        check(context.packageName.endsWith(".debug")) { "Tests must only modify the debug app" }
        server = ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"))
        baseUrl = "http://127.0.0.1:${server.localPort}/"
        serverThread = Thread {
            while (!server.isClosed) {
                try {
                    server.accept().use { socket ->
                        socket.soTimeout = 3000
                        val reader = socket.getInputStream().bufferedReader()
                        val request = reader.readLine().orEmpty()
                        while (!reader.readLine().isNullOrEmpty()) { /* read headers */ }
                        val title = if (request.contains(" /details ")) "Details" else "Home"
                        val body = "<html><head><title>$title</title></head>" +
                            "<body><a href='/details'>Details</a><input aria-label='Name'></body></html>"
                        val bytes = body.toByteArray()
                        socket.getOutputStream().apply {
                            write(("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\n" +
                                "Content-Length: ${bytes.size}\r\nConnection: close\r\n\r\n").toByteArray())
                            write(bytes)
                            flush()
                        }
                    }
                } catch (_: java.io.IOException) { /* closed during cleanup */ }
            }
        }.apply { isDaemon = true; start() }
        context.getSharedPreferences("komari_preferences", 0).edit()
            .clear().putString("server_url", baseUrl).commit()
    }

    @After
    fun tearDown() {
        server.close()
        serverThread.join(3000)
        context.getSharedPreferences("komari_preferences", 0).edit().clear().commit()
    }

    @Test
    fun navigationSurvivesActivityRecreationAndBackReturnsToPreviousPage() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            waitForPage(scenario, "Home")
            scenario.onActivity { activity ->
                findWebView(activity.window.decorView)!!.evaluateJavascript(
                    "location.href='/details'", null
                )
            }
            waitForPage(scenario, "Details")
            scenario.recreate()
            waitForPage(scenario, "Details")
            scenario.onActivity { activity ->
                assertTrue(findWebView(activity.window.decorView)!!.canGoBack())
                activity.onBackPressedDispatcher.onBackPressed()
            }
            waitForPage(scenario, "Home")
        }
    }

    @Test
    fun launcherUsesAdaptiveIconAndRendersOnTheDevice() {
        val icon = context.packageManager.getApplicationIcon(context.packageName)
        assertTrue("API 26+ must use an adaptive icon", icon is AdaptiveIconDrawable)
        val bitmap = Bitmap.createBitmap(432, 432, Bitmap.Config.ARGB_8888)
        icon.setBounds(0, 0, bitmap.width, bitmap.height)
        icon.draw(Canvas(bitmap))
        File(context.getExternalFilesDir(null), "launcher-icon.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        assertNotEquals("Icon must have visible artwork", 0, bitmap.getPixel(216, 216))
        bitmap.recycle()
    }

    private fun waitForPage(scenario: ActivityScenario<MainActivity>, title: String) {
        val loaded = AtomicBoolean(false)
        val deadline = System.currentTimeMillis() + 15000
        while (System.currentTimeMillis() < deadline) {
            scenario.onActivity { activity ->
                val webView = findWebView(activity.window.decorView)!!
                loaded.set(webView.title == title && webView.progress == 100)
            }
            if (loaded.get()) return
            Thread.sleep(100)
        }
        fail("Page did not finish loading: $title")
    }

    private fun findWebView(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findWebView(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }
}
