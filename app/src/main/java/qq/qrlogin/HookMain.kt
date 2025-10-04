package qq.qrlogin;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import qq.qrlogin.util.Logger;

public class HookMain implements IXposedHookLoadPackage {

    private final String jsToInject =
            "(function() {\n" +
                    "    if (window.hasInjectedFinal) return;\n" +
                    "    window.hasInjectedFinal = true;\n" +
                    "    console.log('[QRLogin_JS_Final] Final hook script started.');\n" +
                    "    var responseTextDescriptor = Object.getOwnPropertyDescriptor(XMLHttpRequest.prototype, 'responseText');\n" +
                    "    Object.defineProperty(XMLHttpRequest.prototype, 'responseText', {\n" +
                    "        get: function() {\n" +
                    "            var url = this.responseURL || '';\n" +
                    "            if (url.includes('queryloginverifymethod')) {\n" +
                    "                console.log('[QRLogin_JS_Final] Intercepting responseText for: ' + url);\n" +
                    "                var fakeJson = '{\"retcode\":0,\"retmsg\":\"\",\"phoneVerify\":false,\"faceVerify\":true,\"userinfoVerify\":false,\"verifyInfoMap\":0,\"passwordVerify\":false,\"mbQAVerify\":false,\"getSupportUrlRsp\":\"\",\"uinCompletionVerify\":false,\"qrCodeVerify\":true,\"realnameVerify\":false,\"appeal\":0,\"guaranteeVerify\":false,\"robotVerify\":false,\"wechatVerify\":false,\"wechatNickname\":\"\"}';\n" +
                    "                return fakeJson;\n" +
                    "            }\n" +
                    "            return responseTextDescriptor.get.apply(this, arguments);\n" +
                    "        },\n" +
                    "        configurable: true\n" +
                    "    });\n" +
                    "    console.log('[QRLogin_JS_Final] XMLHttpRequest.responseText getter has been hooked.');\n" +
                    "})();";


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.tencent.mobileqq")) return;
        if (!lpparam.processName.equals("com.tencent.mobileqq:tool")) return;

        Logger.d("load >> " + lpparam.processName);

        try {
            final Class<?> webViewClientClass = XposedHelpers.findClass("com.tencent.smtt.sdk.WebViewClient", lpparam.classLoader);
            final Class<?> webViewClass = XposedHelpers.findClass("com.tencent.smtt.sdk.WebView", lpparam.classLoader);

            XposedHelpers.findAndHookMethod(
                    webViewClientClass,
                    "onPageFinished",
                    webViewClass,
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object webView = param.args[0];
                            String url = (String) param.args[1];

                            if (url != null && url.contains("accounts.qq.com/safe/verify")) {
                                Logger.i("!!! onPageFinished >> " + url);
                                XposedHelpers.callMethod(webView, "evaluateJavascript", jsToInject, null);
                                Logger.i("Injection successful!");
                            }
                        }
                    }
            );

            final Class<?> webChromeClientClass = XposedHelpers.findClass("com.tencent.smtt.sdk.WebChromeClient", lpparam.classLoader);
            final Class<?> consoleMessageClass = XposedHelpers.findClass("com.tencent.smtt.export.external.interfaces.ConsoleMessage", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(webChromeClientClass, "onConsoleMessage", consoleMessageClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Object consoleMessage = param.args[0];
                    if (consoleMessage != null) {
                        String message = (String) XposedHelpers.callMethod(consoleMessage, "message");
                        if (message.contains("QRLogin_JS_Final")) {
                            Logger.e("[script]: " + message);
                        }
                    }
                }
            });

            Logger.d("all monitoring points have been set up");

        } catch (Throwable t) {
            Logger.e("Hook failed: " + t.getMessage());
            XposedBridge.log(t);
        }
    }
}
