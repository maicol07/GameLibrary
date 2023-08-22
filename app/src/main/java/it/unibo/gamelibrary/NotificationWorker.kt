package it.unibo.gamelibrary

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import it.unibo.gamelibrary.utils.channel_id
import it.unibo.gamelibrary.utils.notificationId


class NotificationWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {

        // Do the work here
        val gameId = inputData.getInt("gameId", 0)
        val gameName = inputData.getString("gameName")
        if (gameId != 0) {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("app://game-library/game/$gameId")
            }
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("$gameName is out today")
                .setContentText("Click here to see it!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
            notificationId++

            // Indicate whether the work finished successfully with the Result
            return Result.success()
        } else {
            return Result.failure()
        }
    }
}

