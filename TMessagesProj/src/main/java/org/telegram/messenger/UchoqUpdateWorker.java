package org.telegram.messenger;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class UchoqUpdateWorker extends Worker {

    private static final String API_URL = "https://api.github.com/repos/javaoff364-cloud/Uchoq/releases/latest";
    private static final String CHANNEL_ID = "uchoq_updates";
    private static final int NOTIFICATION_ID = 7184;

    public UchoqUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (!isNetworkAvailable()) {
                return Result.retry();
            }

            JSONObject release = fetchRelease();
            if (release == null || release.optBoolean("draft", true) || release.optBoolean("prerelease", true)) {
                return Result.success();
            }

            String tag = release.optString("tag_name", "");
            String latestVersion = tag.startsWith("v") ? tag.substring(1) : tag;
            String currentVersion = BuildVars.BUILD_VERSION_STRING;

            if (isNewerVersion(latestVersion, currentVersion) && shouldNotify(latestVersion)) {
                String downloadUrl = release.optString("html_url", "https://github.com/javaoff364-cloud/Uchoq/releases/latest");
                String releaseNotes = release.optString("body", "");
                showUpdateNotification(latestVersion, downloadUrl, releaseNotes);
            }
            return Result.success();
        } catch (Exception e) {
            FileLog.e(e);
            return Result.retry();
        }
    }

    private JSONObject fetchRelease() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(15000);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("User-Agent", "Uchoq-Android");
        try (InputStream stream = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return new JSONObject(result.toString());
        } finally {
            connection.disconnect();
        }
    }

    private boolean isNetworkAvailable() {
        try {
            android.net.ConnectivityManager cm =
                    (android.net.ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.net.Network network = cm.getActiveNetwork();
                if (network == null) return false;
                android.net.NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                return capabilities != null && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            android.net.NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNewerVersion(String latest, String current) {
        try {
            String[] a = latest.split("\\.");
            String[] b = current.split("\\.");
            int length = Math.max(a.length, b.length);
            for (int i = 0; i < length; i++) {
                int av = i < a.length ? parseVersionPart(a[i]) : 0;
                int bv = i < b.length ? parseVersionPart(b[i]) : 0;
                if (av != bv) return av > bv;
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    private int parseVersionPart(String value) {
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isDigit(c)) digits.append(c);
            else break;
        }
        return digits.length() == 0 ? 0 : Integer.parseInt(digits.toString());
    }

    private boolean shouldNotify(String version) {
        android.content.SharedPreferences prefs = getApplicationContext().getSharedPreferences("uchoq_updates", Context.MODE_PRIVATE);
        String lastNotified = prefs.getString("last_notified_version", "");
        if (version.equals(lastNotified)) return false;
        prefs.edit().putString("last_notified_version", version).apply();
        return true;
    }

    private void showUpdateNotification(String version, String url, String notes) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.UchoqUpdateChannel),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                7184,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String text = context.getString(R.string.UchoqUpdateAvailableText, version);
        if (notes != null && !notes.trim().isEmpty()) {
            text += " " + notes.replaceAll("\\s+", " ").trim();
            if (text.length() > 180) text = text.substring(0, 177) + "...";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.UchoqUpdateAvailable))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(NOTIFICATION_ID, builder.build());
    }
}
