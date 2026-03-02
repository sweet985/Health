package com.example.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.health.entity.Comment;
import com.example.health.entity.Post;
import com.example.health.entity.PostLike;
import com.example.health.entity.User;
import com.example.health.mapper.CommentMapper;
import com.example.health.mapper.PostLikeMapper;
import com.example.health.mapper.PostMapper;
import com.example.health.mapper.UserMapper;
import com.example.health.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private PostLikeMapper postLikeMapper;
    
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void createPost(Post post) {
        if (post.getType() == 1 && post.getPseudonym() != null) {
             // Check if pseudonym is used in this thread? No, createPost starts a thread.
             // But maybe globally unique in TreeHole? Requirement says "同一个贴子里的化名不能重复".
             // Since this is a new post, it is the first pseudonym in this thread (itself).
             // No check needed for createPost.
        }
        post.setLikeCount(0);
        post.setReplyCount(0);
        save(post);
    }

    @Override
    public Page<Post> getPosts(Page<Post> page, Integer type, Long userId, String keyword) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        if (type != null) {
            wrapper.eq("type", type);
        }
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }
        wrapper.orderByDesc("create_time");
        
        Page<Post> result = page(page, wrapper);
        
        for (Post p : result.getRecords()) {
            if (p.getType() == 2) { // Community
                User u = userMapper.selectById(p.getUserId());
                if (u != null) {
                    p.setUsername(u.getUsername());
                    p.setUserAvatar(u.getAvatar());
                }
            } else { // TreeHole
                 if (p.getPseudonym() != null && !p.getPseudonym().isEmpty()) {
                     p.setUsername(p.getPseudonym() + " (化名)");
                 } else {
                     p.setUsername("匿名用户");
                 }
                 p.setUserAvatar(""); 
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = getById(postId);
        if (post == null) return;
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人帖子");
        }
        removeById(postId);
        commentMapper.delete(new QueryWrapper<Comment>().eq("post_id", postId));
        postLikeMapper.delete(new QueryWrapper<PostLike>().eq("post_id", postId));
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        QueryWrapper<PostLike> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", postId).eq("user_id", userId);
        PostLike like = postLikeMapper.selectOne(wrapper);
        
        Post post = getById(postId);
        if (like != null) {
            // Already liked -> Unlike
            postLikeMapper.deleteById(like.getId());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        } else {
            // Like
            like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            post.setLikeCount(post.getLikeCount() + 1);
        }
        updateById(post);
    }

    @Override
    @Transactional
    public void addComment(Comment comment) {
        Post post = getById(comment.getPostId());
        
        // If TreeHole
        if (post.getType() == 1) {
             // 1. If user is the author, force use author's pseudonym (or lack thereof)
             if (comment.getUserId().equals(post.getUserId())) {
                 comment.setPseudonym(post.getPseudonym());
             } 
             // 2. If user already commented on this post, use previous pseudonym
             else {
                 QueryWrapper<Comment> prevCommentWrapper = new QueryWrapper<>();
                 prevCommentWrapper.eq("post_id", comment.getPostId())
                                   .eq("user_id", comment.getUserId())
                                   .last("LIMIT 1");
                 Comment prevComment = commentMapper.selectOne(prevCommentWrapper);
                 if (prevComment != null) {
                     comment.setPseudonym(prevComment.getPseudonym());
                 }
             }
             
             // 3. Uniqueness check (only if new pseudonym provided and not overwritten above)
             if (comment.getPseudonym() != null) {
                 // Check if matches author (if not author)
                 if (!comment.getUserId().equals(post.getUserId()) && 
                     comment.getPseudonym().equals(post.getPseudonym())) {
                     throw new RuntimeException("化名已存在");
                 }
                 
                 // Check if matches other users
                 QueryWrapper<Comment> check = new QueryWrapper<>();
                 check.eq("post_id", comment.getPostId())
                      .eq("pseudonym", comment.getPseudonym())
                      .ne("user_id", comment.getUserId()); // Exclude self
                 if (commentMapper.selectCount(check) > 0) {
                     throw new RuntimeException("化名已存在");
                 }
             }
        }
        
        commentMapper.insert(comment);
        post.setReplyCount(post.getReplyCount() + 1);
        updateById(post);
    }

    @Override
    public List<Comment> getComments(Long postId) {
        Post post = getById(postId);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", postId).orderByAsc("create_time");
        List<Comment> comments = commentMapper.selectList(wrapper);
        for (Comment c : comments) {
            if (post.getType() == 2) { // Community
                User u = userMapper.selectById(c.getUserId());
                if (u != null) {
                    c.setUsername(u.getUsername());
                    c.setUserAvatar(u.getAvatar());
                }
            } else { // TreeHole
                if (c.getPseudonym() != null && !c.getPseudonym().isEmpty()) {
                    c.setUsername(c.getPseudonym() + " (化名)");
                } else {
                    c.setUsername("匿名用户");
                }
                c.setUserAvatar("");
            }
        }
        return comments;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) return;
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人评论");
        }
        commentMapper.deleteById(commentId);
        Post post = getById(comment.getPostId());
        if (post != null) {
            post.setReplyCount(Math.max(0, post.getReplyCount() - 1));
            updateById(post);
        }
    }

    @Override
    @Transactional
    public void deletePostsByUserId(Long userId) {
        // 1. Find all posts by user
        List<Post> posts = list(new QueryWrapper<Post>().eq("user_id", userId));
        
        for (Post post : posts) {
            // 2. Delete comments on these posts
            commentMapper.delete(new QueryWrapper<Comment>().eq("post_id", post.getId()));
            // 3. Delete likes on these posts
            postLikeMapper.delete(new QueryWrapper<PostLike>().eq("post_id", post.getId()));
        }
        
        // 4. Delete the posts themselves
        remove(new QueryWrapper<Post>().eq("user_id", userId));
    }
}
