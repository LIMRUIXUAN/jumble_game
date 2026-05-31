package asia.fourtitude.interviewq.jumble.core;

import java.io.*;
import java.util.*;

public class JumbleEngine {

    private final Set<String> wordSet = new HashSet<>();
    private final List<String> allWords = new ArrayList<>();
    private final Map<Integer, List<String>> wordsByLength = new HashMap<>();
    // when we initial the object of class, it will generate dataset for above
    // HastSet, ArrayList, HashMap by calling loadWords() func lah
    public JumbleEngine() {
        loadWords();
    }

    private void loadWords() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("words.txt")) {
            if (is == null) {
                System.err.println("Error: Could not find words.txt on classpath");
                return;
            }
            try (Scanner sc = new Scanner(is)) {
                while (sc.hasNextLine()) {
                    addWord(sc.nextLine());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading words.txt: " + e.getMessage());
        }
    }

    private void addWord(String line) {
        String word = line.trim();
        if (!word.isEmpty()) {
            wordSet.add(word);
            allWords.add(word);
            wordsByLength.computeIfAbsent(word.length(), k -> new ArrayList<>()).add(word);
        }
    }

    /**
     * From the input `word`, produces/generates a copy which has the same
     * letters, but in different ordering.
     *
     * Example: from "elephant" to "lehnaetp".
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#scramble()
     * b) scrambled letters/output must not be the same as input
     *
     * @param word The input word to scramble the letters.
     * @return The scrambled output/letters.
     */

    public String scramble(String word) {

        if (word == null)
            return null;

        String cleaned = word.trim();
        String target = cleaned.toLowerCase();
        if (target.length() <= 1)
            return target;

        char first = target.charAt(0);
        boolean sameAlphabet = true;
        for (int i = 1; i < target.length(); i++) {
            if (target.charAt(i) != first) {
                sameAlphabet = false;
                break;
            }
        }
        if (sameAlphabet)
            return target;

        List<Character> chars = new ArrayList<>();
        for (char ch : target.toCharArray()) {
            chars.add(ch);
        }

        String scrambledWord;
        // using do-while looping due to we having at least one time shuffle
        // Time Complexity is O(1) if the best case
        do {
            Collections.shuffle(chars);
            StringBuilder sb = new StringBuilder();
            for (char ch : chars)
                sb.append(ch);
            scrambledWord = sb.toString();
        } while (scrambledWord.equals(target));

        return scrambledWord;
    }

    /**
     * Retrieves the palindrome words from the internal
     * word list/dictionary ("src/main/resources/words.txt").
     *
     * Word of single letter is not considered as valid palindrome word.
     *
     * Examples: "eye", "deed", "level".
     *
     * Evaluation/Grading:
     * a) able to access/use resource from classpath
     * b) using inbuilt Collections
     * c) using "try-with-resources" functionality/statement
     * d) pass unit test: JumbleEngineTest#palindrome()
     *
     * @return The list of palindrome words found in system/engine.
     * @see https://www.google.com/search?q=palindrome+meaning
     */
    public Collection<String> retrievePalindromeWords() {
        Collection<String> palindromeList = new ArrayList<>();
        for (String word : allWords) {
            if (word.length() > 1) {
                String reversed = new StringBuilder(word).reverse().toString();
                if (reversed.equalsIgnoreCase(word)) {
                    palindromeList.add(word);
                }
            }
        }
        return palindromeList;
    }

    /**
     * Picks one word randomly from internal word list.
     *
     * Evaluation/Grading:
     * a) pass unit test: JumbleEngineTest#randomWord()
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param length The word picked, must of length.
     *               When length is null, then return random word of any length.
     * @return One of the word (randomly) from word list.
     *         Or null if none matching.
     */
    public String pickOneRandomWord(Integer length) {
        Random random = new Random();
        if (length == null) {
            if (allWords.isEmpty())
                return null;
            return allWords.get(random.nextInt(allWords.size()));
        }
        if (length <= 0)
            return null;

        List<String> list = wordsByLength.get(length);
        if (list == null || list.isEmpty())
            return null;

        return list.get(random.nextInt(list.size()));
    }

    /**
     * Checks if the `word` exists in internal word list.
     * Matching is case insensitive.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word The input word to check.
     * @return true if `word` exists in internal word list.
     */
    public boolean exists(String word) {
        if (word == null)
            return false;

        // due to dataset given is lowercase, we just force all the input into lowercase
        word = word.trim().toLowerCase();
        if (word.isEmpty())
            return false;
        return wordSet.contains(word);
    }

    /**
     * Finds all the words from internal word list which begins with the
     * input `prefix`.
     * Matching is case insensitive.
     *
     * Invalid `prefix` (null, empty string, blank string, non letter) will
     * return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param prefix The prefix to match.
     * @return The list of words matching the prefix.
     */
    public Collection<String> wordsMatchingPrefix(String prefix) {
        Collection<String> matchingWords = new ArrayList<>();
        if (prefix == null)
            return matchingWords;
        prefix = prefix.trim().toLowerCase();
        if (prefix.isEmpty())
            return matchingWords;

        for (int i = 0; i < prefix.length(); i++) {
            if (!Character.isLetter(prefix.charAt(i))) {
                return matchingWords;
            }
        }

        for (String word : allWords) {
            if (word.toLowerCase().startsWith(prefix)) {
                matchingWords.add(word);
            }
        }
        return matchingWords;
    }

    /**
     * Finds all the words from internal word list which ends with the
     * input `suffix`.
     * Matching is case insensitive.
     *
     * Invalid `suffix` (null, empty string, blank string, non letter) will
     * return empty list.
     *
     * @param suffix The suffix to match.
     * @return The list of words matching the suffix.
     */
    public Collection<String> wordsMatchingSuffix(String suffix) {
        Collection<String> matchingWords = new ArrayList<>();
        if (suffix == null)
            return matchingWords;
        suffix = suffix.trim().toLowerCase();
        if (suffix.isEmpty())
            return matchingWords;

        for (int i = 0; i < suffix.length(); i++) {
            if (!Character.isLetter(suffix.charAt(i))) {
                return matchingWords;
            }
        }

        for (String word : allWords) {
            if (word.toLowerCase().endsWith(suffix)) {
                matchingWords.add(word);
            }
        }
        return matchingWords;
    }

    /**
     * Finds all the words from internal word list that is matching
     * the searching criteria.
     *
     * `startChar` and `endChar` must be 'a' to 'z' only. And case insensitive.
     * `length`, if have value, must be positive integer (>= 1).
     *
     * Words are filtered using `startChar` and `endChar` first.
     * Then apply `length` on the result, to produce the final output.
     *
     * Must have at least one valid value out of 3 inputs
     * (`startChar`, `endChar`, `length`) to proceed with searching.
     * Otherwise, return empty list.
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param startChar The first character of the word to search for.
     * @param endChar   The last character of the word to match with.
     * @param length    The length of the word to match.
     * @return The list of words matching the searching criteria.
     */
    public Collection<String> searchWords(Character startChar, Character endChar, Integer length) {
        Collection<String> matchingWords = new ArrayList<>();

        boolean startValid = (startChar != null
                && ((startChar >= 'a' && startChar <= 'z') || (startChar >= 'A' && startChar <= 'Z')));
        boolean endValid = (endChar != null
                && ((endChar >= 'a' && endChar <= 'z') || (endChar >= 'A' && endChar <= 'Z')));
        boolean lengthValid = (length != null && length >= 1);

        if (!startValid && !endValid && !lengthValid) {
            return matchingWords;
        }

        for (String word : allWords) {
            if (word == null || word.isEmpty())
                continue;

            if (startChar != null && Character.toLowerCase(word.charAt(0)) != Character.toLowerCase(startChar))
                continue;

            if (endChar != null
                    && Character.toLowerCase(word.charAt(word.length() - 1)) != Character.toLowerCase(endChar))
                continue;

            if (length != null && word.length() != length)
                continue;

            matchingWords.add(word);
        }

        return matchingWords;
    }

    /**
     * Generates all possible combinations of smaller/sub words using the
     * letters from input word.
     *
     * The `minLength` set the minimum length of sub word that is considered
     * as acceptable word.
     *
     * If length of input `word` is less than `minLength`, then return empty list.
     *
     * The sub words must exist in internal word list.
     *
     * Example: From "yellow" and `minLength` = 3, the output sub words:
     * low, lowly, lye, ole, owe, owl, well, welly, woe, yell, yeow, yew, yowl
     *
     * Evaluation/Grading:
     * a) pass related unit tests in "JumbleEngineTest"
     * b) provide a good enough implementation, if not able to provide a fast lookup
     * c) bonus points, if able to implement a fast lookup/scheme
     *
     * @param word      The input word to use as base/seed.
     * @param minLength The minimum length (inclusive) of sub words.
     *                  When zero, return empty list.
     *                  Default is 3.
     * @return The list of sub words constructed from input `word`.
     */
    public Collection<String> generateSubWords(String word, Integer minLength) {
        Collection<String> matchingWords = new ArrayList<>();

        if (word == null) {
            return matchingWords;
        }

        if (minLength == null) {
            minLength = 3;
        }

        if (minLength <= 0) {
            return matchingWords;
        }

        word = word.trim().toLowerCase();

        if (word.isEmpty()) {
            return matchingWords;
        }

        if (word.length() < minLength) {
            return matchingWords;
        }

        for (String candidate : allWords) {
            if (candidate == null || candidate.isEmpty())
                continue;

            String candidateLower = candidate.trim().toLowerCase();

            if (candidateLower.length() < minLength || candidateLower.length() > word.length()
                    || candidateLower.equals(word))
                continue;

            if (canBuildFrom(candidateLower, word))
                matchingWords.add(candidate);
        }

        return matchingWords;
    }

    private boolean canBuildFrom(String candidate, String source) {
        int[] counts = new int[256];
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c < 256) {
                counts[c]++;
            }
        }
        for (int i = 0; i < candidate.length(); i++) {
            char c = candidate.charAt(i);
            if (c >= 256) {
                return false;
            }
            if (--counts[c] < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a game state with word to guess, scrambled letters, and
     * possible combinations of words.
     *
     * Word is of length 6 characters.
     * The minimum length of sub words is of length 3 characters.
     *
     * @param length    The length of selected word.
     *                  Expects >= 3.
     * @param minLength The minimum length (inclusive) of sub words.
     *                  Expects positive integer.
     *                  Default is 3.
     * @return The game state.
     */
    public GameState createGameState(Integer length, Integer minLength) {
        Objects.requireNonNull(length, "length must not be null");
        if (minLength == null) {
            minLength = 3;
        } else if (minLength <= 0) {
            throw new IllegalArgumentException("Invalid minLength=[" + minLength + "], expect positive integer");
        }
        if (length < 3) {
            throw new IllegalArgumentException("Invalid length=[" + length + "], expect greater than or equals 3");
        }
        if (minLength > length) {
            throw new IllegalArgumentException(
                    "Expect minLength=[" + minLength + "] greater than length=[" + length + "]");
        }
        String original = this.pickOneRandomWord(length);
        if (original == null) {
            throw new IllegalArgumentException("Cannot find valid word to create game state");
        }
        String scramble = this.scramble(original);
        Map<String, Boolean> subWords = new TreeMap<>();
        for (String subWord : this.generateSubWords(original, minLength)) {
            subWords.put(subWord, Boolean.FALSE);
        }
        return new GameState(original, scramble, subWords);
    }

}
