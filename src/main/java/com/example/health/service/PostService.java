package com.example.health.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.health.entity.Comment;
import com.example.health.entity.Post;

import java.util.List;

public interface PostService extends IService<Post> {
    void createPost(Post post);
    Page<Post> getPosts(Page<Post> page, Integer type, Long userId, String keyword); // Added keyword for search
    void deletePost(Long postId, Long userId);
    void likePost(Long postId, Long userId);
    void addComment(Comment comment);
    List<Comment> getComments(Long postId);
    void deleteComment(Long commentId, Long userId);
    void deletePostsByUserId(Long userId);
}
