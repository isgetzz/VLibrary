package com.v.demo.bean

/**
 * @Author : ww
 * desc    :
 * time    : 2021/1/12 16:00
 */

data class OneBean(
    val _id: String,
    val author: String,
    val category: String,
    val createdAt: String,
    val desc: String,
    val images: List<String>,
    val likeCounts: Int,
    val publishedAt: String,
    val stars: Int,
    val title: String,
    val type: String,
    val url: String,
    val views: Int
)