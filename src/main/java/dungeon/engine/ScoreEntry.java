package dungeon.engine;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a single entry in the top scores list.
 * Stores the score achieved and the date it was achieved.
 * Implements Comparable to allow sorting by score.
 */
public class ScoreEntry implements Comparable<ScoreEntry> {
    private final int score;
    private final LocalDate date; // Using LocalDate for date

    /**
     * Constructs a new ScoreEntry with the given score and the current date.
     *
     * @param score The score achieved.
     */
    public ScoreEntry(int score) {
        this.score = score;
        this.date = LocalDate.now(); // Date of creation
    }

    /**
     * Constructs a new ScoreEntry with the given score and a specific date.
     * This constructor can be useful for loading existing scores.
     *
     * @param score The score achieved.
     * @param date  The date the score was achieved.
     */
    public ScoreEntry(int score, LocalDate date) {
        this.score = score;
        this.date = date;
    }

    /**
     * Returns the score of this entry.
     *
     * @return The score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Returns the date of this entry.
     *
     * @return The LocalDate object representing the date.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns the formatted date string (e.g., "DD/MM/YYYY").
     *
     * @return The formatted date string.
     */
    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Compares this ScoreEntry with another ScoreEntry based on score in descending order.
     * Higher scores come first. If scores are equal, order by date (older first).
     *
     * @param other The other ScoreEntry to compare to.
     * @return A negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(ScoreEntry other) {
        // Sort in descending order of score
        int scoreComparison = Integer.compare(other.score, this.score);
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        // If scores are equal, sort by date ascending (older first)
        return this.date.compareTo(other.date);
    }

    /**
     * Returns a string representation of the ScoreEntry.
     *
     * @return A string in the format "Score: [score], Date: [formattedDate]".
     */
    @Override
    public String toString() {
        return "Score: " + score + ", Date: " + getFormattedDate();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o The reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreEntry that = (ScoreEntry) o;
        return score == that.score && Objects.equals(date, that.date);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(score, date);
    }
}