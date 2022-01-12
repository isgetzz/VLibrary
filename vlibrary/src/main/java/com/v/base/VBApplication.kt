package com.v.base

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.LayoutRes
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.v.base.net.VBLogInterceptor
import com.v.base.net.VBNetApi
import com.v.base.net.VBFastJsonConverterFactory
import com.v.base.utils.ext.vbEmptyView
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


abstract class VBApplication : Application() {


    companion object {
        private lateinit var context: VBApplication

        private var isNetToast = true

        private var baseUrl: String? = null
        private var builder: Retrofit.Builder? = null

        private var statusBarColor: Int = 0

        private var recyclerViewEmptyView: View? = null

        private var recyclerViewErrorView: Int? = null


        fun getApplication(): Application {
            return context
        }

        fun getRetrofitBuilder(): Retrofit.Builder? {
            return builder
        }

        fun getBaseUrl(): String? {
            return baseUrl
        }

        fun getStatusBarColor(): Int {
            return statusBarColor
        }

        fun getRecyclerViewEmptyView(): View? {
            return recyclerViewEmptyView
        }

        fun getRecyclerViewErrorView(): Int? {
            return recyclerViewErrorView
        }

        fun isNetToast(): Boolean {
            return isNetToast
        }

        //直接使用base库里面的网络请求
        val apiBase: VBNetApi by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            if (getBaseUrl().isNullOrEmpty()) {
                throw IllegalStateException("baseUrl为空,请继承BaseApplication重写baseUrl()")
            }
            getRetrofitBuilder()?.build()!!.create(VBNetApi::class.java)
        }
    }


    /**
     * 重写父类的okHttpClient方法，
     * 在这里可以添加拦截器
     */
    protected open fun okHttpClient(): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(10, TimeUnit.SECONDS)   //超时时间 连接、读、写
        readTimeout(5, TimeUnit.SECONDS)
        writeTimeout(5, TimeUnit.SECONDS)
        addInterceptor(VBLogInterceptor())// 日志拦截器
    }.build()

    /**
     * 重写父类的retrofitBuilder方法，
     * 在这里可以对Retrofit.Builder做任意操作，比如添加json解析器
     */
    protected open fun retrofitBuilder(): Retrofit.Builder = Retrofit.Builder()
        .apply {
            addConverterFactory(VBFastJsonConverterFactory.create())
            addCallAdapterFactory(CoroutineCallAdapterFactory())
        }

    /**
     * 使用 VLibrary库自带的网络请求时候请重写此方法
     */
    protected open fun baseUrl(): String = ""

    /**
     * 设置状态栏颜色
     */
    protected open fun statusBarColor(): Int = Color.parseColor("#000000")


    /**
     * 设置默认RecyclerView 数据为空界面
     */
    protected open fun recyclerViewEmptyView(): View? = null

    /**
     * 设置默认RecyclerView错误界面 只会在page为1并且没有数据的时候显示
     */
    @LayoutRes
    protected open fun recyclerViewErrorView(): Int? = R.layout.vb_layout_error


    /**
     * 是否开启debug模式 关联输出日志
     */
    protected open fun isDebug(): Boolean = true

    /**
     * 是否开启网络请求错误 弹出toast
     */
    protected open fun isNetToast(): Boolean = true

    override fun onCreate() {
        super.onCreate()
        context = this
        statusBarColor = statusBarColor()
        recyclerViewEmptyView = recyclerViewEmptyView()
        recyclerViewErrorView = recyclerViewErrorView()

        isNetToast = isNetToast()

        if (isDebug()) {
            Logger.addLogAdapter(AndroidLogAdapter())
        }

        if (!baseUrl().isNullOrEmpty()) {
            baseUrl = baseUrl()
            builder = retrofitBuilder().apply {
                baseUrl(baseUrl)
                client(okHttpClient())
            }

        }

        initSmartRefreshLayout()
        initData()

    }

    /**
     * 全局设置SmartRefreshLayout
     */
    private fun initSmartRefreshLayout() {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            ClassicsHeader(context)
        }

        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            ClassicsFooter(context)
        }
    }


    protected abstract fun initData()


}