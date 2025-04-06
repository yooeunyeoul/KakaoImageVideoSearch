package com.example.kakaoimagevideosearch.utils

class ApiException(val code: Int, override val message: String) : Exception(message)