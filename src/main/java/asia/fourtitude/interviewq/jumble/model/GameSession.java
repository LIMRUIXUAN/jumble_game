package asia.fourtitude.interviewq.jumble.model;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    private String id;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "modified_at", nullable = false)
    private Date modifiedAt;

    @Column(name = "original_word", nullable = false)
    private String originalWord;

    @Column(name = "scramble_word", nullable = false)
    private String scrambleWord;

    @Column(name = "sub_words", columnDefinition = "TEXT")
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Boolean> subWords;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getOriginalWord() {
        return originalWord;
    }

    public void setOriginalWord(String originalWord) {
        this.originalWord = originalWord;
    }

    public String getScrambleWord() {
        return scrambleWord;
    }

    public void setScrambleWord(String scrambleWord) {
        this.scrambleWord = scrambleWord;
    }

    public Map<String, Boolean> getSubWords() {
        return subWords;
    }

    public void setSubWords(Map<String, Boolean> subWords) {
        this.subWords = subWords;
    }
}