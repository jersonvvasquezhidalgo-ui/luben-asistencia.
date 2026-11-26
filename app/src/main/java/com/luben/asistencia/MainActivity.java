package com.luben.asistencia;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private static final String SITE = "https://rrhh.confiterialuben.com";
    private static final int REQUEST_CAMERA = 17;
    private static final int REQUEST_SAVE = 42;
    private static final int REQUEST_UPLOAD = 43;
    private static final int REQUEST_LOCATION = 18;
    private GeolocationPermissions.Callback pendingGeo;
    private String pendingGeoOrigin;
    private byte[] pendingFile;
    private WebView web;
    private PermissionRequest pendingCamera;
    private ValueCallback<Uri[]> pendingUpload;
    private android.widget.ImageView splash;
    private long splashShown;
    /** Evita que el teclado del celular cambie mayúsculas o corrija el usuario y la contraseña. */
    private static final String LOGIN_FIELDS = "(function(){function f(){var l=document.querySelectorAll('input');"
            + "for(var i=0;i<l.length;i++){var e=l[i];if(e.type==='password'||/user|usuario|dni|pass|clave/i.test((e.name||'')+(e.id||'')+(e.autocomplete||'')+(e.placeholder||''))||e.type==='text'){"
            + "e.setAttribute('autocapitalize','none');e.setAttribute('autocorrect','off');e.setAttribute('spellcheck','false');}}}"
            + "f();if(!window.__lubenFields){window.__lubenFields=1;new MutationObserver(f).observe(document.documentElement,{childList:true,subtree:true});}})();";

    private void hideSplash() {
        if (splash == null || splash.getVisibility() != View.VISIBLE) return;
        splash.animate().alpha(0f).setDuration(350).withEndAction(new Runnable() {
            @Override public void run() { splash.setVisibility(View.GONE); }
        }).start();
    }

    private boolean isTrustedUrl(Uri uri) {
        return uri != null && "https".equalsIgnoreCase(uri.getScheme())
                && "rrhh.confiterialuben.com".equalsIgnoreCase(uri.getHost());
    }

    private boolean isOnSite() {
        return web != null && isTrustedUrl(Uri.parse(web.getUrl() == null ? "" : web.getUrl()));
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFFFFFFFF);
        getWindow().setNavigationBarColor(0xFFFFFFFF);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        web = new WebView(this);
        web.setBackgroundColor(0xffffffff);
        final android.widget.FrameLayout root = new android.widget.FrameLayout(this);
        root.setBackgroundColor(0xffffffff);
        root.addView(web, new android.widget.FrameLayout.LayoutParams(-1, -1));
        splash = new android.widget.ImageView(this);
        splash.setImageResource(R.drawable.splash);
        splash.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        splash.setBackgroundColor(0xffffffff);
        root.addView(splash, new android.widget.FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        splashShown = android.os.SystemClock.uptimeMillis();
        root.postDelayed(new Runnable() { @Override public void run() { hideSplash(); } }, 8000);
        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                return insets;
            }
        });

        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setGeolocationEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " LubenAsistencia/1.6");
        WebView.setWebContentsDebuggingEnabled(false);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false);

        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                if (isTrustedUrl(Uri.parse(url == null ? "" : url))) view.evaluateJavascript(LOGIN_FIELDS, null);
                long wait = Math.max(0, 1500 - (android.os.SystemClock.uptimeMillis() - splashShown));
                view.postDelayed(new Runnable() { @Override public void run() { hideSplash(); } }, wait);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (isTrustedUrl(uri)) return false;
                if ("https".equalsIgnoreCase(uri.getScheme())) {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
                return true;
            }
            @Override public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("No se pudo verificar la conexión segura.")
                        .setPositiveButton("Aceptar", null).show();
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                                        FileChooserParams params) {
                if (!isOnSite() || pendingUpload != null) {
                    callback.onReceiveValue(null);
                    return true;
                }
                pendingUpload = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, REQUEST_UPLOAD);
                } catch (Exception error) {
                    pendingUpload = null;
                    callback.onReceiveValue(null);
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("No se pudo abrir el selector de archivos.")
                            .setPositiveButton("Aceptar", null).show();
                }
                return true;
            }

            @Override public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (!isOnSite() || !SITE.equals(request.getOrigin().toString().replaceAll("/$", ""))) {
                            request.deny(); return;
                        }
                        boolean camera = false;
                        for (String resource : request.getResources())
                            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) camera = true;
                        if (!camera) { request.deny(); return; }
                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                        } else {
                            if (pendingCamera != null) pendingCamera.deny();
                            pendingCamera = request;
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                        }
                    }
                });
            }
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (!SITE.equals(origin.replaceAll("/$", ""))) { callback.invoke(origin, false, false); return; }
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false);
                } else {
                    if (pendingGeo != null) pendingGeo.invoke(pendingGeoOrigin, false, false);
                    pendingGeo = callback; pendingGeoOrigin = origin;
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                }
            }
            @Override public void onPermissionRequestCanceled(PermissionRequest request) {
                if (pendingCamera == request) pendingCamera = null;
            }
        });

        web.clearCache(true);
        web.addJavascriptInterface(new Downloads(), "LubenAndroid");
        web.loadUrl(SITE + "/");
    }

    public class Downloads {
        @JavascriptInterface public void saveFile(final String filename, final String base64, final String mime) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    if (!isOnSite() || pendingFile != null || base64 == null || base64.length() > 20000000) return;
                    if (!("application/pdf".equals(mime)
                            || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(mime))) return;
                    try {
                        pendingFile = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType(mime);
                        intent.putExtra(Intent.EXTRA_TITLE, filename.replaceAll("[^A-Za-z0-9_.-]", "_"));
                        startActivityForResult(intent, REQUEST_SAVE);
                    } catch (Exception error) {
                        pendingFile = null;
                        new AlertDialog.Builder(MainActivity.this).setMessage("No se pudo preparar la descarga.")
                                .setPositiveButton("Aceptar", null).show();
                    }
                }
            });
        }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == REQUEST_UPLOAD) {
            if (pendingUpload != null) {
                Uri[] files = result == RESULT_OK && data != null && data.getData() != null
                        ? new Uri[]{data.getData()} : null;
                pendingUpload.onReceiveValue(files);
                pendingUpload = null;
            }
        } else if (request == REQUEST_SAVE) {
            if (result == RESULT_OK && data != null && data.getData() != null && pendingFile != null) {
                try (java.io.OutputStream output = getContentResolver().openOutputStream(data.getData())) {
                    output.write(pendingFile);
                    android.widget.Toast.makeText(this, "Archivo guardado", android.widget.Toast.LENGTH_LONG).show();
                } catch (Exception error) {
                    new AlertDialog.Builder(this).setMessage("No se pudo guardar el archivo.")
                            .setPositiveButton("Aceptar", null).show();
                }
            }
            pendingFile = null;
        }
    }

    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == REQUEST_LOCATION && pendingGeo != null) {
            boolean ok = false;
            for (int r : results) if (r == PackageManager.PERMISSION_GRANTED) ok = true;
            pendingGeo.invoke(pendingGeoOrigin, ok && isOnSite(), false);
            pendingGeo = null; pendingGeoOrigin = null;
        }
        if (code == REQUEST_CAMERA && pendingCamera != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && isOnSite())
                pendingCamera.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
            else pendingCamera.deny();
            pendingCamera = null;
        }
    }

    @Override protected void onPause() {
        super.onPause();
        if (web != null) web.onPause();
        CookieManager.getInstance().flush();
    }
    @Override protected void onResume() { super.onResume(); if (web != null) web.onResume(); }
    @Override public void onBackPressed() { if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() {
        if (pendingCamera != null) pendingCamera.deny();
        if (pendingUpload != null) pendingUpload.onReceiveValue(null);
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
