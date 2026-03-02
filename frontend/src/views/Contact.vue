<template>
  <div class="contact-container">
    <div class="contact-hero">
      <div class="hero-content">
        <h1>联系我们</h1>
        <p>无论您有任何疑问或建议，我们都随时倾听</p>
      </div>
      <div class="hero-waves">
        <svg class="waves" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 24 150 28" preserveAspectRatio="none" shape-rendering="auto">
          <defs>
            <path id="gentle-wave" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z" />
          </defs>
          <g class="parallax">
            <use xlink:href="#gentle-wave" x="48" y="0" fill="rgba(255,255,255,0.7" />
            <use xlink:href="#gentle-wave" x="48" y="3" fill="rgba(255,255,255,0.5)" />
            <use xlink:href="#gentle-wave" x="48" y="5" fill="rgba(255,255,255,0.3)" />
            <use xlink:href="#gentle-wave" x="48" y="7" fill="#fff" />
          </g>
        </svg>
      </div>
    </div>

    <div class="contact-content">
      <div class="contact-grid">
        <div class="contact-info-cards">
          <div class="info-card fade-in-up">
            <div class="icon-wrapper email-icon">
              <el-icon><Message /></el-icon>
            </div>
            <h3>电子邮件</h3>
            <p>support@healthmind.com</p>
            <p>bd@healthmind.com</p>
          </div>
          
          <div class="info-card fade-in-up delay-1">
            <div class="icon-wrapper phone-icon">
              <el-icon><Phone /></el-icon>
            </div>
            <h3>联系电话</h3>
            <p>+86 123 4567 8900</p>
            <p>周一至周五 9:00 - 18:00</p>
          </div>
          
          <div class="info-card fade-in-up delay-2">
            <div class="icon-wrapper loc-icon">
              <el-icon><Location /></el-icon>
            </div>
            <h3>总部地址</h3>
            <p>上海市朝阳区科技园区</p>
            <p>创新大厦 A 座 808 室</p>
          </div>
        </div>

        <div class="feedback-section fade-in-up delay-3">
          <div class="feedback-paper">
            <div class="paper-header">
              <h2>发送留言</h2>
              <div class="stamp">
                <el-icon><Stamp /></el-icon>
              </div>
            </div>
            <el-form :model="form" class="feedback-form">
              <el-form-item>
                <el-input v-model="form.name" placeholder="您的称呼" />
              </el-form-item>
              <el-form-item>
                <el-input v-model="form.email" placeholder="联系邮箱" />
              </el-form-item>
              <el-form-item>
                <el-input 
                  v-model="form.message" 
                  type="textarea" 
                  :rows="6" 
                  placeholder="请输入您的建议或反馈..." 
                  class="message-area"
                />
              </el-form-item>
              <div class="form-footer">
                <el-button type="primary" class="send-btn" @click="submitFeedback">
                  发送信件 <el-icon class="el-icon--right"><Position /></el-icon>
                </el-button>
              </div>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Message, Phone, Location, Stamp, Position } from '@element-plus/icons-vue'

const form = ref({
  name: '',
  email: '',
  message: ''
})

const submitFeedback = () => {
  if (!form.value.message) {
    return ElMessage.warning('请输入留言内容')
  }
  
  // Simulate sending
  setTimeout(() => {
    ElMessage.success('留言已发送，我们会尽快联系您！')
    form.value = { name: '', email: '', message: '' }
  }, 1000)
}
</script>

<style scoped>
.contact-container {
  min-height: calc(100vh - 60px);
  background: #f9fbfd;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}

/* Hero Section */
.contact-hero {
  height: 350px;
  background: linear-gradient(120deg, #89f7fe 0%, #66a6ff 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #fff;
  margin-bottom: 60px;
}

.hero-content {
  z-index: 2;
  transform: translateY(-20px);
}

.hero-content h1 {
  font-size: 3rem;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.hero-content p {
  font-size: 1.2rem;
  opacity: 0.9;
}

.hero-waves {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100px;
  overflow: hidden;
}

.waves {
  width: 100%;
  height: 100px;
}

.parallax > use {
  animation: move-forever 25s cubic-bezier(.55,.5,.45,.5) infinite;
}
.parallax > use:nth-child(1) { animation-delay: -2s; animation-duration: 7s; }
.parallax > use:nth-child(2) { animation-delay: -3s; animation-duration: 10s; }
.parallax > use:nth-child(3) { animation-delay: -4s; animation-duration: 13s; }
.parallax > use:nth-child(4) { animation-delay: -5s; animation-duration: 20s; }

@keyframes move-forever {
  0% { transform: translate3d(-90px,0,0); }
  100% { transform: translate3d(85px,0,0); }
}

/* Content */
.contact-content {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 20px 60px;
}

.contact-grid {
  display: flex;
  flex-direction: column;
  gap: 60px;
}

/* Info Cards */
.contact-info-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  margin-top: -100px; /* Overlap hero */
  z-index: 10;
  position: relative;
}

.info-card {
  background: #fff;
  padding: 40px 20px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.info-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}

.icon-wrapper {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin: 0 auto 20px;
  color: #fff;
}

.email-icon { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); }
.phone-icon { background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%); }
.loc-icon { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); }

.info-card h3 {
  margin-bottom: 15px;
  color: #333;
}

.info-card p {
  color: #666;
  line-height: 1.6;
}

/* Feedback Form - Letter Style */
.feedback-section {
  display: flex;
  justify-content: center;
}

.feedback-paper {
  background: #fff;
  width: 100%;
  max-width: 700px;
  padding: 50px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3), 0 0 40px rgba(0, 0, 0, 0.1) inset;
  position: relative;
  background-image: linear-gradient(#f1f1f1 1px, transparent 1px);
  background-size: 100% 2em; /* Lines */
  line-height: 2em;
}

.feedback-paper::before, .feedback-paper::after {
  content: "";
  position: absolute;
  z-index: -1;
  bottom: 15px;
  left: 10px;
  width: 50%;
  height: 20%;
  max-width: 300px;
  box-shadow: 0 15px 10px rgba(0, 0, 0, 0.7);
  transform: rotate(-3deg);
}

.feedback-paper::after {
  right: 10px;
  left: auto;
  transform: rotate(3deg);
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  border-bottom: 2px solid #333;
  padding-bottom: 10px;
}

.paper-header h2 {
  font-family: 'Georgia', serif;
  font-size: 2rem;
  color: #333;
}

.stamp {
  font-size: 40px;
  color: #d32f2f;
  transform: rotate(-15deg);
  opacity: 0.8;
  border: 3px solid #d32f2f;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.feedback-form .el-input__wrapper),
:deep(.feedback-form .el-textarea__inner) {
  background: transparent;
  box-shadow: none;
  border: none;
  border-bottom: 1px dashed #999;
  border-radius: 0;
  padding-left: 0;
  font-family: 'Georgia', serif;
  font-size: 1.1rem;
}

:deep(.feedback-form .el-input__wrapper.is-focus),
:deep(.feedback-form .el-textarea__inner:focus) {
  box-shadow: none;
  border-bottom: 1px solid #333;
}

.form-footer {
  text-align: right;
  margin-top: 30px;
}

.send-btn {
  padding: 12px 30px;
  font-size: 1rem;
  border-radius: 30px;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

/* Animations */
.fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.8s ease-out forwards;
}

.delay-1 { animation-delay: 0.2s; }
.delay-2 { animation-delay: 0.4s; }
.delay-3 { animation-delay: 0.6s; }

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
