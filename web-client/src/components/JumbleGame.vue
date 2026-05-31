<template>
  <div ref="gameContainerRef" class="relative w-full max-w-3xl mx-auto">
    <div
      class="p-5 md:p-8 transition-all duration-300"
      :class="{ 'animate-shake': shakeCard }"
    >
      <div class="flex flex-col gap-5 md:flex-row md:items-start md:justify-between mb-6 pb-5 border-b border-stone-200">
        <div class="max-w-lg">
          <p class="text-xs font-semibold uppercase tracking-[0.24em] text-amber-700 mb-2">Word practice</p>
          <h1 class="text-3xl md:text-4xl font-black text-stone-900 tracking-tight">
            Jumble game
          </h1>
          <p class="text-sm text-stone-600 mt-2 leading-relaxed">
            LIM RUI XUAN work
          </p>
        </div>
        <div class="flex items-center gap-3 self-start md:self-auto">
          <div class="rounded-[0.65rem] bg-stone-100 border border-stone-200 px-4 py-3 min-w-[130px] shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]">
            <span class="text-[11px] text-stone-500 uppercase tracking-[0.18em] block font-bold">High score</span>
            <span class="text-2xl font-black text-stone-900">{{ highScore }} pts</span>
          </div>
          <button 
            v-if="game || showGameOver"
            type="button" 
            class="flex items-center gap-1.5 px-4 py-3 rounded-[0.65rem] bg-[#3f2c1c] hover:bg-[#2f2014] text-sm font-bold text-white transition-all duration-200 shadow-sm border border-[#3f2c1c]"
            @click="playClickSound(); resetToMenu()"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z" />
            </svg>
            Home
          </button>
        </div>
      </div>

      <div v-if="error" class="alert alert-danger bg-red-50 border-red-200 text-red-700 text-sm rounded-2xl mb-6 flex items-start gap-3" role="alert">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 shrink-0 mt-0.5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
        </svg>
        <div>
          <strong class="font-bold">Connection error</strong>
          <span class="block mt-1 text-xs text-red-600">{{ error }}</span>
        </div>
      </div>

      <div v-if="!game && !showGameOver" class="text-center py-6">
        <p class="text-stone-600 text-sm mb-6 max-w-md mx-auto leading-relaxed">
          Start with a mode that feels right for you.
        </p>

        <div class="mb-8">
          <span class="text-xs text-stone-500 font-bold uppercase tracking-[0.2em] block mb-4">{{ difficultyPrompt }}</span>
          <div class="grid gap-3 md:grid-cols-3 text-left">
            <button 
              type="button" 
              class="difficulty-card"
              data-points="1"
              :class="difficulty === 'easy' ? 'difficulty-card-active difficulty-card-easy' : 'difficulty-card-idle'"
              @click="setDifficulty('easy')"
            >
              <span class="block text-base font-bold mb-1">Easy</span>
              <span class="block text-xs font-medium opacity-80">Unlimited wrong guesses.</span>
            </button>
            <button 
              type="button" 
              class="difficulty-card"
              data-points="2"
              :class="difficulty === 'medium' ? 'difficulty-card-active difficulty-card-medium' : 'difficulty-card-idle'"
              @click="setDifficulty('medium')"
            >
              <span class="block text-base font-bold mb-1">Medium</span>
              <span class="block text-xs font-medium opacity-80">Up to 8 wrong guesses.</span>
            </button>
            <button 
              type="button" 
              class="difficulty-card"
              data-points="3"
              :class="difficulty === 'hard' ? 'difficulty-card-active difficulty-card-hard' : 'difficulty-card-idle'"
              @click="setDifficulty('hard')"
            >
              <span class="block text-base font-bold mb-1">Hard</span>
              <span class="block text-xs font-medium opacity-80">5 wrong guesses and a timer.</span>
            </button>
          </div>
        </div>

        <button 
          class="btn start-game-button w-full max-w-xs py-3 rounded-2xl font-bold bg-stone-900 text-white border-0 hover:bg-stone-800 shadow-lg transition-all duration-200"
          :disabled="loading"
          @click="startNewGame"
        >
          <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status"></span>
          <span class="button-label-stack" v-if="!loading">
            <span class="button-label button-label-primary">Start game</span>
            <span class="button-label button-label-hover">Let's go</span>
          </span>
          <span v-else>Start game</span>
        </button>
      </div>

      <div v-else-if="game && !showGameOver">
        <div class="mb-5 rounded-2xl bg-amber-50 border border-amber-200 px-4 py-3 text-sm text-amber-900">
          <span class="font-bold">Current mode:</span> {{ difficultyLabel }}.
          <span class="text-amber-800"> Keep typing words made only from the letters shown below.</span>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 mb-6 bg-stone-50 border border-stone-200 p-3 rounded-2xl text-center">
          <div>
            <span class="text-[10px] text-stone-500 uppercase tracking-[0.18em] block font-bold">Score</span>
            <span ref="scoreValueRef" class="text-lg font-extrabold text-stone-900">{{ score }}</span>
          </div>
          <div>
            <span class="text-[10px] text-stone-500 uppercase tracking-[0.18em] block font-bold">Found</span>
            <span class="text-lg font-extrabold text-stone-900">{{ game.guessedWords.length }} / {{ game.totalWords }}</span>
          </div>
          <div>
            <span class="text-[10px] text-stone-500 uppercase tracking-[0.18em] block font-bold">Mistakes left</span>
            <span class="text-lg font-extrabold" :class="remainingGuessesClass">
              {{ difficulty === 'easy' ? '∞' : incorrectGuessesLimit - incorrectGuesses }}
            </span>
          </div>
        </div>

        <div v-if="difficulty === 'hard'" class="mb-6">
          <div class="flex justify-between items-center mb-1.5 text-xs text-stone-500 font-semibold">
            <span>Timer</span>
            <span :class="timeLeft <= 10 ? 'text-red-500 animate-pulse' : 'text-stone-700'">{{ timeLeft }}s left</span>
          </div>
          <div class="w-full bg-stone-200 rounded-full h-2 overflow-hidden border border-stone-200">
            <div 
              class="h-full rounded-full transition-all duration-1000"
              :class="timeLeft <= 10 ? 'bg-red-500' : 'bg-amber-500'"
              :style="{ width: (timeLeft / 30) * 100 + '%' }"
            ></div>
          </div>
        </div>

        <div class="text-center py-6 mb-6 bg-stone-50 rounded-2xl border border-stone-200">
          <span class="text-xs text-stone-500 font-bold uppercase tracking-[0.2em] block mb-2">Scrambled letters</span>
          <div class="text-3xl font-extrabold tracking-widest text-stone-900 uppercase">
            {{ scrambleAsDisplay }}
          </div>
        </div>

        <form @submit.prevent="submitGuess" class="mb-6">
          <div class="mb-3">
            <label for="guessInput" class="form-label text-xs text-stone-500 font-bold uppercase tracking-[0.2em] mb-2 block">
              Your guess
            </label>
            <div class="flex flex-col sm:flex-row gap-2">
              <input 
                type="text" 
                id="guessInput"
                class="form-control bg-white border border-stone-300 text-stone-900 rounded-2xl px-4 py-3 focus:border-amber-400 focus:ring focus:ring-amber-200 text-center font-semibold tracking-wider placeholder:text-stone-400"
                v-model="guess"
                placeholder="Type a word"
                autocomplete="off"
                ref="guessInputField"
                :disabled="loading"
              />
              <button 
                type="submit" 
                class="btn px-5 rounded-2xl font-bold bg-stone-900 text-white border-0 hover:bg-stone-800 transition-all duration-200 shrink-0"
                :disabled="loading"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-1" role="status"></span>
                Guess
              </button>
            </div>
            <div v-if="validationError || serverFeedback" class="mt-2 text-xs font-semibold">
              <span v-if="validationError" class="text-red-500">{{ validationError }}</span>
              <span v-else-if="serverFeedback" :class="feedbackClass">{{ serverFeedback }}</span>
            </div>
          </div>
        </form>

        <div class="mb-6">
          <div class="w-full bg-stone-200 rounded-full h-1.5 overflow-hidden border border-stone-200">
            <div 
              class="bg-gradient-to-r from-amber-500 to-orange-500 h-full rounded-full transition-all duration-300"
              :style="{ width: progressPercentage + '%' }"
            ></div>
          </div>
        </div>

        <div>
          <span class="text-xs text-stone-500 font-bold uppercase tracking-[0.2em] block mb-3">Words you found</span>
          <div v-if="game.guessedWords.length === 0" class="text-center py-6 text-sm text-stone-500 border border-dashed border-stone-300 rounded-2xl">
            No words yet. Your first correct guess will show up here.
          </div>
          <div v-else class="flex flex-wrap gap-2 max-h-36 overflow-y-auto p-1">
            <span 
              v-for="word in game.guessedWords" 
              :key="word"
              class="px-3 py-1 bg-stone-100 border border-stone-200 rounded-full text-xs font-bold text-stone-700 shadow-sm"
            >
              {{ word }}
            </span>
          </div>
        </div>
      </div>

      <div v-else-if="showGameOver" class="text-center py-8">
        <div v-if="hasWon" class="w-20 h-20 mx-auto bg-green-50 text-green-600 rounded-full flex items-center justify-center border border-green-200 mb-6 animate-success-bounce">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <div v-else class="w-20 h-20 mx-auto bg-red-50 text-red-500 rounded-full flex items-center justify-center border border-red-200 mb-6">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>

        <h2 class="text-3xl font-extrabold mb-2" :class="hasWon ? 'text-green-600' : 'text-red-500'">
          {{ hasWon ? 'Nice work' : 'Round finished' }}
        </h2>
        <p class="text-stone-600 text-sm mb-6 max-w-xs mx-auto">
          {{ hasWon ? 'You found every valid sub-word in this round.' : gameOverReasonMessage }}
        </p>

        <div class="grid grid-cols-2 gap-3 max-w-sm mx-auto mb-8 bg-stone-50 p-4 border border-stone-200 rounded-2xl">
          <div>
            <span class="text-xs text-stone-500 uppercase tracking-[0.18em] block font-bold mb-1">Final score</span>
            <span class="text-2xl font-extrabold text-stone-900">{{ score }} pts</span>
          </div>
          <div>
            <span class="text-xs text-stone-500 uppercase tracking-[0.18em] block font-bold mb-1">Words found</span>
            <span class="text-2xl font-extrabold text-stone-900">{{ game ? game.guessedWords.length : 0 }} / {{ game ? game.totalWords : 0 }}</span>
          </div>
        </div>

        <div v-if="game && !hasWon" class="mb-8">
          <span class="text-xs text-stone-500 font-bold uppercase tracking-[0.2em] block mb-2">Base word</span>
          <div class="text-2xl font-black text-stone-900 tracking-widest uppercase">
            {{ game.originalWord }}
          </div>
        </div>

        <div class="flex flex-col gap-2.5 items-center max-w-xs mx-auto">
          <button 
            type="button" 
            class="btn w-full py-3 rounded-2xl font-bold bg-stone-900 text-white border-0 hover:bg-stone-800 shadow-lg transition-all duration-200"
            @click="playClickSound(); startNewGame()"
          >
            Play again
          </button>
          <button 
            type="button" 
            class="btn w-full py-3 rounded-2xl font-bold bg-[#3f2c1c] text-white hover:bg-[#2f2014] transition-all duration-200 border-0"
            @click="playClickSound(); resetToMenu()"
          >
            Home
          </button>
        </div>
      </div>
    </div>

    <!-- Score Drops overlay container -->
    <div class="absolute inset-0 pointer-events-none overflow-hidden z-50">
      <div
        v-for="drop in activeDrops"
        :key="drop.id"
        class="absolute font-black text-red-600 text-2xl filter drop-shadow-[0_4px_6px_rgba(220,38,38,0.25)] animate-gravity-drop"
        :style="{ left: drop.left + 'px', top: drop.top + 'px', '--x-drift': drop.xDrift + 'px' }"
        @animationend="removeDrop(drop.id)"
      >
        {{ drop.text }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onUnmounted } from 'vue';
import buttonClickedSound from './sound/button_clicked.mp3';
import correctAnswerSound from './sound/correct_answer.mp3';
import wrongAnswerSound from './sound/wrong_answer.mp3';

// Define the API Base URL dynamically
const API_BASE = import.meta.env.VITE_API_URL || '';

const emit = defineEmits(['game-active']);

// State variables
const game = ref(null);
const loading = ref(false);
const error = ref(null);
const guess = ref('');
const validationError = ref(null);
const serverFeedback = ref(null);
const serverFeedbackType = ref(''); // 'success', 'warning', 'error'
const difficulty = ref('easy');

const score = ref(0);
const highScore = ref(parseInt(localStorage.getItem('jumble_high_score') || '0', 10));

const incorrectGuesses = ref(0);
const incorrectGuessesLimit = ref(null);

const timeLeft = ref(30);
let timerInterval = null;

const showGameOver = ref(false);
const shakeCard = ref(false);
const successBounce = ref(false);

const guessInputField = ref(null);

// Drops and Container Refs
const activeDrops = ref([]);
const gameContainerRef = ref(null);
const scoreValueRef = ref(null);

// Computeds
const scrambleAsDisplay = computed(() => {
  if (!game.value || !game.value.scrambleWord) return '';
  return game.value.scrambleWord.split('').join(' ');
});

const progressPercentage = computed(() => {
  if (!game.value || game.value.totalWords === 0) return 0;
  return (game.value.guessedWords.length / game.value.totalWords) * 100;
});

const hasWon = computed(() => game.value && game.value.remainingWords === 0);

const difficultyLabel = computed(() => {
  if (difficulty.value === 'easy') return 'Easy';
  if (difficulty.value === 'medium') return 'Medium';
  return 'Hard';
});

const difficultyPrompt = computed(() => `Choose ${difficultyLabel.value}`);

const gameOverReasonMessage = computed(() => {
  if (difficulty.value === 'hard' && timeLeft.value <= 0) {
    return 'Time ran out before the next correct answer.';
  }
  return 'You used all available mistakes for this round.';
});

const feedbackClass = computed(() => {
  if (serverFeedbackType.value === 'success') return 'text-green-600';
  if (serverFeedbackType.value === 'warning') return 'text-amber-600';
  return 'text-red-500';
});

const remainingGuessesClass = computed(() => {
  if (difficulty.value === 'easy') return 'text-stone-500';
  const left = incorrectGuessesLimit.value - incorrectGuesses.value;
  if (left <= 2) return 'text-red-500';
  if (left <= 4) return 'text-amber-600';
  return 'text-green-600';
});

// Methods
const playClickSound = () => {
  try {
    if (typeof Audio !== 'undefined') {
      const audio = new Audio(buttonClickedSound);
      audio.play()?.catch(() => {});
    }
  } catch (e) {}
};

const playCorrectSound = () => {
  try {
    if (typeof Audio !== 'undefined') {
      const audio = new Audio(correctAnswerSound);
      audio.play()?.catch(() => {});
    }
  } catch (e) {}
};

const playWrongSound = () => {
  try {
    if (typeof Audio !== 'undefined') {
      const audio = new Audio(wrongAnswerSound);
      audio.play()?.catch(() => {});
    }
  } catch (e) {}
};

const triggerScoreDrop = (amountText) => {
  if (!scoreValueRef.value || !gameContainerRef.value) return;
  const scoreRect = scoreValueRef.value.getBoundingClientRect();
  const containerRect = gameContainerRef.value.getBoundingClientRect();
  
  // Center relative coordinates
  const left = scoreRect.left - containerRect.left + (scoreRect.width / 2) - 10;
  const top = scoreRect.top - containerRect.top;
  
  // Randomly drift left, right, or drop straight
  const choices = ['left', 'right', 'straight'];
  const choice = choices[Math.floor(Math.random() * choices.length)];
  let xDrift = 0;
  if (choice === 'left') {
    xDrift = -40 - Math.random() * 40; // -40px to -80px
  } else if (choice === 'right') {
    xDrift = 40 + Math.random() * 40;  // 40px to 80px
  }
  
  const id = Date.now() + Math.random();
  activeDrops.value.push({ id, text: amountText, left, top, xDrift });
};

const removeDrop = (id) => {
  activeDrops.value = activeDrops.value.filter((d) => d.id !== id);
};

const setDifficulty = (level) => {
  difficulty.value = level;
  playClickSound();
};

const triggerShake = () => {
  shakeCard.value = true;
  setTimeout(() => {
    shakeCard.value = false;
  }, 400);
};

const startNewGame = async () => {
  loading.value = true;
  error.value = null;
  serverFeedback.value = null;
  validationError.value = null;
  guess.value = '';
  incorrectGuesses.value = 0;
  showGameOver.value = false;
  score.value = 0;

  // Set limits based on difficulty
  if (difficulty.value === 'easy') {
    incorrectGuessesLimit.value = null;
  } else if (difficulty.value === 'medium') {
    incorrectGuessesLimit.value = 8;
  } else if (difficulty.value === 'hard') {
    incorrectGuessesLimit.value = 5;
    timeLeft.value = 30;
  }

  try {
    const res = await fetch(`${API_BASE}/api/game/new`);
    if (!res.ok) {
      throw new Error(`Server returned HTTP ${res.status}`);
    }
    const data = await res.json();
    game.value = {
      id: data.id,
      result: data.result,
      originalWord: data.original_word,
      scrambleWord: data.scramble_word,
      totalWords: data.total_words,
      remainingWords: data.remaining_words,
      guessedWords: data.guessed_words || []
    };
    emit('game-active', true);

    if (difficulty.value === 'hard') {
      startTimer();
    }

    nextTick(() => {
      focusInput();
    });
  } catch (err) {
    error.value = err.message || 'Failed to establish connection to game backend API.';
  } finally {
    loading.value = false;
  }
};

const startTimer = () => {
  stopTimer();
  timeLeft.value = 30;
  timerInterval = setInterval(() => {
    timeLeft.value -= 1;
    if (timeLeft.value <= 0) {
      stopTimer();
      triggerGameOver();
    }
  }, 1000);
};

const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
};

const focusInput = () => {
  if (guessInputField.value) {
    guessInputField.value.focus();
  }
};

const submitGuess = async () => {
  validationError.value = null;
  serverFeedback.value = null;

  if (!guess.value || guess.value.trim().length === 0) {
    validationError.value = 'Please type a word first.';
    triggerShake();
    return;
  }

  const cleanedGuess = guess.value.trim().toLowerCase();
  
  // Client-side validations
  if (cleanedGuess.length < 3) {
    validationError.value = 'Guesses must be at least 3 characters long.';
    triggerShake();
    return;
  }
  
  if (!/^[a-z]+$/.test(cleanedGuess)) {
    validationError.value = 'Guesses must contain letters only.';
    triggerShake();
    return;
  }

  loading.value = true;
  try {
    const res = await fetch(`${API_BASE}/api/game/guess`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        id: game.value.id,
        word: cleanedGuess
      })
    });

    if (!res.ok) {
      throw new Error(`Server returned HTTP ${res.status}`);
    }

    const data = await res.json();
    
    // Process response
    const outcome = data.result;
    
    if (outcome === 'Guessed correctly.') {
      game.value.guessedWords = data.guessed_words;
      game.value.remainingWords = data.remaining_words;
      
      // Points addition + speed bonus
      let pointsGained = 10;
      if (difficulty.value === 'hard') {
        pointsGained += Math.floor(timeLeft.value / 3); // speed bonus points
        startTimer(); // reset timer for next guess
      }
      score.value += pointsGained;
      updateHighScore();
      playCorrectSound();

      serverFeedbackType.value = 'success';
      serverFeedback.value = `Correct! +${pointsGained} points.`;
      guess.value = '';
    } else if (outcome === 'All words guessed.') {
      game.value.guessedWords = data.guessed_words;
      game.value.remainingWords = 0;
      score.value += 30; // win bonus
      updateHighScore();
      stopTimer();
      showGameOver.value = true;
      playCorrectSound();
    } else if (outcome === 'Guessed already.') {
      score.value = Math.max(0, score.value - 2); // deduct points
      triggerScoreDrop('-2');
      playWrongSound();
      serverFeedbackType.value = 'warning';
      serverFeedback.value = 'Already found this word! -2 points.';
      triggerShake();
    } else if (outcome === 'Guessed incorrectly.') {
      score.value = Math.max(0, score.value - 2); // deduct points
      triggerScoreDrop('-2');
      playWrongSound();
      serverFeedbackType.value = 'error';
      serverFeedback.value = 'Guessed incorrectly. -2 points.';
      
      if (difficulty.value !== 'easy') {
        incorrectGuesses.value += 1;
        if (incorrectGuesses.value >= incorrectGuessesLimit.value) {
          stopTimer();
          triggerGameOver();
        }
      }
      triggerShake();
    }

    nextTick(() => {
      focusInput();
    });
  } catch (err) {
    error.value = 'Connection lost during guess submission: ' + err.message;
  } finally {
    loading.value = false;
  }
};

const updateHighScore = () => {
  if (score.value > highScore.value) {
    highScore.value = score.value;
    localStorage.setItem('jumble_high_score', highScore.value.toString());
  }
};

const triggerGameOver = () => {
  showGameOver.value = true;
};

const resetToMenu = () => {
  game.value = null;
  showGameOver.value = false;
  incorrectGuesses.value = 0;
  score.value = 0;
  error.value = null;
  stopTimer();
  emit('game-active', false);
};

onUnmounted(() => {
  stopTimer();
});
</script>

<style scoped>
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-6px); }
  40%, 80% { transform: translateX(6px); }
}
.animate-shake {
  animation: shake 0.35s ease-in-out;
}

@keyframes bounce-in {
  0% { transform: scale(0.3); opacity: 0; }
  50% { transform: scale(1.05); }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); opacity: 1; }
}
.animate-success-bounce {
  animation: bounce-in 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.difficulty-card {
  position: relative;
  overflow: hidden;
  border-radius: 0.45rem;
  border: 1px solid #d6b37c;
  padding: 1rem 1rem 0.95rem;
  text-align: center;
  color: #3f2c1c;
  background: linear-gradient(180deg, #f7e2b6 0%, #edc989 100%);
  box-shadow:
    inset 0 1px 0 rgba(255, 250, 239, 0.92),
    inset 0 -2px 0 rgba(181, 126, 44, 0.28),
    0 4px 0 #be8b43,
    0 14px 24px rgba(120, 88, 42, 0.16);
  transition:
    transform 220ms ease,
    box-shadow 220ms ease,
    border-color 220ms ease,
    background 220ms ease;
}

.difficulty-card::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(255, 248, 233, 0.78), rgba(255, 248, 233, 0) 44%),
    linear-gradient(90deg, rgba(255, 255, 255, 0.28), rgba(255, 255, 255, 0) 24%, rgba(114, 73, 22, 0.1) 100%);
  pointer-events: none;
}

.difficulty-card::after {
  content: attr(data-points);
  position: absolute;
  right: 0.65rem;
  bottom: 0.45rem;
  font-size: 0.78rem;
  font-weight: 800;
  color: rgba(92, 54, 14, 0.6);
  pointer-events: none;
}

.difficulty-card:hover {
  transform: translateY(-2px);
  box-shadow:
    inset 0 1px 0 rgba(255, 250, 239, 0.95),
    inset 0 -2px 0 rgba(181, 126, 44, 0.32),
    0 6px 0 #be8b43,
    0 18px 28px rgba(120, 88, 42, 0.18);
}

.difficulty-card-idle:hover {
  border-color: #bf8f4d;
}

.difficulty-card-active {
  color: #2f1b0b;
  transform: translateY(4px);
  box-shadow:
    inset 0 2px 6px rgba(122, 68, 7, 0.18),
    inset 0 1px 0 rgba(255, 247, 227, 0.8),
    0 1px 0 #9e6c2b,
    0 8px 16px rgba(120, 88, 42, 0.16);
}

.difficulty-card-easy {
  border-color: #7cb86f;
  background: linear-gradient(180deg, #dff1bf 0%, #b9dc7c 100%);
}

.difficulty-card-medium {
  border-color: #d5a54a;
  background: linear-gradient(180deg, #f5df93 0%, #ebbf62 100%);
}

.difficulty-card-hard {
  border-color: #d88561;
  background: linear-gradient(180deg, #f3c0a3 0%, #df946f 100%);
}

.start-game-button {
  position: relative;
  overflow: hidden;
}

.button-label-stack {
  position: relative;
  display: inline-grid;
  place-items: center;
  min-width: 7.5rem;
  min-height: 1.6rem;
}

.button-label {
  grid-area: 1 / 1;
  transition:
    transform 260ms ease,
    filter 260ms ease,
    opacity 260ms ease;
}

.button-label-primary {
  opacity: 1;
  filter: blur(0);
  transform: translateY(0) scale(1);
}

.button-label-hover {
  opacity: 0;
  filter: blur(8px);
  transform: translateY(12px) scale(0.95);
}

.start-game-button:hover .button-label-primary {
  opacity: 0;
  filter: blur(8px);
  transform: translateY(-12px) scale(1.04);
}

.start-game-button:hover .button-label-hover {
  opacity: 1;
  filter: blur(0);
  transform: translateY(0) scale(1);
}

@media (max-width: 699px) {
  .difficulty-card {
    padding: 0.92rem 0.85rem 0.88rem;
  }

  .difficulty-card::after {
    right: 0.5rem;
    bottom: 0.35rem;
    font-size: 0.72rem;
  }

  .start-game-button {
    max-width: 100%;
  }
}

@keyframes gravity-drop {
  0% {
    transform: translate(0, 0) scale(1.2);
    opacity: 1;
  }
  30% {
    opacity: 1;
    transform: translate(calc(var(--x-drift) * 0.25), 10px) scale(1);
  }
  100% {
    transform: translate(var(--x-drift), 650px) scale(0.6);
    opacity: 0;
  }
}

.animate-gravity-drop {
  animation: gravity-drop 1.4s cubic-bezier(0.6, 0.04, 0.98, 0.335) forwards;
}
</style>
