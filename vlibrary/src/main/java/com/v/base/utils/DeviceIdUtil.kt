package com.v.base.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and


object DeviceIdUtil {
    /**
     * 获得设备硬件标识
     *
     * @param context 上下文
     * @return 设备硬件标识
     */
    fun getDeviceId(context: Context): String {
        val sbDeviceId = StringBuilder()
        //获得设备默认IMEI（>=6.0 需要ReadPhoneState权限）
        //Android 6.0 以后需要获取动态权限  检查权限
        var imei = ""
        val hasWriteStoragePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        )
        if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            //拥有权限，执行操作
            imei = getIMEI(context)
        }

        //获得AndroidId（无需权限）
        val androidid = getAndroidId(context)
        //获得设备序列号（无需权限）
        val serial = sERIAL
        //获得硬件uuid（根据硬件相关属性，生成uuid）（无需权限）
        val uuid = deviceUUID.replace("-", "")
        //追加imei
        if (!imei.isNullOrEmpty()) {
            sbDeviceId.append(imei)
            sbDeviceId.append("|")
        }
        //追加androidid
        if (!androidid.isNullOrEmpty()) {
            sbDeviceId.append(androidid)
            sbDeviceId.append("|")
        }
        //追加serial
        if (!serial.isNullOrEmpty()) {
            sbDeviceId.append(serial)
            sbDeviceId.append("|")
        }
        //追加硬件uuid
        if (!uuid.isNullOrEmpty()) {
            sbDeviceId.append(uuid)
        }
        //生成SHA1，统一DeviceId长度
        if (sbDeviceId.isNotEmpty()) {
            try {
                val hash = getHashByString(sbDeviceId.toString())
                val sha1 = bytesToHex(hash)
                if (!sha1.isNullOrEmpty()) {
                    //返回最终的DeviceId
                    return sha1
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        //如果以上硬件标识数据均无法获得，
        //则DeviceId默认使用系统随机数，这样保证DeviceId不为空
        return UUID.randomUUID().toString().replace("-", "")
    }

    //需要获得READ_PHONE_STATE权限，>=6.0，默认返回null
    @SuppressLint("MissingPermission")
    private fun getIMEI(context: Context): String {
        try {
            val tm =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.deviceId
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    /**
     * 获得设备的AndroidId
     *
     * @param context 上下文
     * @return 设备的AndroidId
     */
    private fun getAndroidId(context: Context): String {
        try {
            return Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    /**
     * 获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取
     *
     * @return 设备序列号
     */
    private val sERIAL: String
        get() {
            try {
                return Build.SERIAL
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return ""
        }

    /**
     * 获得设备硬件uuid
     * 使用硬件信息，计算出一个随机数
     *
     * @return 设备硬件uuid
     */
    private val deviceUUID: String
        get() = try {
            val dev =
                "3883756" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.DEVICE.length % 10 + Build.HARDWARE.length % 10 + Build.ID.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.SERIAL.length % 10
            UUID(
                dev.hashCode().toLong(),
                Build.SERIAL.hashCode().toLong()
            ).toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }

    /**
     * 取SHA1
     *
     * @param data 数据
     * @return 对应的hash值
     */
    private fun getHashByString(data: String): ByteArray {
        return try {
            val messageDigest =
                MessageDigest.getInstance("SHA1")
            messageDigest.reset()
            messageDigest.update(data.toByteArray(charset("UTF-8")))
            messageDigest.digest()
        } catch (e: Exception) {
            "".toByteArray()
        }
    }

    /**
     * 转16进制字符串
     *
     * @param data 数据
     * @return 16进制字符串
     */
    private fun bytesToHex(data: ByteArray): String {
        val sb = StringBuilder()
        var stmp: String
        for (n in data.indices) {
            stmp = Integer.toHexString((data[n] and 0xFF.toByte()).toInt())
            if (stmp.length == 1) sb.append("0")
            sb.append(stmp)
        }
        return sb.toString().toUpperCase(Locale.CHINA)
    }
}