package com.example.arabskanocticketqrscan

interface IDeserialize<T> {

    fun fromJson(json: String): T
}