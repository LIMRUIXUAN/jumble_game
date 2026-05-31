package asia.fourtitude.interviewq.jumble.controller;

import java.time.ZonedDateTime;
import java.util.Collection;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import asia.fourtitude.interviewq.jumble.core.JumbleEngine;
import asia.fourtitude.interviewq.jumble.model.ExistsForm;
import asia.fourtitude.interviewq.jumble.model.PrefixForm;
import asia.fourtitude.interviewq.jumble.model.ScrambleForm;
import asia.fourtitude.interviewq.jumble.model.SearchForm;
import asia.fourtitude.interviewq.jumble.model.SubWordsForm;

@Controller
@RequestMapping(path = "/")
public class RootController {

    private static final Logger LOG = LoggerFactory.getLogger(RootController.class);

    private final JumbleEngine jumbleEngine;

    @Autowired(required = true)
    public RootController(JumbleEngine jumbleEngine) {
        this.jumbleEngine = jumbleEngine;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("timeNow", ZonedDateTime.now());
        return "index";
    }

    @GetMapping("scramble")
    public String doGetScramble(Model model) {
        model.addAttribute("form", new ScrambleForm());
        return "scramble";
    }

    @PostMapping("scramble")
    /*
     * TODO:
     * a) Validate the input `form`
     * b) To call JumbleEngine#scramble()
     * c) Presentation page to show the result
     * d) Must pass the corresponding unit tests
     */
    public String doPostScramble(
            @Valid @ModelAttribute(name = "form") ScrambleForm form,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "scramble";
        }

        String word = form.getWord();
        String result = jumbleEngine.scramble(word.trim());

        form.setScramble(result);

        return "scramble";
    }

    @GetMapping("palindrome")
    public String doGetPalindrome(Model model) {
        model.addAttribute("words", this.jumbleEngine.retrievePalindromeWords());
        return "palindrome";
    }

    @GetMapping("exists")
    public String doGetExists(Model model) {
        model.addAttribute("form", new ExistsForm());
        return "exists";
    }

    @PostMapping("exists")
    public String doPostExists(
            @Valid @ModelAttribute(name = "form") ExistsForm form,
            BindingResult bindingResult, Model model) {
        /*
         * TODO:
         * a) Validate the input `form`
         * b) To call JumbleEngine#exists()
         * c) Presentation page to show the result
         * d) Must pass the corresponding unit tests
         */

        if (bindingResult.hasErrors()) {
            return "exists";
        }

        String word = form.getWord();
        boolean result = jumbleEngine.exists(word.trim());

        form.setExists(result);
        return "exists";
    }

    @GetMapping("prefix")
    public String doGetPrefix(Model model) {
        model.addAttribute("form", new PrefixForm());
        return "prefix";
    }

    @PostMapping("prefix")
    public String doPostPrefix(
            @Valid @ModelAttribute(name = "form") PrefixForm form,
            BindingResult bindingResult, Model model) {
        /*
         * TODO:
         * a) Validate the input `form`
         * b) To call JumbleEngine#wordsMatchingPrefix()
         * c) Presentation page to show the result
         * d) Must pass the corresponding unit tests
         */
        if (bindingResult.hasErrors()) {
            return "prefix";
        }

        String prefix = form.getPrefix();
        Collection<String> words = jumbleEngine.wordsMatchingPrefix(prefix.trim());

        form.setWords(words);

        return "prefix";
    }

    @GetMapping("search")
    public String doGetSearch(Model model) {
        model.addAttribute("form", new SearchForm());
        return "search";
    }

    @PostMapping("search")
    public String doPostSearch(
            @Valid @ModelAttribute(name = "form") SearchForm form,
            BindingResult bindingResult, Model model) {
        /*
         * TODO:
         * a) Validate the input `form`
         * b) Show the fields error accordingly: "Invalid startChar", "Invalid endChar",
         * "Invalid length".
         * c) To call JumbleEngine#searchWords()
         * d) Presentation page to show the result
         * e) Must pass the corresponding unit tests
         */
        String startCharInput = form.getStartChar();
        String endCharInput = form.getEndChar();
        Integer length = form.getLength();

        boolean isStartCharEmpty = (startCharInput == null || startCharInput.trim().isEmpty())
                && !bindingResult.hasFieldErrors("startChar");
        boolean isEndCharEmpty = (endCharInput == null || endCharInput.trim().isEmpty())
                && !bindingResult.hasFieldErrors("endChar");
        boolean isLengthEmpty = (length == null) && !bindingResult.hasFieldErrors("length");

        if (isStartCharEmpty && isEndCharEmpty && isLengthEmpty) {
            bindingResult.rejectValue("startChar", "error.startChar", "Invalid startChar");
            bindingResult.rejectValue("endChar", "error.endChar", "Invalid endChar");
            bindingResult.rejectValue("length", "error.length", "Invalid length");
        }

        if (length != null && length <= 0) {
            bindingResult.rejectValue("length", "error.length", "Invalid length");
        }

        if (bindingResult.hasErrors()) {
            return "search";
        }

        Character startChar = null;
        if (!isStartCharEmpty) {
            startChar = Character.toLowerCase(startCharInput.trim().charAt(0));
        }

        Character endChar = null;
        if (!isEndCharEmpty) {
            endChar = Character.toLowerCase(endCharInput.trim().charAt(0));
        }

        Collection<String> words = jumbleEngine.searchWords(startChar, endChar, length);

        form.setWords(words);
        return "search";
    }

    @GetMapping("subWords")
    public String goGetSubWords(Model model) {
        model.addAttribute("form", new SubWordsForm());
        return "subWords";
    }

    @PostMapping("subWords")
    public String doPostSubWords(
            @Valid @ModelAttribute(name = "form") SubWordsForm form,
            BindingResult bindingResult, Model model) {
        /*
         * TODO:
         * a) Validate the input `form`
         * b) To call JumbleEngine#generateSubWords()
         * c) Presentation page to show the result
         * d) Must pass the corresponding unit tests
         */
        if (bindingResult.hasErrors()) {
            return "subWords";
        }

        String word = form.getWord();
        Integer minLength = form.getMinLength();

        if (minLength != null && minLength <= 0) {
            bindingResult.rejectValue("minLength", "error.minLength", "Invalid minlength");
            return "subWords";
        }

        Collection<String> words = jumbleEngine.generateSubWords(word.trim(), minLength);

        form.setWords(words);
        return "subWords";
    }

}
