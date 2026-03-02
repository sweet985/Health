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
      <div class="test-wrapper" v-if="currentTest">
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
          <div class="ai-report" v-if="aiReport">
            <h4><el-icon><DataAnalysis /></el-icon> AI 智能分析报告</h4>
            <div class="report-content">{{ aiReport }}</div>
          </div>
          <el-button type="primary" round size="large" @click="closeTest">返回测评中心</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import { Back, ArrowLeft, ArrowRight, VideoPause, Check, DataAnalysis } from '@element-plus/icons-vue'
import { scl90, sds, sas, pss, les, psqi, epq } from '../data/assessments'

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
  // Simple sum for demo purposes (real logic is complex)
  const total = answers.value.reduce((a, b) => a + (b || 0), 0)
  resultScore.value = total
  
  // Generate AI Report
  generateAIReport(currentTest.value.id, total, answers.value)
  
  showResult.value = true
  // Clear saved progress on finish
  localStorage.removeItem(`assessment_${currentTest.value.id}`)
}

const generateAIReport = (testId, score, answers) => {
  // Simulate AI generation delay
  const loading = ElLoading.service({
    lock: true,
    text: 'AI 正在分析您的测评数据...',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  
  setTimeout(() => {
    loading.close()
    
    // Dynamic report generation logic based on test type and score
    let report = ''
    
    if (testId === 'sds') {
      // SDS Standard Score = Raw Score * 1.25
      const indexScore = Math.floor(score * 1.25)
      let level = ''
      let advice = ''
      
      if (indexScore < 53) {
        level = '正常'
        advice = '您的情绪状态非常健康，像晴朗的天空一样明媚。您拥有强大的心理韧性，能够积极应对生活中的挑战。请继续保持这种乐观的生活态度，多参与社交活动，分享您的快乐。'
      } else if (indexScore < 63) {
        level = '轻度抑郁'
        advice = '您最近似乎有些情绪低落，就像天空偶尔飘过的乌云。这可能是对近期压力的一种正常反应。建议您多晒晒太阳，进行适量的运动（如慢跑、瑜伽），并尝试记录“感恩日记”，每天写下三件值得开心的小事。'
      } else if (indexScore < 73) {
        level = '中度抑郁'
        advice = '您的内心世界可能正在经历一场连绵的阴雨，感到疲惫和无助。这并不是软弱的表现，而是心灵在呼救。建议您调整生活节奏，给自己一段“假期”，同时强烈建议咨询心理咨询师，他们能为您提供专业的支持和疏导。'
      } else {
        level = '重度抑郁'
        advice = '您目前正处于情绪的深渊中，感到极度的痛苦和绝望。请记住，这只是暂时的黑暗，您并不孤单。请务必寻求专业精神科医生或心理治疗师的帮助，药物治疗和心理治疗结合往往能带来转机。请哪怕是为了爱您的人，也要勇敢迈出求助的一步。'
      }
      report = `【SDS 抑郁自评结果】\n标准分：${indexScore}分\n评级：${level}\n\n【AI 深度分析与建议】\n${advice}`
      
    } else if (testId === 'sas') {
      // SAS Standard Score = Raw Score * 1.25
      const indexScore = Math.floor(score * 1.25)
      let level = ''
      let advice = ''
      
      if (indexScore < 50) {
        level = '正常'
        advice = '您的心态平和稳定，焦虑水平处于非常健康的范围内。您能够从容不迫地处理日常事务，这种松弛感是幸福生活的重要基石。'
      } else if (indexScore < 60) {
        level = '轻度焦虑'
        advice = '您可能感到些许紧张和不安，就像绷紧的琴弦。建议尝试“4-7-8呼吸法”（吸气4秒，憋气7秒，呼气8秒）来快速放松神经。合理规划时间，减少不必要的担忧，专注于当下的行动。'
      } else if (indexScore < 70) {
        level = '中度焦虑'
        advice = '焦虑似乎已经开始干扰您的生活，让您感到坐立难安。这种持续的警觉状态会消耗大量能量。建议您练习正念冥想，学习接纳而非对抗焦虑情绪。如果感到难以自控，寻求专业咨询会是非常明智的选择。'
      } else {
        level = '重度焦虑'
        advice = '您的焦虑水平较高，可能伴随有心慌、出汗等躯体症状，这让您感到非常痛苦。请不要独自硬扛，专业的医疗干预（如抗焦虑药物或认知行为疗法）能有效缓解这些症状。请尽快咨询专业医生。'
      }
      report = `【SAS 焦虑自评结果】\n标准分：${indexScore}分\n评级：${level}\n\n【AI 深度分析与建议】\n${advice}`

    } else if (testId === 'scl90') {
      let level = ''
      let advice = ''
      if (score < 160) {
        level = '心理健康状况良好'
        advice = '综合各项指标，您的心理健康水平处于理想状态。您在身心各方面都表现出了良好的适应性。继续保持健康的生活方式，定期进行身心放松。'
      } else if (score < 200) {
        level = '轻度心理问题倾向'
        advice = '您可能在某些方面（如躯体不适、人际敏感等）存在轻微的困扰。这通常与近期的生活事件或压力有关。建议您关注那些得分较高的具体症状，有针对性地进行自我调节。'
      } else if (score < 250) {
        level = '中度心理问题'
        advice = '您的心理健康状况可能亮起了黄灯，某些症状已经明显影响到了您的生活质量。建议您进行更深入的心理评估，并考虑寻求心理咨询的帮助，以防问题进一步发展。'
      } else {
        level = '较重心理问题'
        advice = '您的SCL-90总分较高，提示您可能正面临较为严重的心理困扰。这可能涉及多个方面，如情绪、思维或人际关系。强烈建议您前往正规医院的心理科或精神科进行专业诊断和治疗。'
      }
      report = `【SCL-90 综合评估结果】\n总分：${score}分\n评级：${level}\n\n【AI 深度分析与建议】\n${advice}\n(注：SCL-90是一个多维度量表，总分仅供参考，具体症状需结合各因子分分析。)`

    } else if (testId === 'pss') {
      // PSS-14 range 0-56
      let level = ''
      let advice = ''
      if (score < 28) {
        level = '压力适中'
        advice = '您目前的压力水平在可控范围内，您具备良好的抗压能力和应对技巧。适当的压力可以转化为动力，助您更好地完成任务。'
      } else {
        level = '压力过大'
        advice = '您目前感知到的压力较大，可能会觉得生活有些失控，感到力不从心。长期的压力会损害身心健康。建议您学会做减法，拒绝不必要的负担，并保证充足的睡眠和休息。'
      }
      report = `【PSS 感知压力评估】\n得分：${score}分\n评级：${level}\n\n【AI 深度分析与建议】\n${advice}`

    } else if (testId === 'psqi') {
      // PSQI range 0-21 (simplified logic here)
      // Assuming our simplified calculation sums up options roughly
      let level = ''
      let advice = ''
      if (score < 7) {
        level = '睡眠质量很好'
        advice = '恭喜您，您的睡眠质量很高！良好的睡眠是身心健康的基石。请继续保持规律的作息习惯。'
      } else if (score < 14) {
        level = '睡眠质量一般'
        advice = '您的睡眠存在一些小问题，如入睡稍慢或偶尔易醒。建议睡前一小时远离电子屏幕，泡个热水澡或喝杯热牛奶助眠。'
      } else {
        level = '睡眠质量较差'
        advice = '您可能存在较为严重的睡眠障碍。长期睡眠不足会影响情绪和免疫力。建议您建立严格的睡眠仪式，必要时咨询睡眠专科医生。'
      }
      report = `【PSQI 睡眠质量评估】\n得分：${score}分\n评级：${level}\n\n【AI 深度分析与建议】\n${advice}`

    } else if (testId === 'les') {
       // LES Higher is more stress impact
       let level = ''
       if (score < 20) level = '生活事件影响较小'
       else if (score < 50) level = '生活事件影响中等'
       else level = '生活事件影响显著'
       
       report = `【LES 生活事件评估】\n得分：${score}分\n评级：${level}\n\n【AI 深度分析与建议】\n生活中的变故（无论是积极的还是消极的）都会带来心理压力。测评显示近期生活事件对您的影响为${level}。请注意，即使是结婚、升职等喜事也需要心理适应期。建议您多与亲友沟通，寻求社会支持，平稳度过适应期。`

    } else if (testId === 'epq') {
        // General personality comment
        report = `【EPQ 人格特质分析】\n\n【AI 深度分析与建议】\nEPQ问卷主要评估您的内外向(E)、神经质(N)等维度。由于这只是一个简版或单维度的分数汇总，无法精确生成完整的性格画像。但从您的答题倾向来看，您可能拥有独特的性格魅力。性格无好坏之分，了解自己的性格特质有助于您选择更适合的工作和生活方式，扬长避短。`
    } else {
        report = `基于您在${currentTest.value.name}中的表现（得分：${score}），AI分析认为您的状态${score < (currentTest.value.questions * 2) ? '良好且稳定' : '需要一定的关注和调整'}。每个人的心理状态都是动态变化的，此次测评仅反映当下的情况。建议您关注自己的内心需求，适当休息和放松。`
    }
    
    aiReport.value = report
  }, 1500)
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
:deep(.test-dialog .el-dialog__header) {
  display: none;
}

:deep(.test-dialog .el-dialog__body) {
  padding: 0;
  height: 100%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.test-wrapper {
  width: 100%;
  max-width: 800px;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 40px 20px;
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
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.result-card {
  text-align: center;
  padding: 40px;
  max-width: 500px;
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

.ai-report {
  margin-top: 30px;
  background: #f0f9ff;
  border-radius: 12px;
  padding: 20px;
  text-align: left;
  border: 1px solid #dbeafe;
}

.ai-report h4 {
  color: #0284c7;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 1.1rem;
}

.report-content {
  color: #334155;
  line-height: 1.8;
  font-size: 0.95rem;
  text-align: justify;
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
