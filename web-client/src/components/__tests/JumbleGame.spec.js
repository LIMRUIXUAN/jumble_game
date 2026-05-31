import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import JumbleGame from '../JumbleGame.vue';

describe('JumbleGame.vue', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
    localStorage.clear();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('renders initial start menu correctly', () => {
    const wrapper = mount(JumbleGame);
    expect(wrapper.text()).toContain('Jumble game');
    expect(wrapper.text()).toContain('Start game');
    expect(wrapper.text()).toContain('Easy');
    expect(wrapper.text()).toContain('Medium');
    expect(wrapper.text()).toContain('Hard');
  });

  it('allows changing difficulty level selection', async () => {
    const wrapper = mount(JumbleGame);
    const buttons = wrapper.findAll('button[type="button"]');
    
    // Medium button is buttons[1]
    await buttons[1].trigger('click');
    expect(wrapper.vm.difficulty).toBe('medium');

    // Hard button is buttons[2]
    await buttons[2].trigger('click');
    expect(wrapper.vm.difficulty).toBe('hard');
  });

  it('starts a new game successfully and displays scrambled letters', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'test-uuid-123',
        result: 'Created new game.',
        original_word: 'tomato',
        scramble_word: 'amotto',
        total_words: 10,
        remaining_words: 10,
        guessed_words: []
      })
    });

    const wrapper = mount(JumbleGame);
    await wrapper.find('.start-game-button').trigger('click');
    await flushPromises();

    expect(wrapper.vm.game).toBeTruthy();
    expect(wrapper.vm.game.id).toBe('test-uuid-123');
    expect(wrapper.text()).toContain('a m o t t o');
    expect(wrapper.text()).toContain('0 / 10');
  });

  it('performs client-side validation on short and invalid guesses', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'test-uuid-123',
        result: 'Created new game.',
        original_word: 'tomato',
        scramble_word: 'amotto',
        total_words: 10,
        remaining_words: 10,
        guessed_words: []
      })
    });

    const wrapper = mount(JumbleGame);
    await wrapper.find('.start-game-button').trigger('click');
    await flushPromises();

    const input = wrapper.find('input[type="text"]');

    // Short guess validation
    await input.setValue('to');
    await wrapper.find('form').trigger('submit');
    expect(wrapper.text()).toContain('Guesses must be at least 3 characters long.');

    // Non-letters validation
    await input.setValue('tom1');
    await wrapper.find('form').trigger('submit');
    expect(wrapper.text()).toContain('Guesses must contain letters only.');
  });

  it('increments score and updates lists on a correct guess', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'test-uuid-123',
        result: 'Created new game.',
        original_word: 'tomato',
        scramble_word: 'amotto',
        total_words: 3,
        remaining_words: 3,
        guessed_words: []
      })
    });

    const wrapper = mount(JumbleGame);
    await wrapper.find('.start-game-button').trigger('click');
    await flushPromises();

    // Mock correct guess
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        result: 'Guessed correctly.',
        guessed_words: ['tot', 'tom'],
        remaining_words: 1,
        total_words: 3
      })
    });

    const input = wrapper.find('input[type="text"]');
    await input.setValue('tot');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.vm.score).toBe(10);
    expect(wrapper.text()).toContain('Correct!');
    expect(wrapper.text()).toContain('tot');
    expect(wrapper.text()).toContain('tom');
  });

  it('decrements points and increments incorrect guesses limit on wrong guess', async () => {
    // Start game in Medium mode
    const wrapper = mount(JumbleGame);
    const buttons = wrapper.findAll('button[type="button"]');
    await buttons[1].trigger('click'); // Medium (8 Guesses)
    
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'test-uuid-123',
        result: 'Created new game.',
        original_word: 'tomato',
        scramble_word: 'amotto',
        total_words: 3,
        remaining_words: 3,
        guessed_words: []
      })
    });

    await wrapper.find('.start-game-button').trigger('click');
    await flushPromises();

    // Set some starting score
    wrapper.vm.score = 20;

    // Mock incorrect guess
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        result: 'Guessed incorrectly.',
        guessed_words: [],
        remaining_words: 3,
        total_words: 3
      })
    });

    const input = wrapper.find('input[type="text"]');
    await input.setValue('wrong');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.vm.score).toBe(18); // -2 points
    expect(wrapper.vm.incorrectGuesses).toBe(1); // 1 incorrect guess
    expect(wrapper.text()).toContain('Guessed incorrectly. -2 points.');
  });

  it('triggers game over when incorrect guesses limit is reached', async () => {
    // Start game in Hard mode (5 Guesses limit)
    const wrapper = mount(JumbleGame);
    const buttons = wrapper.findAll('button[type="button"]');
    await buttons[2].trigger('click'); // Hard
    
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'test-uuid-123',
        result: 'Created new game.',
        original_word: 'tomato',
        scramble_word: 'amotto',
        total_words: 3,
        remaining_words: 3,
        guessed_words: []
      })
    });

    await wrapper.find('.start-game-button').trigger('click');
    await flushPromises();

    // Simulate 5 incorrect guesses
    wrapper.vm.incorrectGuesses = 4; // 4 guesses used

    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        result: 'Guessed incorrectly.',
        guessed_words: [],
        remaining_words: 3,
        total_words: 3
      })
    });

    const input = wrapper.find('input[type="text"]');
    await input.setValue('wrong');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.vm.incorrectGuesses).toBe(5);
    expect(wrapper.vm.showGameOver).toBe(true);
    expect(wrapper.text()).toContain('Round finished');
    expect(wrapper.text()).toContain('tomato'); // reveals base word
  });
});
