package com.bujji.access

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.net.ServerSocket
import kotlin.concurrent.thread

class BujjiService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        startServer()
    }

    private fun startServer() {
        thread {
            val server = ServerSocket(8766)

            while (true) {
                val client = server.accept()
                val request = client.getInputStream().bufferedReader().readLine()

                if (request.contains("dump")) {
                    val text = dumpScreen(rootInActiveWindow)
                    client.getOutputStream().write(text.toByteArray())
                }

                if (request.contains("click:")) {
                    val target = request.substringAfter("click:")
                    clickText(rootInActiveWindow, target)
                }

                client.close()
            }
        }
    }

    private fun dumpScreen(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""

        var result = ""
        if (node.text != null) result += node.text.toString() + "\n"

        for (i in 0 until node.childCount)
            result += dumpScreen(node.getChild(i))

        return result
    }

    private fun clickText(node: AccessibilityNodeInfo?, target: String) {
        if (node == null) return

        if (node.text != null && node.text.toString().contains(target, true)) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return
        }

        for (i in 0 until node.childCount)
            clickText(node.getChild(i), target)
    }
}
