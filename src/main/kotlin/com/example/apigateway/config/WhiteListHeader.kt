package com.example.apigateway.config

object  WhiteListHeader {
    val contentLength: String = "Content-Length"
    val Host: String = "Host"
    val Connection: String = "Connection"
    val contentType: String = "Content-Type"
    val UserAgent: String = "User-Agent"
    val AcceptEncoding: String = "Accept-Encoding"
    val customHeader1: String = "X-Authorization-Id"
    val customHeader2: String = "X-Authorization-role"
    val customHeader3: String = "X-Authorization-email"
    val whiteList = listOf<String>(
        contentLength,
        Host,
        Connection,
        UserAgent,
        AcceptEncoding,
        customHeader1,
        customHeader2,
        customHeader3,
        contentType
    )
}

