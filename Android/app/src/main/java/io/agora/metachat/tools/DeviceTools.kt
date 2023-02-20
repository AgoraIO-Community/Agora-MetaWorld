package io.agora.metachat.tools

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.Size
import android.util.TypedValue
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import java.text.DecimalFormat
import java.util.*

/**
 * @author create by zhangwei03
 *
 */
object DeviceTools {

    private var isZh = false

    private var languageCode:String = ""

    @JvmStatic
    fun getIsZh(): Boolean = isZh

    @JvmStatic
    fun getLanguageCode():String = languageCode

    @JvmStatic
    @ColorInt
    fun getColor(resources: Resources, @ColorRes id: Int, theme: Resources.Theme? = null): Int {
        return ResourcesCompat.getColor(resources, id, theme)
    }

    @JvmStatic
    fun getDrawableId(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    @JvmStatic
    fun isZh(context: Context): Boolean {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }
        isZh = locale.country == "CN"
        languageCode = locale.country
        return isZh
    }

    @JvmStatic
    fun getDisplaySize(): Size {
        val metrics = Resources.getSystem().displayMetrics
        return Size(metrics.widthPixels, metrics.heightPixels)
    }

    @JvmStatic
    fun dp2px(dp: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        )
    }

    @JvmStatic
    fun sp2px(sp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), Resources.getSystem().displayMetrics)
    }

    /**
     * 获取屏幕宽度
     */
    @JvmStatic
    fun screenWidth(activity: Activity): Int {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.x
    }

    /**
     * 获取屏幕高度
     */
    @JvmStatic
    fun screenHeight(activity: Activity): Int {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.y
    }

    @JvmStatic
    fun isMainProcess(context: Context): Boolean {
        val processName: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessNameByApplication()
        } else {
            getProcessNameByReflection()
        }
        return context.applicationInfo.packageName == processName
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private fun getProcessNameByApplication(): String? {
        return Application.getProcessName()
    }

    private fun getProcessNameByReflection(): String? {
        var processName: String? = null
        try {
            val declaredMethod = Class.forName(
                "android.app.ActivityThread", false,
                Application::class.java.classLoader
            )
                .getDeclaredMethod("currentProcessName", *arrayOfNulls<Class<*>?>(0))
            declaredMethod.isAccessible = true
            val invoke = declaredMethod.invoke(null, *arrayOfNulls(0))
            if (invoke is String) {
                processName = invoke
            }
        } catch (e: Throwable) {
        }
        return processName
    }

    fun getNetFileSizeDescription(size: Long): String {
        val bytes = StringBuffer()
        val format = DecimalFormat("###.0")
        if (size >= 1024 * 1024 * 1024) {
            val i = size / (1024.0 * 1024.0 * 1024.0)
            bytes.append(format.format(i)).append("GB")
        } else if (size >= 1024 * 1024) {
            val i = size / (1024.0 * 1024.0)
            bytes.append(format.format(i)).append("MB")
        } else if (size >= 1024) {
            val i = size / 1024.0
            bytes.append(format.format(i)).append("KB")
        } else {
            if (size <= 0) {
                bytes.append("0B")
            } else {
                bytes.append(size.toInt()).append("B")
            }
        }
        return bytes.toString()
    }

}

