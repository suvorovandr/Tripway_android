package com.tiparo.tripway.utils

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import android.os.Environment
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun copyPhotoFromOuterStorageToApp(photoUri: Uri, application: Application): Uri? {
        //TODO обработать возможные ошибки и кейсы с файлами
        //TODO понять какой размер для сжатия выбрать
        try {
            val bitmapPhoto = decodeSampledBitmapFromUriMedia(
                application,
                photoUri,
                480,
                640
            )

            //TODO гененрировать уникальные имена
            val file = getAppSpecificPhotoStorageFile(
                application,
                photoUri.lastPathSegment ?: ""
            )
            //TODO определить оптимальный формат файла
            val outF = FileOutputStream(file)

            val compressSuccess =
                bitmapPhoto.compress(Bitmap.CompressFormat.JPEG, 100, outF)

            if (!compressSuccess) {
                throw Exception("Fail when trying to compress bitmap. Uri = $photoUri")
            }
            return Uri.fromFile(file)

        } catch (exception: Throwable) {
            Timber.e(exception)
            return null
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledBitmapFromUriMedia(
        application: Application,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap =
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options().run {
            var input = getInputStreamFromUri(application, uri)

            inJustDecodeBounds = true
            inPreferredConfig = Bitmap.Config.ARGB_8888

            try {
                BitmapFactory.decodeStream(input, null, this)
            } catch (exception: Throwable) {
                throw Exception(
                    "The image data could not be decoded. Uri = $uri",
                    exception
                )
            }

            input.close()

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            input = getInputStreamFromUri(application, uri)

            val bitmap = try {
                BitmapFactory.decodeStream(input, null, this)
            } catch (exception: Throwable) {
                throw Exception(
                    "The image data could not be decoded. Uri = $uri",
                    exception
                )
            } ?: throw Exception("Bitmap of uri = $uri is null!")

            val rotatedBitmap = rotateImageIfRequired(application, bitmap, uri)

            input.close()

            rotatedBitmap
        }

    private fun getInputStreamFromUri(application: Application, uri: Uri) = try {
        application.contentResolver.openInputStream(uri)
    } catch (exception: Throwable) {
        throw Exception(
            "Cant't do openInputStream(). Uri = $uri",
            exception
        )
    } ?: throw Exception("InputStream of uri = $uri is null!")

    private fun rotateImageIfRequired(application: Application, bitmap: Bitmap, uri: Uri) : Bitmap {
        val input = getInputStreamFromUri(application, uri)
        val ei = ExifInterface(input)

        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        input.close()

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }

    fun getAppSpecificPhotoStorageFile(context: Context, photoName: String): File =
    // Get the picture directory that's inside the app-specific directory on
    // external storage.
        //TODO тут могут возникнуть ошибки
        File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), photoName)
}