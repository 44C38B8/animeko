package me.him188.ani.utils.io

import kotlinx.io.files.Path
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.useDirectoryEntries
import java.nio.file.Path as NioPath


fun Path.toFile(): File = File(this.toString())
fun SystemPath.toFile(): File = path.toFile()

fun Path.toNioPath(): NioPath = Paths.get(this.toString())
fun SystemPath.toNioPath(): NioPath = path.toNioPath()

fun NioPath.toKtPath(): Path = Path(this.toString())
fun File.toKtPath(): Path = Path(this.toString())

actual inline fun <T> SystemPath.useDirectoryEntries(block: (Sequence<SystemPath>) -> T): T {
    return this.toNioPath().useDirectoryEntries { seq ->
        block(seq.map { SystemPath(it.toKtPath()) })
    }
}

actual fun SystemPath.length(): Long {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    return path.file.length()
}

actual fun SystemPath.isDirectory(): Boolean {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    return path.file.isDirectory
}

actual fun SystemPath.isRegularFile(): Boolean {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    return path.file.isFile
}

fun SystemPath.moveDirectoryRecursively(target: SystemPath, visitor: ((SystemPath) -> Unit)? = null) {
    val sourceDir = this.toNioPath()
    val targetDir = target.toNioPath()

    if (!Files.exists(sourceDir)) {
        throw NoSuchFileException(sourceDir.toFile(), null, "Source directory doesn't exist")
    }

    if (!Files.isDirectory(sourceDir)) {
        throw IllegalArgumentException("Source path is not a directory")
    }

    Files.createDirectories(targetDir)

    Files.walkFileTree(
        sourceDir,
        object : SimpleFileVisitor<NioPath>() {
            override fun preVisitDirectory(dir: NioPath, attrs: BasicFileAttributes): FileVisitResult {
                val targetPath = targetDir.resolve(sourceDir.relativize(dir))
                try {
                    Files.createDirectories(targetPath)
                } catch (e: IOException) {
                    throw IOException("Failed to create directory: $targetPath", e)
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: NioPath, attrs: BasicFileAttributes): FileVisitResult {
                val targetFile = targetDir.resolve(sourceDir.relativize(file))
                visitor?.invoke(file.toKtPath().inSystem)

                try {
                    Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING)
                } catch (e: IOException) {
                    throw IOException("Failed to move file: $file to $targetFile", e)
                }

                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: NioPath, exc: IOException?): FileVisitResult {
                if (exc != null) throw exc

                try {
                    Files.delete(dir)
                } catch (e: IOException) {
                    throw IOException("Failed to delete directory: $dir", e)
                }

                return FileVisitResult.CONTINUE
            }
        },
    )
}

actual val SystemPath.absolutePath: String
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    get() = path.file.absolutePath

actual fun SystemPaths.createTempDirectory(
    prefix: String,
): SystemPath = kotlin.io.path.createTempDirectory(prefix = prefix).toKtPath().inSystem

actual fun SystemPaths.createTempFile(
    prefix: String,
    suffix: String
): SystemPath = kotlin.io.path.createTempFile(prefix = prefix, suffix = suffix).toKtPath().inSystem
