package com.karmios.modulo.core.ext.basiccommands

import kotlinx.serialization.toUtf8Bytes
import java.security.MessageDigest


private const val HASH_ALGORITHM = "SHA-256"

internal val String.hash: String
  get() = MessageDigest.getInstance(HASH_ALGORITHM).let { digest ->
      digest.digest(this.toUtf8Bytes()).toHex
  }

private val ByteArray.toHex: String
  get() {
      val sb = StringBuilder()
      this.forEach { byte ->
          val hex = Integer.toHexString(0xff and byte.toInt())
          if (hex.length == 1)
              sb.append('0')
          sb.append(hex)
      }
      return sb.toString()
  }
