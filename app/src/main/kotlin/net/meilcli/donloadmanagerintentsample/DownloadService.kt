package net.meilcli.donloadmanagerintentsample

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toFile
import android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE as downloadManagerActionDownloadComplete
import android.app.DownloadManager.COLUMN_LOCAL_URI as downloadManagerColumnLocalUri
import android.app.DownloadManager.COLUMN_STATUS as downloadManagerColumnStatus
import android.app.DownloadManager.EXTRA_DOWNLOAD_ID as downloadManagerExtraDownloadId
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED as downloadManagerNotificationVisibilityVisibleNotifyCompleted
import android.app.DownloadManager.STATUS_SUCCESSFUL as downloadManagerStatusSuccessful
import android.content.Intent.ACTION_VIEW as intentActionView
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK as intentFlagActivityNewTask
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION as intentFlagGrantReadUriPermission
import android.content.pm.PackageManager.MATCH_ALL as packageManagerMatchAll
import android.os.Environment.DIRECTORY_DOWNLOADS as environmentDirectoryDownloads

class DownloadService(private val context: Context) {

    companion object {

        // Manifestで指定したauthoritiesと同じ値にする
        // 2重定義が冗長なのでbuild.gradleあたりで定義したほうがいいかも
        private const val authorities = "net.meilcli.donloadmanagerintentsample.fileprovider"
    }

    private val downloadManager = checkNotNull(context.getSystemService<DownloadManager>())

    private val onDownloadCompleted = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val downloadId = intent.getLongExtra(downloadManagerExtraDownloadId, 0)
            val mimeType = downloadManager.getMimeTypeForDownloadedFile(downloadId)

            val query = DownloadManager.Query().apply {
                setFilterById(downloadId)
            }
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst().not()) {
                return
            }

            val status = cursor.getInt(cursor.getColumnIndex(downloadManagerColumnStatus))

            if (status != downloadManagerStatusSuccessful) {
                return
            }

            val path = cursor.getString(cursor.getColumnIndex(downloadManagerColumnLocalUri))
            // file:///storageから始まってしまうのでUriを経由して/storageから始まるようにする
            val uri = Uri.parse(path)
            val contentUri = FileProvider.getUriForFile(this@DownloadService.context, authorities, uri.toFile())

            sendIntent(contentUri, mimeType)
        }
    }

    init {
        context.registerReceiver(onDownloadCompleted, IntentFilter(downloadManagerActionDownloadComplete))
    }

    fun download(url: String, fileName: String) {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri).apply {
            setDestinationInExternalFilesDir(context, environmentDirectoryDownloads, fileName)
            setNotificationVisibility(downloadManagerNotificationVisibilityVisibleNotifyCompleted)
            setVisibleInDownloadsUi(true)
        }
        downloadManager.enqueue(request)
    }

    private fun sendIntent(contentUri: Uri, mimeType: String) {
        Log.d("AAA", "contentUri: $contentUri")

        val packageManager = context.packageManager
        val intent = Intent().apply {
            action = intentActionView
            addFlags(intentFlagGrantReadUriPermission)
            addFlags(intentFlagActivityNewTask)
            setDataAndType(contentUri, mimeType)
        }

        if (packageManager.resolveActivity(intent, packageManagerMatchAll) == null) {
            return
        }

        context.startActivity(intent)
    }
}