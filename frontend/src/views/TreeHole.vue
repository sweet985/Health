<template>
  <div class="tree-hole-container">
    <div class="header">
      <h2>匿名树洞</h2>
      <p>在这里，卸下伪装，倾诉你的秘密...</p>
    </div>

    <div class="publish-box">
      <div class="publish-header">
        <el-input 
          v-model="pseudonym" 
          placeholder="化名（选填）" 
          size="small"
          class="dark-input-small pseudonym-input"
          maxlength="10"
        />
      </div>
      <el-input 
        v-model="content" 
        type="textarea" 
        placeholder="对着树洞说点什么吧..." 
        :rows="4" 
        resize="none"
        class="dark-input"
      />
      <div class="publish-footer">
        <el-button type="primary" color="#626aef" @click="publish">投递秘密</el-button>
      </div>
    </div>
    
    <div v-for="post in posts" :key="post.id" class="post-card fade-in">
      <div class="post-header">
        <div class="avatar-placeholder">?</div>
        <div class="post-info">
          <span class="username">{{ post.username }}</span>
          <span class="time">{{ formatTime(post.createTime) }}</span>
        </div>
        <el-popconfirm 
          v-if="post.userId === userStore.userInfo.id" 
          title="确定要销毁这条秘密吗？" 
          @confirm="deletePost(post.id)"
        >
          <template #reference>
            <el-button link type="danger" class="delete-btn">销毁</el-button>
          </template>
        </el-popconfirm>
      </div>
      
      <div class="post-content">{{ post.content }}</div>
      
      <div class="post-actions">
        <div class="action-item" @click="like(post)">
          <el-icon><Star /></el-icon>
          <span>{{ post.likeCount || 0 }}</span>
        </div>
        <div class="action-item" @click="toggleComments(post)">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ post.replyCount || 0 }}</span>
        </div>
      </div>
      
      <!-- Comments -->
      <div v-if="post.showComments" class="comments-section">
        <div v-for="comment in post.comments" :key="comment.id" class="comment-item">
          <div class="comment-main">
            <span class="comment-user">{{ comment.username || '匿名用户' }}:</span>
            <span class="comment-text">{{ comment.content }}</span>
          </div>
          <el-button 
            v-if="comment.userId === userStore.userInfo.id" 
            link 
            type="danger" 
            size="small" 
            @click="deleteComment(post, comment.id)"
            class="comment-delete"
          >
            ×
          </el-button>
        </div>
        
        <div class="comment-input-area">
           <el-input 
             v-if="!post.isAuthor"
             v-model="post.commentPseudonym" 
             placeholder="化名" 
             size="small"
             class="dark-input-small comment-pseudonym"
             maxlength="10"
           />
           <el-input 
             v-model="post.newComment" 
             :placeholder="post.isAuthor ? '以发帖人身份回应...' : '温暖回应...'" 
             size="small"
             class="dark-input-small comment-content"
             @keyup.enter="submitComment(post)"
           >
             <template #append>
               <el-button @click="submitComment(post)">发送</el-button>
             </template>
           </el-input>
        </div>
      </div>
    </div>
    
    <el-pagination 
      layout="prev, pager, next" 
      :total="total" 
      @current-change="loadPosts" 
      class="dark-pagination"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Star, ChatDotRound } from '@element-plus/icons-vue'

const content = ref('')
const pseudonym = ref('')
const posts = ref([])
const total = ref(0)
const userStore = useUserStore()

const loadPosts = async (page = 1) => {
  try {
    const res = await request.get('/post/list', { params: { page, size: 10, type: 1 } })
    posts.value = res.records.map(p => {
      // Check if current user is author or has commented to pre-fill pseudonym
      let myPseudonym = ''
      if (p.userId === userStore.userInfo.id) {
         // I am the author
         // We need to know author's pseudonym. 
         // The post object has 'username' which is "Pseudonym (化名)" or "匿名用户"
         // But we don't have the raw pseudonym field in the responseDTO usually (unless we add it)
         // Let's rely on backend logic: if I am author, backend forces my pseudonym.
         // So frontend doesn't need to send it again?
         // Yes, backend says: "if comment.userId == post.userId, comment.setPseudonym(post.getPseudonym())"
         // So we can hide the input or show it disabled with value.
         // But we don't have the raw pseudonym value easily here without parsing or adding field.
         // Let's just hide the input if I am the author.
      }
      
      return { 
        ...p, 
        showComments: false, 
        comments: [], 
        newComment: '',
        commentPseudonym: '',
        isAuthor: p.userId === userStore.userInfo.id
      }
    })
    total.value = res.total
  } catch (e) {
    console.error(e)
  }
}

const publish = async () => {
  if (!content.value.trim()) return ElMessage.warning('秘密不能为空哦')
  try {
    await request.post('/post/create', { type: 1, content: content.value, pseudonym: pseudonym.value })
    ElMessage.success('投递成功')
    content.value = ''
    pseudonym.value = ''
    loadPosts()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '投递失败')
  }
}

const deletePost = async (id) => {
  await request.post(`/post/delete/${id}`)
  ElMessage.success('秘密已销毁')
  loadPosts()
}

const like = async (post) => {
  await request.post(`/post/like/${post.id}`)
  // Reload to get accurate count (since backend toggles)
  // Or smart update: 
  // Since we don't know if it was liked before without `isLiked` field, reload is safer.
  // But for UX, let's assume +1 first, then maybe fix later. 
  // Actually, let's reload the single post or just list. Reloading list is easy.
  loadPosts() 
}

const toggleComments = async (post) => {
  if (!post.showComments) {
    const res = await request.get(`/post/comments/${post.id}`)
    post.comments = res
    post.showComments = true
  } else {
    post.showComments = false
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) return
  try {
    await request.post('/post/comment', { 
      postId: post.id, 
      content: post.newComment,
      pseudonym: post.commentPseudonym 
    })
    post.newComment = ''
    post.commentPseudonym = ''
    // Refresh comments
    const res = await request.get(`/post/comments/${post.id}`)
    post.comments = res
    post.replyCount++
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '评论失败')
  }
}

const deleteComment = async (post, commentId) => {
  try {
    await request.post(`/post/comment/delete/${commentId}`)
    ElMessage.success('已删除')
    // Refresh comments
    const res = await request.get(`/post/comments/${post.id}`)
    post.comments = res
    post.replyCount--
  } catch (e) {
    // Error handled by request interceptor usually
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => loadPosts())
</script>

<style scoped>
.tree-hole-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #1a1a2e; /* Deep dark blue/purple */
  min-height: 100vh;
  color: #e0e0e0;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 1px solid #303050;
}
.header h2 {
  font-size: 2em;
  color: #a29bfe;
  text-shadow: 0 0 10px rgba(162, 155, 254, 0.5);
}
.header p {
  color: #b2bec3;
  font-style: italic;
}

.publish-box {
  background: #16213e;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.3);
  margin-bottom: 30px;
  border: 1px solid #303050;
}

.publish-footer {
  margin-top: 15px;
  text-align: right;
}

.publish-header {
  margin-bottom: 10px;
}

.pseudonym-input {
  width: 150px;
}

.post-card {
  background: #16213e;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.2);
  border: 1px solid #2a2a4a;
  transition: transform 0.2s;
}
.post-card:hover {
  transform: translateY(-2px);
  border-color: #626aef;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}
.avatar-placeholder {
  width: 40px;
  height: 40px;
  background: #303050;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a29bfe;
  font-weight: bold;
  margin-right: 12px;
}
.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.username {
  color: #dfe6e9;
  font-weight: bold;
}
.time {
  font-size: 12px;
  color: #636e72;
}
.delete-btn {
  color: #ff7675;
}

.post-content {
  font-size: 16px;
  line-height: 1.6;
  color: #ecf0f1;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.post-actions {
  display: flex;
  gap: 20px;
  border-top: 1px solid #303050;
  padding-top: 15px;
}
.action-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #b2bec3;
  transition: color 0.2s;
}
.action-item:hover {
  color: #a29bfe;
}

.comments-section {
  margin-top: 20px;
  background: #0f3460;
  padding: 15px;
  border-radius: 8px;
}
.comment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  border-bottom: 1px solid #1a1a2e;
  padding-bottom: 8px;
}
.comment-user {
  color: #74b9ff;
  margin-right: 8px;
}
.comment-text {
  color: #dfe6e9;
}
.comment-delete {
  color: #ff7675;
  opacity: 0.6;
}
.comment-delete:hover {
  opacity: 1;
}

.comment-input-area {
  display: flex;
  gap: 10px;
}

.comment-pseudonym {
  width: 100px;
}

.comment-content {
  flex: 1;
}

/* Override Element UI styles for dark theme */
:deep(.dark-input .el-textarea__inner) {
  background-color: #0f3460;
  border: 1px solid #303050;
  color: #fff;
}
:deep(.dark-input-small .el-input__wrapper) {
  background-color: #1a1a2e;
  box-shadow: none;
  border: 1px solid #303050;
}
:deep(.dark-input-small .el-input__inner) {
  color: #fff;
}
:deep(.dark-pagination .el-pagination__total),
:deep(.dark-pagination .el-pager li) {
  color: #b2bec3;
}
:deep(.dark-pagination .el-pager li.is-active) {
  color: #a29bfe;
}
:deep(.dark-pagination .btn-prev),
:deep(.dark-pagination .btn-next) {
  background: transparent;
  color: #b2bec3;
}

.fade-in {
  animation: fadeIn 0.5s ease-in;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
