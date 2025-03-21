package com.example.arabskanocticketqrscan

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LocalStorage {

    companion object {
        fun copyToInternalStorage(context: Context, fileName: String, destinationFileName: String) {
            val outputFile = File(context.filesDir, destinationFileName)

            if (!outputFile.exists()) {
                context.assets.open(fileName).use { inputStream ->
                    FileOutputStream(outputFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }

        fun retrieveAssetContent(context: Context, fileName: String): String {
            return context.assets.open(fileName).reader().readText()
        }

        fun retrieveFileContent(context: Context, fileName: String): String {
            val file = File(context.filesDir, fileName)
            return file.readText()
        }

        fun saveContentString(context: Context, fileName: String, content: String) {
            val file = File(context.filesDir, fileName)
            file.writeText(content)
        }
    }
}