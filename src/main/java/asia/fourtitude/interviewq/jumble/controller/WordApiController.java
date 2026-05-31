package asia.fourtitude.interviewq.jumble.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import asia.fourtitude.interviewq.jumble.core.JumbleEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Word API", description = "Word REST API endpoint.")
@RequestMapping(path = "/api/word")
public class WordApiController {

    private final JumbleEngine jumbleEngine;

    @Autowired(required = true)
    public WordApiController(JumbleEngine jumbleEngine) {
        this.jumbleEngine = jumbleEngine;
    }

    @Operation(
            summary = "Auto complete based on prefix",
            description = "Returns a list of words matching the input `prefix` (of at least 3 letters).")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    examples = {
                                            @ExampleObject(
                                                    name = "Success",
                                                    description = "The list of words matching the `prefix`.",
                                                    value = "[\n" +
                                                            "  \"awe\",\n" +
                                                            "  \"awed\",\n" +
                                                            "  \"awes\",\n" +
                                                            "  \"awesome\",\n" +
                                                            "  \"awesomely\",\n" +
                                                            "  \"awesomeness\",\n" +
                                                            "  \"awestruck\"\n" +
                                                            "]") })) })
    @GetMapping(value = "/{prefix}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<String>> autoComplete(
            @Parameter(
                    description = "The prefix.",
                    required = true,
                    example = "awe")
            @PathVariable String prefix) {
        prefix = StringUtils.trimToEmpty(prefix);
        if (prefix.length() < 3) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }
        Collection<String> words = this.jumbleEngine.wordsMatchingPrefix(prefix);
        return new ResponseEntity<>(words, HttpStatus.OK);
    }

    @Operation(summary = "Check if a word exists in the dictionary")
    @GetMapping(value = "/exists/{word}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> exists(
            @Parameter(description = "The word to check.", required = true, example = "tomato")
            @PathVariable String word) {
        String normalized = StringUtils.trimToEmpty(word);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("word", normalized);
        payload.put("exists", this.jumbleEngine.exists(normalized));
        return new ResponseEntity<>(payload, HttpStatus.OK);
    }

    @Operation(summary = "Find words matching a prefix")
    @GetMapping(value = "/prefix/{prefix}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> prefix(
            @Parameter(description = "The prefix to match.", required = true, example = "pen")
            @PathVariable String prefix) {
        String normalized = StringUtils.trimToEmpty(prefix);
        Collection<String> words = normalized.length() < 1
                ? Collections.emptyList()
                : this.jumbleEngine.wordsMatchingPrefix(normalized);
        return new ResponseEntity<>(listPayload("prefix", normalized, words), HttpStatus.OK);
    }

    @Operation(summary = "Find words matching a suffix")
    @GetMapping(value = "/suffix/{suffix}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> suffix(
            @Parameter(description = "The suffix to match.", required = true, example = "ing")
            @PathVariable String suffix) {
        String normalized = StringUtils.trimToEmpty(suffix);
        Collection<String> words = normalized.length() < 1
                ? Collections.emptyList()
                : this.jumbleEngine.wordsMatchingSuffix(normalized);
        return new ResponseEntity<>(listPayload("suffix", normalized, words), HttpStatus.OK);
    }

    @Operation(summary = "Generate valid subwords from a base word")
    @GetMapping(value = "/subwords/{word}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> subWords(
            @Parameter(description = "The base word.", required = true, example = "yellow")
            @PathVariable String word,
            @Parameter(description = "Minimum subword length.", example = "3")
            @RequestParam(required = false) Integer minLength) {
        String normalized = StringUtils.trimToEmpty(word);
        Collection<String> words = normalized.isEmpty()
                ? Collections.emptyList()
                : this.jumbleEngine.generateSubWords(normalized, minLength);
        Map<String, Object> payload = listPayload("word", normalized, words);
        payload.put("minLength", minLength == null ? 3 : minLength);
        return new ResponseEntity<>(payload, HttpStatus.OK);
    }

    @Operation(summary = "Search words by first letter, last letter, and/or length")
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> search(
            @Parameter(description = "Starting letter.", example = "a")
            @RequestParam(required = false) String start,
            @Parameter(description = "Ending letter.", example = "e")
            @RequestParam(required = false) String end,
            @Parameter(description = "Word length.", example = "5")
            @RequestParam(required = false) Integer length) {
        Character startChar = firstLetterOrNull(start);
        Character endChar = firstLetterOrNull(end);
        Collection<String> words = this.jumbleEngine.searchWords(startChar, endChar, length);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("start", startChar == null ? null : String.valueOf(startChar));
        payload.put("end", endChar == null ? null : String.valueOf(endChar));
        payload.put("length", length);
        payload.put("count", words.size());
        payload.put("words", words);
        return new ResponseEntity<>(payload, HttpStatus.OK);
    }

    private Character firstLetterOrNull(String input) {
        String normalized = StringUtils.trimToEmpty(input);
        if (normalized.isEmpty()) {
            return null;
        }
        char ch = normalized.charAt(0);
        if (!Character.isLetter(ch)) {
            return null;
        }
        return ch;
    }

    private Map<String, Object> listPayload(String fieldName, String fieldValue, Collection<String> words) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(fieldName, fieldValue);
        payload.put("count", words.size());
        payload.put("words", words);
        return payload;
    }

}
