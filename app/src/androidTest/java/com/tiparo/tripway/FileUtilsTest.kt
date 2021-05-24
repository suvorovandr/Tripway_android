package com.tiparo.tripway

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tiparo.tripway.utils.FileUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FileUtilsTest {
    @Test
    fun decodeSampledBitmapFromUriMedia_Test_ifExist() {
        //Here we need to obtain some uri from media
        val uri = Uri.parse("content://media/external/images/media/91783")
        val appContext = ApplicationProvider.getApplicationContext<BaseApplication>()

        val bitmap = FileUtils.decodeSampledBitmapFromUriMedia(
            appContext,
            uri,
            360,
            480
        )

        assertNotNull(bitmap)
    }

    @Test
    fun decodeSampledBitmapFromUriMedia_Test_NotExist() {
        //Here we need to obtain some uri from media
        val uri = Uri.parse("content://media/external/images/media/9999999999")
        val appContext = ApplicationProvider.getApplicationContext<BaseApplication>()

        assertThrows(Throwable::class.java) {
            FileUtils.decodeSampledBitmapFromUriMedia(
                appContext,
                uri,
                360,
                480
            )
        }
    }

    @Test
    fun getAppSpecificPhotoStorageFile_Test_NotExist() {
        //Here we need to obtain some uri from media
        val uri = Uri.parse("content://media/external/images/media/9999999999")
        val appContext = ApplicationProvider.getApplicationContext<BaseApplication>()

        val fileName = FileUtils.getAppSpecificPhotoStorageFile(
            appContext,
            uri.lastPathSegment ?: ""
        )

        println(fileName)
        assertNotNull(fileName)
    }

    @Test
    fun copyPhotoFromOuterStorageToApp_Test_IfExist() {
        //Here we need to obtain some uri from media
        val uri = Uri.parse("content://media/external/images/media/91783")
        val appContext = ApplicationProvider.getApplicationContext<BaseApplication>()

        val outputUri = FileUtils.copyPhotoFromOuterStorageToApp(uri, appContext)

        println(outputUri)
        assertNotNull("Inner uri from output uri=${uri} => ", outputUri)
    }

    @Test
    fun copyPhotoFromOuterStorageToApp_Test_IfNotExist() {
        //Here we need to obtain some uri from media
        val uri = Uri.parse("content://media/external/images/media/99999999999")
        val appContext = ApplicationProvider.getApplicationContext<BaseApplication>()

        val outputUri = FileUtils.copyPhotoFromOuterStorageToApp(uri, appContext)

        assertNull("Inner uri from unreal output uri=${uri} => ", outputUri)
    }
}
