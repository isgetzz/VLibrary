package com.v.base.net

import com.v.base.VBApplication
import com.v.base.VBConfig
import com.v.base.annotaion.VBError
import com.v.base.utils.ext.logE
import com.v.base.utils.toast
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException


/**
 * author  : ww
 * desc    : 错误处理
 * time    : 2021-03-16 09:52:45
 */
object VBExceptionHandle {

    fun handleException(e: Throwable,isToast :Boolean): VBAppException {
        val ex: VBAppException
        e.let {
            when (it) {
                is VBAppException -> {
                    ex = it
                }
                is HttpException -> {
                    ex = VBAppException(VBError.NETWORK_ERROR, e)
                }
                is ConnectException -> {
                    ex = VBAppException(VBError.NETWORK_ERROR, e)
                }
                is javax.net.ssl.SSLException -> {
                    ex = VBAppException(VBError.SSL_ERROR, e)
                }
                is ConnectTimeoutException -> {
                    ex = VBAppException(VBError.TIMEOUT_ERROR, e)
                }
                is java.net.SocketTimeoutException -> {
                    ex = VBAppException(VBError.TIMEOUT_ERROR, e)
                }
                is java.net.UnknownHostException -> {
                    ex = VBAppException(VBError.TIMEOUT_ERROR, e)
                }
                is JSONException -> {
                    ex = VBAppException(VBError.PARSE_ERROR, e)
                }
                else -> {
                    ex = VBAppException(VBError.UNKNOWN, e)
                }
            }
        }
        (ex.toString()).logE()
        if (VBConfig.options.netOptions.isNetToast || isToast) {
            (ex.errorMsg).toast()
        }
        return ex
    }
}