package net.micode.moneytracker.util

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * specialized service for handling ZIP compression and decompression operations.
 * This class is decoupled from Android framework and app-specific logic (SRP).
 */
class ZipService {

    /**
     * Compresses a list of files into a ZIP archive.
     * @param filesWithNames List of pairs containing the file to compress and its entry name in the ZIP.
     * @param outputStream The destination stream for the ZIP data.
     */
    fun zipFiles(filesWithNames: List<Pair<File, String>>, outputStream: OutputStream) {
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
            filesWithNames.forEach { (file, entryName) ->
                if (file.exists()) {
                    FileInputStream(file).use { input ->
                        val entry = ZipEntry(entryName)
                        zipOut.putNextEntry(entry)
                        input.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                }
            }
        }
    }

    /**
     * Extracts files from a ZIP archive using a mapper to determine the destination of each entry.
     * @param inputStream The source stream of the ZIP data.
     * @param destinationMapper A function that maps an entry name to a target [File].
     */
    fun unzipFiles(inputStream: InputStream, destinationMapper: (String) -> File?) {
        ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                val outFile = destinationMapper(entry.name)
                outFile?.let {
                    it.parentFile?.mkdirs()
                    FileOutputStream(it).use { out -> zipIn.copyTo(out) }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    }
}
