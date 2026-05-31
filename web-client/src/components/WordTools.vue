<template>
  <section class="word-tools">
    <div class="word-tools-header">
      <div class="max-w-2xl">
        <p class="text-xs font-semibold uppercase tracking-[0.24em] text-amber-700 mb-2">Word tools</p>
        <h2 class="text-3xl md:text-4xl font-black text-stone-900 tracking-tight">
          Practice without the scramble
        </h2>
        <p class="text-sm text-stone-600 mt-3 leading-relaxed">
          Explore the dictionary with focused tools for checking words, narrowing patterns, and finding smaller words inside a bigger one.
        </p>
      </div>
      <div class="word-tools-badge">
        <span class="text-[11px] text-stone-500 uppercase tracking-[0.18em] block font-bold">Live tools</span>
        <span class="text-2xl font-black text-stone-900">{{ toolCards.length }}</span>
      </div>
    </div>

    <div class="word-tools-layout">
      <aside class="tool-rail" aria-label="Word tool choices">
        <button
          v-for="tool in toolCards"
          :key="tool.id"
          type="button"
          class="tool-rail-button"
          :class="{ 'tool-rail-button-active': activeTool === tool.id, 'tool-rail-button-idle': activeTool !== tool.id }"
          @click="selectTool(tool.id)"
        >
          <span class="tool-rail-label">{{ tool.label }}</span>
          <span class="tool-rail-copy">{{ tool.short }}</span>
        </button>
      </aside>

      <div class="tool-panel">
        <div class="tool-panel-intro">
          <p class="text-xs font-bold uppercase tracking-[0.22em] text-stone-500">{{ currentTool.kicker }}</p>
          <h3 class="text-2xl font-black text-stone-900 mt-2">{{ currentTool.title }}</h3>
          <p class="text-sm text-stone-600 mt-3 leading-relaxed">{{ currentTool.description }}</p>
        </div>

        <form class="tool-form" @submit.prevent="submitActiveTool">
          <div v-if="activeTool === 'exists'" class="tool-fields">
            <label class="tool-label" for="exists-word">Word to check</label>
            <input id="exists-word" v-model="existsForm.word" class="tool-input" type="text" placeholder="example: tomato" />
          </div>

          <div v-else-if="activeTool === 'prefix'" class="tool-fields">
            <label class="tool-label" for="prefix-word">Prefix</label>
            <input id="prefix-word" v-model="prefixForm.prefix" class="tool-input" type="text" placeholder="example: pen" />
          </div>

          <div v-else-if="activeTool === 'suffix'" class="tool-fields">
            <label class="tool-label" for="suffix-word">Suffix</label>
            <input id="suffix-word" v-model="suffixForm.suffix" class="tool-input" type="text" placeholder="example: ing" />
          </div>

          <div v-else-if="activeTool === 'subwords'" class="tool-fields tool-fields-grid">
            <div>
              <label class="tool-label" for="subwords-word">Base word</label>
              <input id="subwords-word" v-model="subwordsForm.word" class="tool-input" type="text" placeholder="example: yellow" />
            </div>
            <div>
              <label class="tool-label" for="subwords-min-length">Minimum length</label>
              <input id="subwords-min-length" v-model.number="subwordsForm.minLength" class="tool-input" type="number" min="1" max="20" />
            </div>
          </div>

          <div v-else class="tool-fields tool-fields-grid tool-fields-grid-search">
            <div>
              <label class="tool-label" for="search-start">Starts with</label>
              <input id="search-start" v-model="searchForm.start" class="tool-input" type="text" maxlength="1" placeholder="a" />
            </div>
            <div>
              <label class="tool-label" for="search-end">Ends with</label>
              <input id="search-end" v-model="searchForm.end" class="tool-input" type="text" maxlength="1" placeholder="e" />
            </div>
            <div>
              <label class="tool-label" for="search-length">Length</label>
              <input id="search-length" v-model.number="searchForm.length" class="tool-input" type="number" min="1" max="24" placeholder="5" />
            </div>
          </div>

          <div class="tool-actions">
            <button type="submit" class="tool-submit" :disabled="loading">
              {{ loading ? 'Working...' : currentTool.action }}
            </button>
            <button type="button" class="tool-clear" :disabled="loading" @click="resetActiveTool">
              Reset
            </button>
          </div>
        </form>

        <p v-if="error" class="tool-feedback tool-feedback-error">{{ error }}</p>

        <div class="tool-results">
          <div v-if="activeTool === 'exists'" class="result-summary">
            <p class="text-xs font-bold uppercase tracking-[0.2em] text-stone-500">Result</p>
            <p v-if="existsResult" class="result-callout" :class="existsResult.exists ? 'result-callout-success' : 'result-callout-muted'">
              <span class="font-bold">{{ existsResult.word || 'This word' }}</span>
              {{ existsResult.exists ? ' exists in the dictionary.' : ' was not found in the dictionary.' }}
            </p>
            <p v-else class="result-empty">Check a word to see whether it exists in the dictionary.</p>
          </div>

          <div v-else class="result-summary">
            <div class="result-summary-bar">
              <div>
                <p class="text-xs font-bold uppercase tracking-[0.2em] text-stone-500">Matches</p>
                <p class="text-2xl font-black text-stone-900">{{ listResult.count }}</p>
              </div>
              <p class="result-hint">{{ currentTool.resultHint }}</p>
            </div>

            <div v-if="listResult.words.length > 0" class="result-word-grid">
              <span v-for="word in visibleWords" :key="word" class="result-chip">{{ word }}</span>
            </div>
            <p v-else class="result-empty">{{ currentTool.empty }}</p>

            <p v-if="listResult.words.length > visibleLimit" class="result-tail">
              Showing {{ visibleLimit }} of {{ listResult.words.length }} words.
            </p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue';
import buttonClickedSound from './sound/button_clicked.mp3';

// Define the API Base URL dynamically
const API_BASE = import.meta.env.VITE_API_URL || '';

const toolCards = [
  {
    id: 'exists',
    label: 'Exists',
    short: 'Check whether a word is in the dictionary.',
    kicker: 'Dictionary check',
    title: 'Word existence checker',
    description: 'Quickly verify whether a word is recognized by the app dictionary.',
    action: 'Check word',
    resultHint: 'Single yes or no result.',
    empty: 'No lookup yet.'
  },
  {
    id: 'prefix',
    label: 'Prefix',
    short: 'Find words that begin with the same letters.',
    kicker: 'Pattern finder',
    title: 'Prefix match',
    description: 'Useful for brainstorming completions and building vocabulary families.',
    action: 'Find prefix words',
    resultHint: 'Best for 2 or more letters.',
    empty: 'No prefix matches yet.'
  },
  {
    id: 'suffix',
    label: 'Suffix',
    short: 'Find words that end with the same letters.',
    kicker: 'Ending finder',
    title: 'Suffix match',
    description: 'Great for spotting common endings like -ing, -ed, or -tion.',
    action: 'Find suffix words',
    resultHint: 'Try endings like ing, ed, or ly.',
    empty: 'No suffix matches yet.'
  },
  {
    id: 'subwords',
    label: 'Subwords',
    short: 'Generate smaller valid words from a larger word.',
    kicker: 'Word builder',
    title: 'Subword finder',
    description: 'See what valid smaller words can be built from the letters of a base word.',
    action: 'Generate subwords',
    resultHint: 'Uses real dictionary words only.',
    empty: 'No subwords generated yet.'
  },
  {
    id: 'search',
    label: 'Search',
    short: 'Filter by start, end, and length.',
    kicker: 'Flexible search',
    title: 'Word search and length filter',
    description: 'Mix first letter, last letter, and exact length to narrow the list.',
    action: 'Search words',
    resultHint: 'You can fill one, two, or all three filters.',
    empty: 'No search results yet.'
  }
];

const visibleLimit = 36;
const activeTool = ref('exists');
const loading = ref(false);
const error = ref('');

const existsForm = ref({ word: '' });
const prefixForm = ref({ prefix: '' });
const suffixForm = ref({ suffix: '' });
const subwordsForm = ref({ word: '', minLength: 3 });
const searchForm = ref({ start: '', end: '', length: null });

const existsResult = ref(null);
const listResult = ref({ count: 0, words: [] });

const currentTool = computed(() => toolCards.find((tool) => tool.id === activeTool.value) ?? toolCards[0]);
const visibleWords = computed(() => listResult.value.words.slice(0, visibleLimit));

const resetResults = () => {
  error.value = '';
  existsResult.value = null;
  listResult.value = { count: 0, words: [] };
};

const normalizeWord = (value) => value.trim().toLowerCase();

const fetchJson = async (url) => {
  const response = await fetch(`${API_BASE}${url}`);
  if (!response.ok) {
    throw new Error(`Server returned HTTP ${response.status}`);
  }
  return response.json();
};

const submitExists = async () => {
  const word = normalizeWord(existsForm.value.word);
  if (!word) {
    throw new Error('Please enter a word to check.');
  }
  existsResult.value = await fetchJson(`/api/word/exists/${encodeURIComponent(word)}`);
};

const submitPrefix = async () => {
  const prefix = normalizeWord(prefixForm.value.prefix);
  if (prefix.length < 1) {
    throw new Error('Please enter a prefix.');
  }
  listResult.value = await fetchJson(`/api/word/prefix/${encodeURIComponent(prefix)}`);
};

const submitSuffix = async () => {
  const suffix = normalizeWord(suffixForm.value.suffix);
  if (suffix.length < 1) {
    throw new Error('Please enter a suffix.');
  }
  listResult.value = await fetchJson(`/api/word/suffix/${encodeURIComponent(suffix)}`);
};

const submitSubwords = async () => {
  const word = normalizeWord(subwordsForm.value.word);
  const minLength = Number(subwordsForm.value.minLength || 3);
  if (!word) {
    throw new Error('Please enter a base word.');
  }
  if (minLength < 1) {
    throw new Error('Minimum length must be at least 1.');
  }
  listResult.value = await fetchJson(`/api/word/subwords/${encodeURIComponent(word)}?minLength=${minLength}`);
};

const submitSearch = async () => {
  const params = new URLSearchParams();
  const start = normalizeWord(searchForm.value.start);
  const end = normalizeWord(searchForm.value.end);
  const length = searchForm.value.length;

  if (start) params.set('start', start);
  if (end) params.set('end', end);
  if (length) params.set('length', String(length));

  if (![start, end, length].some(Boolean)) {
    throw new Error('Add at least one search filter.');
  }

  listResult.value = await fetchJson(`/api/word/search?${params.toString()}`);
};

const submitActiveTool = async () => {
  loading.value = true;
  error.value = '';
  if (activeTool.value !== 'exists') {
    existsResult.value = null;
  } else {
    listResult.value = { count: 0, words: [] };
  }

  try {
    if (activeTool.value === 'exists') {
      await submitExists();
    } else if (activeTool.value === 'prefix') {
      await submitPrefix();
    } else if (activeTool.value === 'suffix') {
      await submitSuffix();
    } else if (activeTool.value === 'subwords') {
      await submitSubwords();
    } else {
      await submitSearch();
    }
  } catch (err) {
    error.value = err.message || 'Something went wrong while using this tool.';
  } finally {
    loading.value = false;
  }
};

const resetActiveTool = () => {
  error.value = '';
  if (activeTool.value === 'exists') {
    existsForm.value.word = '';
  } else if (activeTool.value === 'prefix') {
    prefixForm.value.prefix = '';
  } else if (activeTool.value === 'suffix') {
    suffixForm.value.suffix = '';
  } else if (activeTool.value === 'subwords') {
    subwordsForm.value = { word: '', minLength: 3 };
  } else {
    searchForm.value = { start: '', end: '', length: null };
  }
  resetResults();
};

const playClickSound = () => {
  try {
    if (typeof Audio !== 'undefined') {
      const audio = new Audio(buttonClickedSound);
      audio.play()?.catch(() => {});
    }
  } catch (e) {}
};

const selectTool = (toolId) => {
  activeTool.value = toolId;
  playClickSound();
};
</script>

<style scoped>
.word-tools {
  display: grid;
  gap: 1.5rem;
}

.word-tools-header {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.4rem 1.5rem;
  border: 1px solid rgba(231, 229, 228, 0.95);
  background: linear-gradient(180deg, rgba(255, 252, 245, 0.98), rgba(250, 245, 235, 0.9));
  box-shadow: 0 18px 45px rgba(120, 88, 42, 0.08);
}

.word-tools-badge {
  min-width: 140px;
  padding: 0.9rem 1rem;
  border: 1px solid rgba(214, 211, 209, 0.9);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.word-tools-layout {
  display: grid;
  gap: 1rem;
}

.tool-rail {
  display: grid;
  gap: 0.75rem;
}

.tool-rail-button {
  position: relative;
  overflow: hidden;
  border-radius: 0.45rem;
  border: 1px solid #d6b37c;
  padding: 1rem 1.05rem;
  text-align: left;
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

.tool-rail-button::before {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(255, 248, 233, 0.78), rgba(255, 248, 233, 0) 44%),
    linear-gradient(90deg, rgba(255, 255, 255, 0.28), rgba(255, 255, 255, 0) 24%, rgba(114, 73, 22, 0.1) 100%);
  pointer-events: none;
}

.tool-rail-button:hover {
  transform: translateY(-2px);
  box-shadow:
    inset 0 1px 0 rgba(255, 250, 239, 0.95),
    inset 0 -2px 0 rgba(181, 126, 44, 0.32),
    0 6px 0 #be8b43,
    0 18px 28px rgba(120, 88, 42, 0.18);
}

.tool-rail-button-idle:hover {
  border-color: #bf8f4d;
}

.tool-rail-button-active {
  color: #2f1b0b;
  transform: translateY(4px);
  box-shadow:
    inset 0 2px 6px rgba(122, 68, 7, 0.18),
    inset 0 1px 0 rgba(255, 247, 227, 0.8),
    0 1px 0 #9e6c2b,
    0 8px 16px rgba(120, 88, 42, 0.16);
}

.tool-rail-label {
  display: block;
  font-weight: 800;
  color: #1c1917;
}

.tool-rail-copy {
  display: block;
  margin-top: 0.28rem;
  font-size: 0.82rem;
  line-height: 1.5;
  color: #57534e;
}

.tool-panel {
  border: 1px solid rgba(231, 229, 228, 0.95);
  background: rgba(255, 255, 255, 0.92);
  padding: 1.25rem;
  box-shadow: 0 16px 40px rgba(28, 25, 23, 0.06);
}

.tool-panel-intro {
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(231, 229, 228, 0.9);
}

.tool-form {
  display: grid;
  gap: 1rem;
  margin-top: 1rem;
}

.tool-fields {
  display: grid;
  gap: 0.55rem;
}

.tool-fields-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.tool-fields-grid-search {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.tool-label {
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #78716c;
}

.tool-input {
  width: 100%;
  border: 1px solid #d6d3d1;
  background: #fff;
  padding: 0.88rem 0.95rem;
  color: #1c1917;
  font-weight: 600;
  outline: none;
  transition: 180ms ease;
}

.tool-input:focus {
  border-color: #d6a45b;
  box-shadow: 0 0 0 4px rgba(245, 197, 115, 0.18);
}

.tool-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.tool-submit,
.tool-clear {
  min-height: 48px;
  padding: 0.85rem 1.2rem;
  font-weight: 800;
  border: 1px solid transparent;
  transition: 180ms ease;
}

.tool-submit {
  background: #1f3145;
  color: #fff;
}

.tool-submit:hover:not(:disabled) {
  background: #29415d;
}

.tool-clear {
  border-color: #d6d3d1;
  background: #fff;
  color: #44403c;
}

.tool-clear:hover:not(:disabled) {
  border-color: #a8a29e;
}

.tool-submit:disabled,
.tool-clear:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.tool-feedback {
  margin-top: 1rem;
  padding: 0.9rem 1rem;
  font-size: 0.92rem;
  font-weight: 700;
}

.tool-feedback-error {
  border: 1px solid #fecaca;
  background: #fff1f2;
  color: #be123c;
}

.tool-results {
  margin-top: 1.2rem;
}

.result-summary {
  display: grid;
  gap: 0.9rem;
}

.result-summary-bar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 1rem;
  align-items: end;
}

.result-hint {
  font-size: 0.85rem;
  color: #78716c;
}

.result-callout {
  padding: 1rem 1.05rem;
  border: 1px solid #d6d3d1;
  background: #fafaf9;
  color: #292524;
}

.result-callout-success {
  border-color: #86efac;
  background: #f0fdf4;
}

.result-callout-muted {
  border-color: #e7e5e4;
  background: #fafaf9;
}

.result-empty {
  padding: 1rem 1.05rem;
  border: 1px dashed #d6d3d1;
  color: #78716c;
  background: rgba(250, 250, 249, 0.8);
}

.result-word-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
}

.result-chip {
  display: inline-flex;
  align-items: center;
  min-height: 2rem;
  padding: 0.3rem 0.7rem;
  border: 1px solid #e7e5e4;
  background: #fffdf8;
  color: #44403c;
  font-size: 0.9rem;
  font-weight: 700;
}

.result-tail {
  font-size: 0.82rem;
  color: #78716c;
}

@media (min-width: 960px) {
  .word-tools-layout {
    grid-template-columns: 270px minmax(0, 1fr);
    align-items: start;
  }

  .tool-rail {
    position: sticky;
    top: 1rem;
  }
}

@media (max-width: 699px) {
  .word-tools-header,
  .tool-panel,
  .tool-rail-button,
  .result-callout,
  .result-empty,
  .word-tools-badge {
    border-radius: 0;
  }

  .tool-fields-grid,
  .tool-fields-grid-search {
    grid-template-columns: 1fr;
  }

  .word-tools {
    gap: 1rem;
  }

  .word-tools-header {
    padding: 1rem;
    gap: 0.85rem;
  }

  .word-tools-badge {
    min-width: 0;
    width: 100%;
  }

  .tool-rail {
    gap: 0.55rem;
  }

  .tool-rail-button {
    padding: 0.9rem 0.95rem;
  }

  .tool-panel {
    padding: 1rem;
  }

  .tool-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }

  .tool-submit,
  .tool-clear {
    width: 100%;
  }

  .result-summary-bar {
    align-items: start;
  }

  .result-word-grid {
    gap: 0.5rem;
  }

  .result-chip {
    font-size: 0.84rem;
  }
}
</style>
