package qq.qrlogin

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import qq.qrlogin.util.Logger

class HookMain : IXposedHookLoadPackage {

    private val jsToInject = """
        (function() {
            if (window.hasInjectedFinal) return;
            window.hasInjectedFinal = true;
            console.log('[QRLogin_JS_Final] Final hook script started.');
            var responseTextDescriptor = Object.getOwnPropertyDescriptor(XMLHttpRequest.prototype, 'responseText');
            Object.defineProperty(XMLHttpRequest.prototype, 'responseText', {
                get: function() {
                    var url = this.responseURL || '';
                    if (url.includes('queryloginverifymethod')) {
                        console.log('[QRLogin_JS_Final] Intercepting responseText for: ' + url);
                        var fakeJson = '{"retcode":0,"retmsg":"","phoneVerify":false,"faceVerify":true,"userinfoVerify":false,"verifyInfoMap":0,"passwordVerify":false}';
                        return fakeJson;
                    }
                    return responseTextDescriptor.get.apply(this, arguments);
                },
                configurable: true
            });
            console.log('[QRLogin_JS_Final] XMLHttpRequest.responseText getter has been hooked.');
        })();
    """.trimIndent()

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "com.tencent.mobileqq" || lpparam.processName != "com.tencent.mobileqq:tool") {
            return
        }

        Logger.d("loading to ${lpparam.processName}")

        try {
            val webViewClientClass = XposedHelpers.findClass("com.tencent.smtt.sdk.WebViewClient", lpparam.classLoader)
            val webViewClass = XposedHelpers.findClass("com.tencent.smtt.sdk.WebView", lpparam.classLoader)
            val webChromeClientClass = XposedHelpers.findClass("com.tencent.smtt.sdk.WebChromeClient", lpparam.classLoader)
            val consoleMessageClass = XposedHelpers.findClass("com.tencent.smtt.export.external.interfaces.ConsoleMessage", lpparam.classLoader)

            XposedHelpers.findAndHookMethod(
                webViewClientClass,
                "onPageFinished",
                webViewClass,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val webView = param.args[0]
                        val url = param.args[1] as String

                        if (url.contains("accounts.qq.com/safe/verify") == true) {
                            Logger.i("!!! onPageFinished >> $url")
                            XposedHelpers.callMethod(webView, "evaluateJavascript", jsToInject, null)
                            Logger.i("Injection successful!")
                        }
                    }
                }
            )

            XposedHelpers.findAndHookMethod(
                webChromeClientClass,
                "onConsoleMessage",
                consoleMessageClass,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.args[0]!!.let { consoleMessage ->
                            val message = XposedHelpers.callMethod(consoleMessage, "message") as String
                            if (message.contains("QRLogin_JS_Final") == true) {
                                Logger.e("[script]: $message")
                            }
                        }
                    }
                }
            )

            Logger.d("all monitoring points have been set up")

        } catch (t: Throwable) {
            Logger.e("Hook failed: ${t.message}")
            XposedBridge.log(t)
        }
    }
}