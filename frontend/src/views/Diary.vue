<template>
  <div class="diary-container">
    <div class="diary-header-section">
      <div class="header-content">
        <h2 class="main-title">心灵足迹</h2>
        <p class="sub-title">记录每一个微小的瞬间，拥抱真实的自己</p>
        <el-button type="primary" class="add-diary-btn" @click="openDialog" round>
          <el-icon class="icon-pulse"><EditPen /></el-icon> 书写心事
        </el-button>
      </div>
      
      <!-- Mood Score Card -->
      <div class="mood-score-card" v-if="moodScore !== null">
        <div class="score-ring">
          <el-progress 
            type="dashboard" 
            :percentage="moodScore" 
            :color="moodScoreColor"
            :width="100"
            :stroke-width="8"
            class="mood-progress"
          >
            <template #default="{ percentage }">
              <span class="score-value">{{ percentage }}</span>
              <span class="score-label">能量值</span>
            </template>
          </el-progress>
        </div>
        <div class="score-tips">
          <p class="tip-title">{{ getScoreTitle(moodScore) }}</p>
          <p class="tip-desc">{{ getScoreDesc(moodScore) }}</p>
        </div>
      </div>
    </div>

    <div class="diary-content-area">
      <el-empty v-if="diaries.length === 0" description="这里还是一片荒原，种下一篇日记吧~" :image-size="200" />
      
      <div v-else class="diary-waterfall">
        <div v-for="(diary, index) in diaries" :key="diary.id" 
             class="diary-card" 
             :style="{ animationDelay: `${index * 0.1}s` }"
             :class="getMoodClass(diary.mood)">
          
          <div class="card-decoration"></div>
          
          <div class="card-top">
            <div class="mood-badge">
              <span class="mood-emoji">{{ getMoodEmoji(diary.mood) }}</span>
              <span class="mood-text">{{ diary.mood }}</span>
            </div>
            <div class="date-badge">
              <span class="day">{{ getDay(diary.createTime) }}</span>
              <span class="month">{{ getMonth(diary.createTime) }}</span>
            </div>
          </div>

          <div class="card-body">
            <h3 class="diary-title">{{ diary.title || '无题' }}</h3>
            <p class="diary-text">{{ truncateContent(diary.content) }}</p>
            <div class="diary-meta">
              <span class="time"><el-icon><Clock /></el-icon> {{ formatTime(diary.createTime) }}</span>
            </div>
          </div>

          <div class="card-actions">
            <el-tooltip content="AI 暖心抱抱" placement="top">
              <el-button circle class="action-btn hug-btn" @click="hugMe(diary)">
                🤗
              </el-button>
            </el-tooltip>
            <div class="right-actions">
              <el-button circle class="action-btn edit-btn" @click="editDiary(diary)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button circle class="action-btn delete-btn" @click="deleteDiary(diary.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination 
        background 
        layout="prev, pager, next" 
        :total="total" 
        :page-size="10"
        @current-change="loadDiaries"
      />
    </div>

    <!-- Edit Dialog -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="form.id ? '重温心事' : '写下此刻'" 
      width="550px"
      class="diary-dialog"
      :close-on-click-modal="false"
      center
    >
      <el-form :model="form" label-position="top" class="diary-form">
        <el-form-item label="今日心情">
          <div class="mood-selector">
            <div 
              v-for="mood in moodOptions" 
              :key="mood.value"
              class="mood-option"
              :class="{ active: form.mood === mood.value }"
              @click="form.mood = mood.value"
            >
              <span class="mood-icon">{{ mood.icon }}</span>
              <span class="mood-name">{{ mood.label }}</span>
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="给今天的心情起个名字..." class="custom-input" />
        </el-form-item>
        
        <el-form-item label="内容">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="8" 
            placeholder="在这里，你可以卸下所有防备..." 
            class="custom-textarea"
            resize="none"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false" round>暂存心底</el-button>
          <el-button type="primary" @click="submitDiary" round class="submit-btn">封存记忆</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Clock, Edit, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const diaries = ref([])
const total = ref(0)
const moodScore = ref(100)
const dialogVisible = ref(false)

const moodOptions = [
  { label: '开心', value: '开心', icon: '😄' },
  { label: '平静', value: '平静', icon: '🍃' },
  { label: '焦虑', value: '焦虑', icon: '😰' },
  { label: '难过', value: '难过', icon: '🌧️' },
  { label: '生气', value: '生气', icon: '🔥' },
  { label: '恐惧', value: '恐惧', icon: '😨' }
]

const moodScoreColor = computed(() => {
  if (moodScore.value >= 80) return '#95d475'
  if (moodScore.value >= 60) return '#79bbff'
  if (moodScore.value >= 40) return '#eebe77'
  return '#f89898'
})

const form = ref({
  id: null,
  title: '',
  content: '',
  mood: '平静'
})

const loadDiaries = async (page = 1) => {
  try {
    const res = await request.get('/diary/list', { params: { page, size: 10 } })
    diaries.value = res.records
    total.value = res.total
    await loadUserInfo()
  } catch (e) {
    console.error(e)
  }
}

const loadUserInfo = async () => {
  try {
    const res = await request.get('/user/info')
    if (res) {
       moodScore.value = res.moodScore !== undefined ? res.moodScore : 100
       userStore.userInfo.moodScore = moodScore.value
    }
  } catch (e) {}
}

const openDialog = () => {
  form.value = { id: null, title: '', content: '', mood: '平静' }
  dialogVisible.value = true
}

const editDiary = (diary) => {
  form.value = { ...diary }
  dialogVisible.value = true
}

const submitDiary = async () => {
  if (!form.value.content) return ElMessage.warning('内容不能为空哦')
  
  try {
    const url = form.value.id ? '/diary/update' : '/diary/add'
    const res = await request.post(url, form.value)
    
    if (url === '/diary/add' && typeof res === 'object') {
       ElMessage.success(res.message || '保存成功')
       if (res.score !== undefined) {
          moodScore.value = res.score
       }
       if (res.alert === 'low_score') {
          ElMessageBox.alert(res.alertMessage, 'AI 关怀提醒', {
             confirmButtonText: '收到了，谢谢',
             type: 'warning',
             center: true,
             iconClass: 'el-icon-warning'
          })
       }
    } else {
       ElMessage.success(typeof res === 'string' ? res : '保存成功')
    }

    dialogVisible.value = false
    loadDiaries()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const deleteDiary = (id) => {
  ElMessageBox.confirm('确定要移除这段记忆吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(async () => {
    try {
      await request.post(`/diary/delete/${id}`)
      ElMessage.success('删除成功')
      loadDiaries()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// Helpers
const getDay = (time) => {
  if (!time) return ''
  return new Date(time).getDate()
}

const getMonth = (time) => {
  if (!time) return ''
  return (new Date(time).getMonth() + 1) + '月'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const getMoodEmoji = (mood) => {
  const opt = moodOptions.find(o => o.value === mood)
  return opt ? opt.icon : '😐'
}

const getMoodClass = (mood) => {
  const map = {
    '开心': 'card-happy',
    '平静': 'card-calm',
    '焦虑': 'card-anxious',
    '难过': 'card-sad',
    '生气': 'card-angry',
    '恐惧': 'card-fear'
  }
  return map[mood] || ''
}

const truncateContent = (content) => {
  if (!content) return ''
  return content.length > 60 ? content.slice(0, 60) + '...' : content
}

const getScoreTitle = (score) => {
  if (score >= 80) return '能量满满'
  if (score >= 60) return '状态不错'
  if (score >= 40) return '有些低落'
  return '需要抱抱'
}

const getScoreDesc = (score) => {
  if (score >= 80) return '继续保持这份阳光！'
  if (score >= 60) return '生活平淡也是一种幸福。'
  if (score >= 40) return '别忘了给自己喘口气的机会。'
  return '没关系，我们一直都在。'
}

// Hug feature
const hugMessages = {
  '开心': [
    '🎉 真的很为你感到高兴！愿这份快乐能一直延续下去~',
    '✨ 你的笑容是今天最美的风景！',
    '🌟 哇！太棒了！记住这个美好的时刻。',
  ],
  '平静': [
    '🍃 平平淡淡才是真。享受这份宁静的时光。',
    '🍵 心如止水，万物静好。',
    '🌿 即使无事发生，也是一种确幸。',
  ],
  '焦虑': [
    '🫂 别担心，深呼吸。你已经做得很好了。',
    '🌬️ 焦虑只是暂时的乌云，阳光终会穿透它。',
    '🐢 不需要急着赶路，按自己的节奏来。',
  ],
  '难过': [
    '🤗 想给你一个大大的拥抱。哭出来也没关系。',
    '🌧️ 允许自己悲伤，眼泪是心灵的排毒。',
    '🌙 黑夜无论怎样悠长，白昼总会到来。',
  ],
  '生气': [
    '😤 生气是正常的，释放出来吧。',
    '🔥 深呼吸，别让愤怒冲昏了头脑。',
    '🌊 想象愤怒是一团火，试着用水去浇灭它。',
  ],
  '恐惧': [
    '🛡️ 别怕，我在呢。勇敢面对它。',
    '💡 试着打开灯，看清它的真面目。',
    '🦁 真正的勇气不是没有恐惧，而是带着恐惧继续前行。',
  ]
}

const hugMe = (diary) => {
  const mood = diary.mood
  const messages = hugMessages[mood] || ['🌟 无论心情如何，都要记得爱自己。']
  const randomMessage = messages[Math.floor(Math.random() * messages.length)]
  
  ElMessageBox.alert(randomMessage, 'AI 暖心回应', {
    confirmButtonText: '谢谢',
    type: 'success',
    center: true,
    roundButton: true,
    customClass: 'hug-message-box'
  })
}

onMounted(() => {
  loadDiaries()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700&display=swap');

.diary-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 90vh;
  font-family: 'Nunito', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  background-color: #fcfcfc;
}

.diary-header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 60px;
  padding: 40px;
  background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
  border-radius: 24px;
  color: #fff;
  box-shadow: 0 10px 30px rgba(142, 197, 252, 0.4);
  position: relative;
  overflow: hidden;
}

.diary-header-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -20%;
  width: 500px;
  height: 500px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.header-content {
  z-index: 1;
}

.main-title {
  font-size: 3rem;
  margin: 0 0 10px;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
}

.sub-title {
  font-size: 1.2rem;
  margin: 0 0 25px;
  opacity: 0.9;
}

.add-diary-btn {
  background: #fff;
  color: #8ec5fc;
  border: none;
  padding: 12px 30px;
  font-size: 1.1rem;
  font-weight: 600;
  transition: transform 0.3s;
}

.add-diary-btn:hover {
  transform: translateY(-2px);
  color: #e0c3fc;
}

.mood-score-card {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px 30px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  z-index: 1;
  color: #333;
}

.score-value {
  font-size: 24px;
  font-weight: 800;
  color: #333;
  display: block;
}

.score-label {
  font-size: 10px;
  color: #999;
}

.score-tips .tip-title {
  font-weight: 700;
  font-size: 1.1rem;
  margin: 0 0 5px;
  color: #333;
}

.score-tips .tip-desc {
  font-size: 0.9rem;
  color: #666;
  margin: 0;
}

/* Diary Grid/Waterfall */
.diary-waterfall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
}

.diary-card {
  background: #fff;
  border-radius: 20px;
  padding: 25px;
  position: relative;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  animation: fadeUp 0.6s ease-out backwards;
}

.diary-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 35px rgba(0,0,0,0.08);
}

/* Card Themes */
.card-happy { background: linear-gradient(to bottom right, #fff, #f0f9eb); border-top: 4px solid #95d475; }
.card-calm { background: linear-gradient(to bottom right, #fff, #ecf5ff); border-top: 4px solid #79bbff; }
.card-anxious { background: linear-gradient(to bottom right, #fff, #fdf6ec); border-top: 4px solid #eebe77; }
.card-sad { background: linear-gradient(to bottom right, #fff, #f4f4f5); border-top: 4px solid #909399; }
.card-angry { background: linear-gradient(to bottom right, #fff, #fef0f0); border-top: 4px solid #f56c6c; }
.card-fear { background: linear-gradient(to bottom right, #fff, #f2f2f2); border-top: 4px solid #333; }

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.mood-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.8);
  padding: 6px 12px;
  border-radius: 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.mood-emoji { font-size: 1.4rem; }
.mood-text { font-size: 0.9rem; font-weight: 600; color: #555; }

.date-badge {
  text-align: center;
  background: #fff;
  padding: 5px 10px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  min-width: 50px;
}

.date-badge .day {
  display: block;
  font-size: 1.2rem;
  font-weight: 800;
  color: #333;
  line-height: 1;
}

.date-badge .month {
  font-size: 0.8rem;
  color: #999;
}

.card-body {
  flex: 1;
}

.diary-title {
  font-size: 1.3rem;
  margin: 0 0 12px;
  color: #2c3e50;
  font-weight: 700;
}

.diary-text {
  color: #666;
  font-size: 1rem;
  line-height: 1.6;
  margin: 0 0 20px;
}

.diary-meta {
  font-size: 0.85rem;
  color: #aaa;
  display: flex;
  align-items: center;
}

.card-actions {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px dashed rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-btn {
  border: none;
  background: transparent;
  font-size: 1.1rem;
  transition: all 0.2s;
}

.hug-btn { font-size: 1.5rem; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.hug-btn:hover { transform: scale(1.2) rotate(10deg); }

.right-actions .action-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.right-actions .delete-btn:hover {
  color: #f56c6c;
}

/* Dialog Styles */
.mood-selector {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.mood-option {
  flex: 1;
  text-align: center;
  padding: 10px 5px;
  border-radius: 12px;
  border: 1px solid #eee;
  cursor: pointer;
  transition: all 0.2s;
}

.mood-option:hover { background: #f9f9f9; }
.mood-option.active {
  background: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.mood-icon { display: block; font-size: 1.8rem; margin-bottom: 5px; }
.mood-name { font-size: 0.9rem; font-weight: 600; }

.custom-input :deep(.el-input__wrapper),
.custom-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 15px;
  box-shadow: none;
  background: #f8f9fa;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper):hover,
.custom-input :deep(.el-input__wrapper).is-focus,
.custom-textarea :deep(.el-textarea__inner):hover,
.custom-textarea :deep(.el-textarea__inner):focus {
  background: #fff;
  border-color: #e0c3fc;
  box-shadow: 0 0 0 1px #e0c3fc inset;
}

.submit-btn {
  padding: 12px 30px;
  background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
  border: none;
  font-weight: 600;
}

.submit-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.icon-pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
