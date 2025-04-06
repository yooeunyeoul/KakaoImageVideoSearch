package com.example.myapplication.utils

class ApiException(val code: Int, override val message: String) : Exception(message)