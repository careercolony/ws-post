package com.careercolony.postservices.model

case class Post(memberID: Int, title: String,
                description: String, post_type: String,
                author: String, thumbnail_url: String,
                post_url: String, readers: List[String])