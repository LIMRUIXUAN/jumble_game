import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import WordTools from '../WordTools.vue';

describe('WordTools.vue', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('renders the default tool panel', () => {
    const wrapper = mount(WordTools);
    expect(wrapper.text()).toContain('Practice without the scramble');
    expect(wrapper.text()).toContain('Word existence checker');
    expect(wrapper.text()).toContain('Word to check');
  });

  it('switches between tool panels', async () => {
    const wrapper = mount(WordTools);
    const buttons = wrapper.findAll('.tool-rail-button');
    await buttons[2].trigger('click');
    expect(wrapper.text()).toContain('Suffix match');
    expect(wrapper.text()).toContain('Suffix');
  });

  it('shows validation feedback before searching with empty filters', async () => {
    const wrapper = mount(WordTools);
    const buttons = wrapper.findAll('.tool-rail-button');
    await buttons[4].trigger('click');
    await wrapper.find('form').trigger('submit');
    expect(wrapper.text()).toContain('Add at least one search filter.');
  });

  it('renders fetched prefix results', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        prefix: 'pen',
        count: 2,
        words: ['pen', 'pencil']
      })
    });

    const wrapper = mount(WordTools);
    const buttons = wrapper.findAll('.tool-rail-button');
    await buttons[1].trigger('click');
    await wrapper.find('#prefix-word').setValue('pen');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(fetch).toHaveBeenCalledWith('/api/word/prefix/pen');
    expect(wrapper.text()).toContain('2');
    expect(wrapper.text()).toContain('pencil');
  });
});
