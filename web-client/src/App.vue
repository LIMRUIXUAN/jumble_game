<template>
  <main class="app-shell">
    <section class="hero-section">
      <nav class="top-nav" aria-label="App sections">
        <button
          type="button"
          class="app-nav-button"
          :class="[
            { 'app-nav-button-active': activeView === 'game' },
            navDirection === 'left' ? 'app-nav-button-swipe-left' : 'app-nav-button-swipe-right'
          ]"
          @click="setActiveView('game')"
        >
          Jumble Game
        </button>
        <button
          type="button"
          class="app-nav-button"
          :class="[
            { 'app-nav-button-active': activeView === 'tools' },
            navDirection === 'left' ? 'app-nav-button-swipe-left' : 'app-nav-button-swipe-right'
          ]"
          @click="setActiveView('tools')"
        >
          WordTools
        </button>
      </nav>
    </section>

    <section id="play" class="game-section">
      <JumbleGame v-if="activeView === 'game'" />
      <WordTools v-else />
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue';
import JumbleGame from './components/JumbleGame.vue';
import WordTools from './components/WordTools.vue';

const activeView = ref('game');
const navDirection = ref('right');

const setActiveView = (view) => {
  if (view === activeView.value) {
    return;
  }
  navDirection.value = view === 'tools' ? 'right' : 'left';
  activeView.value = view;
};
</script>
