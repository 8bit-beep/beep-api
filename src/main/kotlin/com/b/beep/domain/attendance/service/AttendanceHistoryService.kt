package com.b.beep.domain.attendance.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration

@Service
class AttendanceHistoryService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String
) {
    fun listFiles(): List<String> {
        val request = ListObjectsV2Request.builder()
            .bucket(bucket)
            .prefix("uploads")
            .build()

        return s3Client.listObjectsV2(request).contents().map { it.key() }
    }

    fun generatePresignedUrl(key: String, expiration: Duration = Duration.ofMinutes(10)): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(expiration)
            .getObjectRequest(getObjectRequest)
            .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }

    fun exists(key: String): Boolean {
        return try {
            val request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
            s3Client.headObject(request)
            true
        } catch (e: NoSuchKeyException) {
            false
        }
    }
}
