package io.agora.metachat.tools

import android.content.Context
import java.io.*

object FileUtil {
    fun saveFile(context: Context, data: ByteArray, folder: String, fileName: String) {
        val dir = File(context.filesDir, folder)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, fileName)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        var fos: BufferedOutputStream? = null
        try {
            fos = BufferedOutputStream(FileOutputStream(file))
            fos.write(data, 0, data.size)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.flush()
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {

                }
            }
        }
    }

    fun copyAssetFile(context: Context, fileName: String,
                      overwrite: Boolean = false,
                      listener: FileResultListener? = null) {
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null

        try {
            bis = BufferedInputStream(context.assets.open(fileName))
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                if (overwrite) {
                    file.delete()
                    file.createNewFile()
                } else {
                    listener?.onSuccess()
                    return
                }
            }

            bos = BufferedOutputStream(FileOutputStream(file))
            val buf = ByteArray(512)
            var read: Int
            while ((bis.read(buf, 0, buf.size).also { read = it }) != -1) {
                bos.write(buf, 0, read)
            }

            listener?.onSuccess()
        } catch (e: IOException) {
            e.printStackTrace()
            listener?.onFail()
        } finally {
            if (bis != null) {
                try {
                    bis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            if (bos != null) {
                try {
                    bos.flush()
                    bos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}

interface FileResultListener {
    fun onSuccess()

    fun onFail()
}