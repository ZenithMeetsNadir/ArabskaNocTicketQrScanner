package com.example.arabskanocticketqrscan

interface IFromJson<T> {

    fun parseFrom(json: String): T
    fun getJson(obj: T): String
}