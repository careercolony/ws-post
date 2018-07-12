package com.careercolony.postservices.model


case class GetPost(memberID: Int, title: String,
                   description: String, post_type: String,
                   author: String, thumbnail_url: String,
                   post_url: String, post_date: String,
                   postID: Int, readers: Option[List[String]])
