package asia.fourtitude.interviewq.jumble.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import asia.fourtitude.interviewq.jumble.TestConfig;

@WebMvcTest(WordApiController.class)
@Import(TestConfig.class)
class WordApiControllerTest {

    static final ObjectMapper OM = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Test
    void whenCheckExistingWord_thenReturnsTrue() throws Exception {
        MvcResult resu = this.mvc.perform(get("/api/word/exists/tomato"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = OM.readTree(resu.getResponse().getContentAsString());
        assertEquals("tomato", json.get("word").asText());
        assertTrue(json.get("exists").asBoolean());
    }

    @Test
    void whenSearchPrefix_thenReturnsMatchingWords() throws Exception {
        MvcResult resu = this.mvc.perform(get("/api/word/prefix/pen"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = OM.readTree(resu.getResponse().getContentAsString());
        assertEquals("pen", json.get("prefix").asText());
        assertTrue(json.get("count").asInt() > 0);
        assertTrue(json.get("words").isArray());
    }

    @Test
    void whenSearchSuffix_thenReturnsMatchingWords() throws Exception {
        MvcResult resu = this.mvc.perform(get("/api/word/suffix/ing"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = OM.readTree(resu.getResponse().getContentAsString());
        assertEquals("ing", json.get("suffix").asText());
        assertTrue(json.get("count").asInt() > 0);
        assertTrue(json.get("words").isArray());
    }

    @Test
    void whenGenerateSubwords_thenReturnsWords() throws Exception {
        MvcResult resu = this.mvc.perform(get("/api/word/subwords/fusion").param("minLength", "3"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = OM.readTree(resu.getResponse().getContentAsString());
        assertEquals("fusion", json.get("word").asText());
        assertEquals(3, json.get("minLength").asInt());
        assertTrue(json.get("count").asInt() > 0);
        assertTrue(json.get("words").isArray());
    }

    @Test
    void whenSearchWordsWithFilters_thenReturnsMatches() throws Exception {
        MvcResult resu = this.mvc.perform(get("/api/word/search")
                .param("start", "f")
                .param("end", "r")
                .param("length", "6"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = OM.readTree(resu.getResponse().getContentAsString());
        assertEquals("f", json.get("start").asText());
        assertEquals("r", json.get("end").asText());
        assertEquals(6, json.get("length").asInt());
        assertTrue(json.get("count").asInt() > 0);
        assertTrue(json.get("words").toString().contains("flower"));
    }
}
