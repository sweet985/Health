<template>
  <div class="assessment-container">
    <div class="header">
      <h1>心理测评中心</h1>
      <p>科学探索内心，发现更好的自己</p>
    </div>

    <div class="test-grid">
      <div v-for="test in tests" :key="test.id" class="test-card" @click="startTest(test)">
        <div class="card-icon">{{ test.icon }}</div>
        <div class="card-content">
          <h3>{{ test.name }}</h3>
          <p class="desc">{{ test.description }}</p>
          <div class="meta">
            <span class="tag">{{ test.time }}分钟</span>
            <span class="tag">{{ test.questions }}题</span>
          </div>
        </div>
        <el-button type="primary" plain round class="start-btn">开始测评</el-button>
      </div>
    </div>

    <!-- Test Modal -->
    <el-dialog 
      v-model="dialogVisible" 
      fullscreen 
      :show-close="false"
      class="test-dialog"
    >
      <!-- Question View -->
      <div class="test-wrapper" v-if="!showResult && currentTest">
        <!-- Header -->
        <div class="test-header">
          <el-button circle icon="Back" @click="closeTest" class="back-btn"></el-button>
          <h2>{{ currentTest.name }}</h2>
          <div class="header-right">
             <el-switch
               v-model="autoMode"
               active-text="自动模式"
               inactive-text="手动模式"
               style="margin-right: 20px;"
             />
             <div class="progress-info">
               {{ currentStep + 1 }} / {{ currentTest.questionsList.length }}
             </div>
          </div>
        </div>

        <!-- Question -->
        <div class="question-container fade-in" :key="currentStep">
          <h3 class="question-text">{{ currentTest.questionsList[currentStep].text }}</h3>
          
          <div class="options-list">
            <div 
              v-for="(option, index) in currentTest.questionsList[currentStep].options" 
              :key="index"
              class="option-item"
              :class="{ 'selected': answers[currentStep] === option.value }"
              @click="selectOption(option.value)"
            >
              <span class="opt-label">{{ String.fromCharCode(65 + index) }}</span>
              <span class="opt-text">{{ option.text }}</span>
            </div>
          </div>
        </div>

        <!-- Controls -->
        <div class="test-controls">
          <el-button 
            @click="prevQuestion" 
            :disabled="currentStep === 0"
            icon="ArrowLeft"
            round
          >
            上一题
          </el-button>
          
          <el-button 
            @click="pauseTest" 
            type="warning" 
            plain 
            round 
            icon="VideoPause"
          >
            暂停测评
          </el-button>

          <el-button 
            v-if="currentStep < currentTest.questionsList.length - 1"
            @click="nextQuestion" 
            :disabled="answers[currentStep] === undefined"
            type="primary" 
            round
          >
            下一题 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
          
          <el-button 
            v-else
            @click="calculateResult" 
            :disabled="answers[currentStep] === undefined"
            type="success" 
            round
            icon="Check"
          >
            提交测评
          </el-button>
        </div>

        <!-- Question Navigator -->
        <div class="question-navigator">
          <div 
            v-for="(_, index) in currentTest.questionsList" 
            :key="index"
            class="nav-ball"
            :class="{ 
              'active': index === currentStep,
              'answered': answers[index] !== undefined
            }"
            @click="jumpToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
      </div>

      <!-- Result View -->
      <div class="result-wrapper fade-in" v-if="showResult">
        <div class="result-card">
          <div class="score-circle">
            <span class="score">{{ resultScore }}</span>
            <span class="label">分</span>
          </div>
          <h3>{{ resultTitle }}</h3>
          <p class="result-desc">{{ resultDesc }}</p>
          
          <div class="formal-report-wrapper" v-show="aiReport">
            <div class="report-header">
              <h2>心愈空间 · 专属测评报告</h2>
              <div class="report-meta">
                <span><strong>测评项目：</strong>{{ currentTest?.name }}</span>
                <span><strong>测评得分：</strong>{{ resultScore }} 分</span>
                <span><strong>生成日期：</strong>{{ new Date().toLocaleDateString() }}</span>
              </div>
            </div>
            
            <div ref="chartRef" class="chart-container" v-show="showChart"></div>
            
            <div class="report-content markdown-body" v-html="aiReport"></div>
            
            <div class="report-footer">
              <p>※ 声明：本报告由 AI 深度分析生成，辅助您了解当前心理状态，不作为专业医疗诊断依据。如需确诊，请前往正规医疗机构。</p>
            </div>
          </div>
          
          <el-button type="primary" round size="large" @click="closeTest" class="close-btn">返回测评中心</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import { Back, ArrowLeft, ArrowRight, VideoPause, Check, DataAnalysis } from '@element-plus/icons-vue'
import { scl90, sds, sas, pss, les, psqi, epq } from '../data/assessments'
import request from '../utils/request'
import MarkdownIt from 'markdown-it'
import * as echarts from 'echarts'

const md = new MarkdownIt({ breaks: true })
const chartRef = ref(null)
const showChart = ref(false)

// Test Data Definitions
const tests = [
  {
    id: 'scl90',
    name: 'SCL-90 症状自评量表',
    icon: '🩺',
    description: '世界上最著名的心理健康测试量表之一，从感觉、情感、思维、意识、行为等9个方面全面评估心理健康状况。',
    time: '15-20',
    questions: scl90.length,
    questionsList: scl90
  },
  {
    id: 'sds',
    name: 'SDS 抑郁自评量表',
    icon: '🌧️',
    description: '衡量抑郁状态轻重程度的首选量表，帮助您了解自己的情绪低落程度。',
    time: '5-10',
    questions: sds.length,
    questionsList: sds
  },
  {
    id: 'sas',
    name: 'SAS 焦虑自评量表',
    icon: '😰',
    description: '用于评定焦虑症状的轻重程度及其在治疗中的变化，适用于具有焦虑症状的成年人。',
    time: '5-10',
    questions: sas.length,
    questionsList: sas
  },
  {
    id: 'pss',
    name: 'PSS 感知压力量表',
    icon: '🤯',
    description: '评估您在过去一个月中感觉生活不可控、不可预测或超负荷的程度。',
    time: '3-5',
    questions: pss.length,
    questionsList: pss
  },
  {
    id: 'les',
    name: 'LES 生活事件量表',
    icon: '📅',
    description: '评估生活中的重大变动（如工作变动、家庭变故等）对您造成的心理压力。',
    time: '10-15',
    questions: les.length,
    questionsList: les
  },
  {
    id: 'psqi',
    name: 'PSQI 匹兹堡睡眠质量指数',
    icon: '😴',
    description: '综合评估睡眠质量，包括入睡时间、睡眠时长、睡眠效率、睡眠障碍等。',
    time: '5-8',
    questions: psqi.length,
    questionsList: psqi
  },
  {
    id: 'epq',
    name: 'EPQ 艾森克人格问卷',
    icon: '🧠',
    description: '测定人格维度，包括内外向、神经质、精神质等，帮助您了解自己的性格特质。',
    time: '10-15',
    questions: epq.length,
    questionsList: epq
  }
]

const dialogVisible = ref(false)
const currentTest = ref(null)
const currentStep = ref(0)
const answers = ref([])
const showResult = ref(false)
const resultScore = ref(0)
const autoMode = ref(false)
const aiReport = ref('')

const startTest = (test) => {
  currentTest.value = test
  
  // Check for saved progress
  const saved = localStorage.getItem(`assessment_${test.id}`)
  if (saved) {
    ElMessageBox.confirm('检测到您有未完成的测评记录，是否继续？', '提示', {
      confirmButtonText: '继续测评',
      cancelButtonText: '重新开始',
      type: 'info'
    }).then(() => {
      const data = JSON.parse(saved)
      currentStep.value = data.step
      // Ensure answers is an array and sparse array holes are preserved as undefined/empty
      // JSON.parse converts undefined to null, so we need to map back to undefined
      answers.value = (data.answers || []).map(v => v === null ? undefined : v)
      autoMode.value = data.autoMode || false
      dialogVisible.value = true
    }).catch(() => {
      // Clear saved data
      localStorage.removeItem(`assessment_${test.id}`)
      initNewTest()
    })
  } else {
    initNewTest()
  }
}

const initNewTest = () => {
  currentStep.value = 0
  answers.value = new Array(currentTest.value.questionsList.length).fill(undefined)
  showResult.value = false
  autoMode.value = false
  aiReport.value = ''
  dialogVisible.value = true
}

const selectOption = (val) => {
  answers.value[currentStep.value] = val
  
  if (autoMode.value) {
    if (currentStep.value < currentTest.value.questionsList.length - 1) {
      setTimeout(() => {
        currentStep.value++
      }, 300)
    }
  }
}

const nextQuestion = () => {
  if (currentStep.value < currentTest.value.questionsList.length - 1) {
    currentStep.value++
  }
}

const prevQuestion = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const jumpToQuestion = (index) => {
  currentStep.value = index
}

const pauseTest = () => {
  if (!currentTest.value) return
  const data = {
    step: currentStep.value,
    answers: answers.value,
    autoMode: autoMode.value,
    timestamp: new Date().getTime()
  }
  localStorage.setItem(`assessment_${currentTest.value.id}`, JSON.stringify(data))
  ElMessage.success('进度已保存，下次进入可继续测评')
  dialogVisible.value = false
  currentTest.value = null
}

const calculateResult = () => {
  // Check if all questions are answered
  if (answers.value.some(a => a === undefined)) {
    ElMessage.warning('请回答完所有问题后再提交')
    return
  }

  // Simple sum for demo purposes (real logic is complex)
  const total = answers.value.reduce((a, b) => a + (b || 0), 0)
  resultScore.value = total
  
  // Show result view immediately
  showResult.value = true
  
  // Clear saved progress on finish
  localStorage.removeItem(`assessment_${currentTest.value.id}`)
  
  // Wait for next tick so DOM is updated (result view is visible) before generating report and chart
  nextTick(() => {
    generateAIReport(currentTest.value.id, total, answers.value)
  })
}

const generateAIReport = async (testId, score, answersArray) => {
  aiReport.value = ''
  const loading = ElLoading.service({
    lock: true,
    text: 'AI 专家正在为您生成深度诊断报告，这可能需要几十秒钟，请耐心等待...',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  
  try {
    const testDetails = currentTest.value.questionsList.map((q, index) => {
      const selectedValue = answersArray[index]
      const selectedOption = q.options.find(opt => opt.value === selectedValue)
      return {
        question: q.text,
        score: selectedValue,
        answer: selectedOption ? selectedOption.text : '未答'
      }
    })

    const requestData = {
      testName: currentTest.value.name,
      score: score,
      answersJson: JSON.stringify(testDetails, null, 2)
    }

    // Call the original non-streaming endpoint
    const res = await request.post('/ai/assessment-report', requestData)
    
    if (res) {
      // Create chart first, wait for DOM to update with v-show=true
      showChart.value = true
      nextTick(() => {
        renderChart(testDetails)
      })

      // Render Markdown to HTML FIRST, THEN wrap the generated HTML in blocks.
      // This prevents markdown-it from escaping the div tags.
      let renderedHtml = md.render(res)
      
      // Wrap everything between <h3> tags in a report-block div
      let processedHtml = renderedHtml.replace(/(<h3.*?>.*?<\/h3>[\s\S]*?)(?=<h3|$)/gi, '<div class="report-block">\n$1\n</div>')
      
      aiReport.value = processedHtml

      // Save assessment record to backend
      try {
        await request.post('/assessment/record/save', {
          score: score, // 使用传入的 score，而不是未定义的 totalScore.value
          level: resultTitle.value, // 使用计算属性的值作为 level
          reportContent: res // Save original markdown
        })
      } catch (saveErr) {
        console.error('Failed to save assessment record', saveErr)
      }
      
    } else {
      aiReport.value = '<div class="report-block">生成报告失败，请稍后重试。</div>'
    }

  } catch (error) {
    console.error('AI Report generation failed:', error)
    aiReport.value = '<div class="report-block">AI 报告服务暂时不可用，请联系管理员。</div>'
  } finally {
    loading.close()
  }
}

const renderChart = (testDetails) => {
  if (!chartRef.value) return
  const myChart = echarts.init(chartRef.value)
  
  // Get top 6 highest scored items
  const sortedDetails = [...testDetails].sort((a, b) => b.score - a.score).slice(0, 6)
  
  const option = {
    title: {
      text: '显著因子得分分析 (Top 6)',
      left: 'center',
      textStyle: { color: '#334155', fontSize: 16 }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function (params) {
        // Show full question text in tooltip
        const dataIndex = params[0].dataIndex;
        const fullQuestion = sortedDetails[sortedDetails.length - 1 - dataIndex].question;
        const score = params[0].value;
        return `${fullQuestion}<br/>得分: <strong>${score}</strong> 分`;
      }
    },
    grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
    xAxis: { 
      type: 'value', 
      max: (value) => value.max + 1,
      splitLine: { show: true, lineStyle: { type: 'dashed' } }
    },
    yAxis: { 
      type: 'category', 
      data: sortedDetails.map(item => item.question.length > 15 ? item.question.substring(0, 15) + '...' : item.question).reverse(),
      axisLabel: { 
        interval: 0, 
        width: 180, // Increased width to show more text
        overflow: 'truncate' 
      }
    },
    series: [
      {
        name: '分值',
        type: 'bar',
        data: sortedDetails.map(item => item.score).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#38bdf8' },
            { offset: 1, color: '#0284c7' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        label: { 
          show: true, 
          position: 'right', 
          color: '#0284c7', 
          fontWeight: 'bold',
          formatter: '{c} 分'
        }
      }
    ]
  }
  myChart.setOption(option)
}

const resultTitle = computed(() => {
  if (resultScore.value < 30) return '状态良好'
  if (resultScore.value < 60) return '轻度困扰'
  return '建议咨询'
})

const resultDesc = computed(() => {
  if (resultScore.value < 30) return '您的心理状态非常健康，请继续保持！'
  if (resultScore.value < 60) return '您可能面临一些轻微的压力或困扰，建议适当放松，多进行自我调节。'
  return '测试结果显示您的相关指标偏高，这可能只是暂时的，也可能需要专业帮助。建议您寻求心理咨询师的支持。'
})

const closeTest = () => {
  if (!showResult.value) {
    ElMessageBox.confirm('测试尚未完成，确定要退出吗？', '提示', {
      type: 'warning'
    }).then(() => {
      dialogVisible.value = false
      currentTest.value = null
    }).catch(() => {})
  } else {
    dialogVisible.value = false
    currentTest.value = null
  }
}
</script>

<style scoped>
.assessment-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.header {
  text-align: center;
  margin-bottom: 50px;
}

.header h1 {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 10px;
}

.header p {
  color: #666;
  font-size: 1.1rem;
}

.test-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
}

.test-card {
  background: #fff;
  border-radius: 20px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  position: relative;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.test-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0,0,0,0.1);
  border-color: #e0e0e0;
}

.card-icon {
  font-size: 40px;
  margin-bottom: 20px;
  background: #f9f9f9;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-content h3 {
  font-size: 1.2rem;
  color: #333;
  margin-bottom: 10px;
}

.desc {
  color: #666;
  font-size: 0.9rem;
  line-height: 1.6;
  margin-bottom: 20px;
  flex: 1;
}

.meta {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tag {
  background: #f0f2f5;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.start-btn {
  width: 100%;
}

/* Test Dialog Styles */
:deep(.test-dialog) {
  display: flex;
  flex-direction: column;
  margin: 0 !important;
  height: 100vh !important; /* Force full viewport height */
  max-height: 100vh !important;
}

:deep(.test-dialog .el-dialog__header) {
  display: none;
}

:deep(.test-dialog .el-dialog__body) {
  padding: 0;
  flex: 1;
  background: #f5f7fa;
  overflow-y: auto;
  position: relative;
  max-height: none !important; /* Remove max-height constraint */
}

.test-wrapper {
  width: 100%;
  max-width: 800px;
  min-height: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  padding: 40px 20px;
  box-sizing: border-box;
}

.test-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 60px;
}

.back-btn {
  font-size: 20px;
}

.test-header h2 {
  font-size: 1.5rem;
  color: #333;
}

.progress-info {
  font-size: 1.2rem;
  color: #409eff;
  font-weight: bold;
}

.header-right {
  display: flex;
  align-items: center;
}

.question-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  max-width: 700px;
  margin: 0 auto;
  width: 100%;
}

.question-text {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 50px;
  text-align: center;
  line-height: 1.4;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.option-item {
  background: #fff;
  padding: 20px 30px;
  border-radius: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  border: 2px solid transparent;
}

.option-item:hover {
  border-color: #409eff;
  transform: translateX(10px);
}

.opt-label {
  width: 36px;
  height: 36px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-weight: bold;
  font-size: 16px;
}

.opt-text {
  font-size: 1.1rem;
  color: #333;
}

.test-controls {
  margin-top: 40px;
  margin-bottom: 40px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.question-navigator {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  padding: 20px 0;
  border-top: 1px dashed #e0e0e0;
}

.nav-ball {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s;
  background: #fff;
}

.nav-ball:hover {
  border-color: #409eff;
  color: #409eff;
}

.nav-ball.active {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
  transform: scale(1.1);
  box-shadow: 0 2px 10px rgba(64, 158, 255, 0.3);
}

.nav-ball.answered {
  background: #f0f9eb;
  border-color: #e1f3d8;
  color: #67c23a;
}

/* If active AND answered, active style takes precedence or mix? 
   Let's make active override answered for clarity of current position.
   CSS order matters. */
.nav-ball.answered.active {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.selected {
  border-color: #409eff;
  background-color: #ecf5ff;
}

/* Result View */
.result-wrapper {
  width: 100%;
  min-height: 100%;
  background: #f5f7fa;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 40px 20px;
  box-sizing: border-box;
}

.result-card {
  text-align: center;
  padding: 40px 0;
  max-width: 800px;
  width: 100%;
  background: transparent;
}

.score-circle {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: 8px solid #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 30px;
  flex-direction: column;
}

.score {
  font-size: 3rem;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
}

.label {
  font-size: 1rem;
  color: #999;
}

.result-desc {
  color: #666;
  margin: 20px 0 40px;
  line-height: 1.6;
}

@import 'github-markdown-css/github-markdown-light.css';

/* Fix el-dialog body scrolling */
:deep(.el-dialog__body) {
  max-height: 75vh;
  overflow-y: auto;
  padding: 20px 30px;
}

/* Formal Report Styles */
.formal-report-wrapper {
  margin-top: 40px;
  background: transparent;
  text-align: left;
}

.report-header {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  margin-bottom: 25px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  text-align: center;
}

.report-header h2 {
  color: #0f172a;
  font-size: 2rem;
  letter-spacing: 2px;
  margin-bottom: 20px;
  font-weight: 700;
}

.report-meta {
  display: flex;
  justify-content: space-around;
  color: #475569;
  font-size: 1rem;
  background: #f1f5f9;
  padding: 15px 20px;
  border-radius: 8px;
}

.chart-container {
  width: 100%;
  height: 350px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 25px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.report-content {
  background: transparent !important;
}

/* Report Blocks (generated via regex) */
:deep(.report-block) {
  background: #fff;
  border-radius: 12px;
  padding: 30px 40px;
  margin-bottom: 25px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.report-footer {
  margin-top: 20px;
  padding: 20px;
  color: #94a3b8;
  font-size: 0.85rem;
  text-align: center;
}

.close-btn {
  margin-top: 30px;
}

/* Customizing Markdown output */
:deep(.markdown-body) {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif;
  color: #1e293b; /* Darker text for better contrast */
}

:deep(.markdown-body h3) {
  color: #0284c7; /* Deeper blue for section headers */
  font-size: 1.4rem;
  margin-top: 40px; /* Larger gap between sections */
  margin-bottom: 25px;
  border-bottom: 2px solid #bae6fd; /* Thicker border */
  padding-bottom: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
}

:deep(.markdown-body p) {
  margin-bottom: 20px; /* More space between paragraphs */
  text-align: justify;
  line-height: 1.9; /* More breathable line height */
  color: #334155;
  font-size: 1.05rem; /* Slightly larger text */
}

:deep(.markdown-body ul), :deep(.markdown-body ol) {
  margin-bottom: 25px;
  padding-left: 30px;
  background: #f8fafc; /* Add subtle background to lists for grouping */
  padding-top: 15px;
  padding-bottom: 15px;
  padding-right: 20px;
  border-radius: 8px;
  border-left: 4px solid #38bdf8; /* Left border accent */
}

:deep(.markdown-body li) {
  margin-bottom: 12px;
  line-height: 1.8;
  color: #334155;
  font-size: 1.05rem;
}

:deep(.markdown-body strong) {
  color: #0f172a;
  font-weight: 600;
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
