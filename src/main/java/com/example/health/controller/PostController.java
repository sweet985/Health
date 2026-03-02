package com.example.health.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.health.common.CommonResult;
import com.example.health.entity.Comment;
import com.example.health.entity.Post;
import com.example.health.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    @PostMapping("/create")
    public CommonResult<String> createPost(@RequestBody Post post) {
        post.setUserId(getCurrentUserId());
        postService.createPost(post);
        return CommonResult.success("发布成功");
    }

    @GetMapping("/list")
    public CommonResult<Page<Post>> getPosts(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) Integer type,
                                             @RequestParam(required = false) Long userId,
                                             @RequestParam(required = false) String keyword) {
        Page<Post> pageParam = new Page<>(page, size);
        return CommonResult.success(postService.getPosts(pageParam, type, userId, keyword));
    }
    
    @GetMapping("/my")
    public CommonResult<Page<Post>> getMyPosts(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        Page<Post> pageParam = new Page<>(page, size);
        return CommonResult.success(postService.getPosts(pageParam, null, getCurrentUserId(), null));
    }

    @PostMapping("/delete/{id}")
    public CommonResult<String> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id, getCurrentUserId());
            return CommonResult.success("删除成功");
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @PostMapping("/like/{id}")
    public CommonResult<String> likePost(@PathVariable Long id) {
        postService.likePost(id, getCurrentUserId());
        return CommonResult.success("操作成功");
    }

    @PostMapping("/comment")
    public CommonResult<String> addComment(@RequestBody Comment comment) {
        comment.setUserId(getCurrentUserId());
        postService.addComment(comment);
        return CommonResult.success("回复成功");
    }

    @GetMapping("/comments/{postId}")
    public CommonResult<List<Comment>> getComments(@PathVariable Long postId) {
        return CommonResult.success(postService.getComments(postId));
    }

    @PostMapping("/comment/delete/{id}")
    public CommonResult<String> deleteComment(@PathVariable Long id) {
        try {
            postService.deleteComment(id, getCurrentUserId());
            return CommonResult.success("删除成功");
        } catch (RuntimeException e) {
            return CommonResult.failed(e.getMessage());
        }
    }
}
