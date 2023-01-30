package com.greenbay.api.utils

import com.greenbay.api.exception.GreenBayException
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.mail.StartTLSOptions

class EmailUtils(vertx: Vertx) {
    private lateinit var mailClient: MailClient

    init {
        val config = MailConfig().apply {
            hostname = System.getenv("GB_MAIL_HOST")
            port = Integer.valueOf(System.getenv("GB_MAIL_PORT"))
            username = System.getenv("GB_MAIL_USERNAME")
            password = System.getenv("GB_MAIL_PASSWORD")
            starttls = StartTLSOptions.REQUIRED
        }
        mailClient = MailClient.create(vertx, config)
    }

    private fun getMailClient() = mailClient

    fun sendMail(
        mailTo: String,
        mailSubject: String,
        body: String,
        htmlString: String? = null,
        success: (result: JsonObject) -> Unit,
        fail: (throwable: Throwable) -> Unit
    ) {
        val message = MailMessage().apply {
            from = "${System.getenv("GB_MAIL_ADDRESS")} Green Bay No reply"
            to = listOf(mailTo)
            subject = mailSubject
            text = body
            html = htmlString
        }
        try {
            getMailClient().sendMail(message)
                .onSuccess {
                    success.invoke(it.toJson())
                }.onFailure {
                    fail.invoke(it)
                }
        } catch (ex: Exception) {
            throw GreenBayException(ex.message, ex)
        }
    }
}