package asia.fourtitude.interviewq.jumble.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import asia.fourtitude.interviewq.jumble.TestConfig;
import asia.fourtitude.interviewq.jumble.core.JumbleEngine;
import asia.fourtitude.interviewq.jumble.model.GameGuessInput;
import asia.fourtitude.interviewq.jumble.model.GameGuessOutput;
import asia.fourtitude.interviewq.jumble.model.GameSessionRepository;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(GameApiController.class)
@Import(TestConfig.class)
class GameApiControllerTest {

        static final ObjectMapper OM = new ObjectMapper();

        @Autowired
        private MockMvc mvc;

        @Autowired
        JumbleEngine jumbleEngine;

        @MockBean
        private GameSessionRepository gameSessionRepository;

        /*
         * NOTE: Refer to "RootControllerTest.java", "GameWebControllerTest.java"
         * as reference. Search internet for resource/tutorial/help in implementing
         * the unit tests.
         *
         * Refer to "http://localhost:8080/swagger-ui/index.html" for REST API
         * documentation and perform testing.
         *
         * Refer to Postman collection ("interviewq-jumble.postman_collection.json")
         * for REST API documentation and perform testing.
         */

        @Test
        void whenCreateNewGame_thenSuccess() throws Exception {
                /*
                 * Doing HTTP GET "/api/game/new"
                 *
                 * Input: None
                 *
                 * Expect: Assert these
                 * a) HTTP status == 200
                 * b) `result` equals "Created new game."
                 * c) `id` is not null
                 * d) `originalWord` is not null
                 * e) `scrambleWord` is not null
                 * f) `totalWords` > 0
                 * g) `remainingWords` > 0 and same as `totalWords`
                 * h) `guessedWords` is empty list
                 */
                // assertTrue(false, "to be implemented");
                MvcResult resu = this.mvc.perform(get("/api/game/new"))
                                .andExpect(status().isOk())
                                .andReturn();
                String json = resu.getResponse().getContentAsString();
                GameGuessOutput output = OM.readValue(json, GameGuessOutput.class);
                assertEquals("Created new game.", output.getResult());
                assertNotNull(output.getId());
                assertNotNull(output.getOriginalWord());
                assertNotNull(output.getScrambleWord());
                assertTrue(output.getTotalWords() > 0);
                assertTrue(output.getRemainingWords() > 0);
                assertEquals(output.getTotalWords(), output.getRemainingWords());
                assertNotNull(output.getGuessedWords());
                assertTrue(output.getGuessedWords().isEmpty());
        }

        @Test
        void givenMissingId_whenPlayGame_thenInvalidId() throws Exception {
                /*
                 * Doing HTTP POST "/api/game/guess"
                 *
                 * Input: JSON request body
                 * a) `id` is null or missing
                 * b) `word` is null/anything or missing
                 *
                 * Expect: Assert these
                 * a) HTTP status == 404
                 * b) `result` equals "Invalid Game ID."
                 */
                // assertTrue(false, "to be implemented");
                GameGuessInput input = new GameGuessInput();
                input.setId(null);
                input.setWord(null);

                MvcResult resu = this.mvc.perform(post("/api/game/guess")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(OM.writeValueAsString(input)))
                                .andExpect(status().isNotFound())
                                .andReturn();
                String json = resu.getResponse().getContentAsString();
                GameGuessOutput output = OM.readValue(json, GameGuessOutput.class);
                assertEquals("Invalid Game ID.", output.getResult());
        }

        @Test
        void givenMissingRecord_whenPlayGame_thenRecordNotFound() throws Exception {
                /*
                 * Doing HTTP POST "/api/game/guess"
                 *
                 * Input: JSON request body
                 * a) `id` is some valid ID (but not exists in game system)
                 * b) `word` is null/anything or missing
                 *
                 * Expect: Assert these
                 * a) HTTP status == 404
                 * b) `result` equals "Game board/state not found."
                 */
                GameGuessInput input = new GameGuessInput();
                input.setId(java.util.UUID.randomUUID().toString());
                input.setWord("abc");

                MvcResult resu = this.mvc.perform(post("/api/game/guess")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(OM.writeValueAsString(input)))
                                .andExpect(status().isNotFound())
                                .andReturn();
                String json = resu.getResponse().getContentAsString();
                GameGuessOutput output = OM.readValue(json, GameGuessOutput.class);
                assertEquals("Game board/state not found.", output.getResult());
        }

        @Test
        void givenCreateNewGame_whenSubmitNullWord_thenGuessedIncorrectly() throws Exception {
                /*
                 * Doing HTTP POST "/api/game/guess"
                 *
                 * Given:
                 * a) has valid game ID from previously created game
                 *
                 * Input: JSON request body
                 * a) `id` of previously created game
                 * b) `word` is null or missing
                 *
                 * Expect: Assert these
                 * a) HTTP status == 200
                 * b) `result` equals "Guessed incorrectly."
                 * c) `id` equals to `id` of this game
                 * d) `originalWord` is equals to `originalWord` of this game
                 * e) `scrambleWord` is not null
                 * f) `guessWord` is equals to `input.word`
                 * g) `totalWords` is equals to `totalWords` of this game
                 * h) `remainingWords` is equals to `remainingWords` of previous game state (no
                 * change)
                 * i) `guessedWords` is empty list (because this is first attempt)
                 */
                MvcResult createRes = this.mvc.perform(get("/api/game/new"))
                                .andExpect(status().isOk())
                                .andReturn();
                GameGuessOutput game = OM.readValue(createRes.getResponse().getContentAsString(),
                                GameGuessOutput.class);

                GameGuessInput input = new GameGuessInput();
                input.setId(game.getId());
                input.setWord(null);

                MvcResult playRes = this.mvc.perform(post("/api/game/guess")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(OM.writeValueAsString(input)))
                                .andExpect(status().isOk())
                                .andReturn();
                GameGuessOutput output = OM.readValue(playRes.getResponse().getContentAsString(),
                                GameGuessOutput.class);
                assertEquals("Guessed incorrectly.", output.getResult());
                assertEquals(game.getId(), output.getId());
                assertEquals(game.getOriginalWord(), output.getOriginalWord());
                assertNotNull(output.getScrambleWord());
                assertNull(output.getGuessWord());
                assertEquals(game.getTotalWords(), output.getTotalWords());
                assertEquals(game.getRemainingWords(), output.getRemainingWords());
                assertTrue(output.getGuessedWords().isEmpty());
        }

        @Test
        void givenCreateNewGame_whenSubmitWrongWord_thenGuessedIncorrectly() throws Exception {
                /*
                 * Doing HTTP POST "/api/game/guess"
                 *
                 * Given:
                 * a) has valid game ID from previously created game
                 *
                 * Input: JSON request body
                 * a) `id` of previously created game
                 * b) `word` is some value (that is not correct answer)
                 *
                 * Expect: Assert these
                 * a) HTTP status == 200
                 * b) `result` equals "Guessed incorrectly."
                 * c) `id` equals to `id` of this game
                 * d) `originalWord` is equals to `originalWord` of this game
                 * e) `scrambleWord` is not null
                 * f) `guessWord` equals to input `guessWord`
                 * g) `totalWords` is equals to `totalWords` of this game
                 * h) `remainingWords` is equals to `remainingWords` of previous game state (no
                 * change)
                 * i) `guessedWords` is empty list (because this is first attempt)
                 */
                MvcResult createRes = this.mvc.perform(get("/api/game/new"))
                                .andExpect(status().isOk())
                                .andReturn();
                GameGuessOutput game = OM.readValue(createRes.getResponse().getContentAsString(),
                                GameGuessOutput.class);

                GameGuessInput input = new GameGuessInput();
                input.setId(game.getId());
                input.setWord("wrongguess");

                MvcResult playRes = this.mvc.perform(post("/api/game/guess")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(OM.writeValueAsString(input)))
                                .andExpect(status().isOk())
                                .andReturn();
                GameGuessOutput output = OM.readValue(playRes.getResponse().getContentAsString(),
                                GameGuessOutput.class);
                assertEquals("Guessed incorrectly.", output.getResult());
                assertEquals(game.getId(), output.getId());
                assertEquals(game.getOriginalWord(), output.getOriginalWord());
                assertNotNull(output.getScrambleWord());
                assertEquals("wrongguess", output.getGuessWord());
                assertEquals(game.getTotalWords(), output.getTotalWords());
                assertEquals(game.getRemainingWords(), output.getRemainingWords());
                assertTrue(output.getGuessedWords().isEmpty());
        }

        @Test
        void givenCreateNewGame_whenSubmitFirstCorrectWord_thenGuessedCorrectly() throws Exception {
                /*
                 * Doing HTTP POST "/api/game/guess"
                 *
                 * Given:
                 * a) has valid game ID from previously created game
                 *
                 * Input: JSON request body
                 * a) `id` of previously created game
                 * b) `word` is of correct answer
                 *
                 * Expect: Assert these
                 * a) HTTP status == 200
                 * b) `result` equals "Guessed correctly."
                 * c) `id` equals to `id` of this game
                 * d) `originalWord` is equals to `originalWord` of this game
                 * e) `scrambleWord` is not null
                 * f) `guessWord` equals to input `guessWord`
                 * g) `totalWords` is equals to `totalWords` of this game
                 * h) `remainingWords` is equals to `remainingWords - 1` of previous game state
                 * (decrement by 1)
                 * i) `guessedWords` is not empty list
                 * j) `guessWords` contains input `guessWord`
                 */
                MvcResult createRes = this.mvc.perform(get("/api/game/new"))
                                .andExpect(status().isOk())
                                .andReturn();
                GameGuessOutput game = OM.readValue(createRes.getResponse().getContentAsString(),
                                GameGuessOutput.class);

                java.util.Collection<String> subWords = jumbleEngine.generateSubWords(game.getOriginalWord(), 3);
                String correctWord = subWords.iterator().next();

                GameGuessInput input = new GameGuessInput();
                input.setId(game.getId());
                input.setWord(correctWord);

                MvcResult playRes = this.mvc.perform(post("/api/game/guess")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(OM.writeValueAsString(input)))
                                .andExpect(status().isOk())
                                .andReturn();
                GameGuessOutput output = OM.readValue(playRes.getResponse().getContentAsString(),
                                GameGuessOutput.class);
                assertEquals("Guessed correctly.", output.getResult());
                assertEquals(game.getId(), output.getId());
                assertEquals(game.getOriginalWord(), output.getOriginalWord());
                assertNotNull(output.getScrambleWord());
                assertEquals(correctWord, output.getGuessWord());
                assertEquals(game.getTotalWords(), output.getTotalWords());
                assertEquals(game.getRemainingWords() - 1, output.getRemainingWords());
                assertNotNull(output.getGuessedWords());
                assertTrue(output.getGuessedWords().contains(correctWord));
        }

        @Test
        void givenCreateNewGame_whenSubmitAllCorrectWord_thenAllGuessed() throws Exception {
                /*
                 * Doing HTTP POST "/api/game/guess"
                 *
                 * Given:
                 * a) has valid game ID from previously created game
                 * b) has submit all correct answers, except the last answer
                 *
                 * Input: JSON request body
                 * a) `id` of previously created game
                 * b) `word` is of the last correct answer
                 *
                 * Expect: Assert these
                 * a) HTTP status == 200
                 * b) `result` equals "All words guessed."
                 * c) `id` equals to `id` of this game
                 * d) `originalWord` is equals to `originalWord` of this game
                 * e) `scrambleWord` is not null
                 * f) `guessWord` equals to input `guessWord`
                 * g) `totalWords` is equals to `totalWords` of this game
                 * h) `remainingWords` is 0 (no more remaining, game ended)
                 * i) `guessedWords` is not empty list
                 * j) `guessWords` contains input `guessWord`
                 */
                // assertTrue(false, "to be implemented");
                MvcResult createRes = this.mvc.perform(get("/api/game/new"))
                                .andExpect(status().isOk())
                                .andReturn();
                GameGuessOutput game = OM.readValue(createRes.getResponse().getContentAsString(),
                                GameGuessOutput.class);

                java.util.List<String> subWords = new java.util.ArrayList<>(
                                jumbleEngine.generateSubWords(game.getOriginalWord(), 3));

                for (int i = 0; i < subWords.size() - 1; i++) {
                        GameGuessInput input = new GameGuessInput();
                        input.setId(game.getId());
                        input.setWord(subWords.get(i));

                        this.mvc.perform(post("/api/game/guess")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(OM.writeValueAsString(input)))
                                        .andExpect(status().isOk());
                }

                GameGuessInput lastInput = new GameGuessInput();
                lastInput.setId(game.getId());
                lastInput.setWord(subWords.get(subWords.size() - 1));

                MvcResult playRes = this.mvc.perform(post("/api/game/guess")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(OM.writeValueAsString(lastInput)))
                                .andExpect(status().isOk())
                                .andReturn();
                GameGuessOutput output = OM.readValue(playRes.getResponse().getContentAsString(),
                                GameGuessOutput.class);
                assertEquals("All words guessed.", output.getResult());
                assertEquals(game.getId(), output.getId());
                assertEquals(game.getOriginalWord(), output.getOriginalWord());
                assertNotNull(output.getScrambleWord());
                assertEquals(subWords.get(subWords.size() - 1), output.getGuessWord());
                assertEquals(game.getTotalWords(), output.getTotalWords());
                assertEquals(0, output.getRemainingWords());
                assertNotNull(output.getGuessedWords());
                assertEquals(game.getTotalWords(), output.getGuessedWords().size());
                assertTrue(output.getGuessedWords().contains(subWords.get(subWords.size() - 1)));
        }

}
