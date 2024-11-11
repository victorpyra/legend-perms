package fyi.tiko.perms.utils;

/**
 * Used to translate durations to a readable format and back.
 *
 * @author tiko
 */
public class Translators {

    /**
     * Private constructor to hide the implicit public one.
     */
    private Translators() {

    }

    /**
     * Checks if the given duration is in the correct format.
     * The format has to be: 1d 2h 3m.
     *
     * @param duration The duration to check.
     * @return True if the duration is in the correct format.
     */
    public static boolean isCorrectDurationFormat(String duration) {
        var split = duration.split(" ");

        try {
            Long.parseLong(split[0].replace("d", ""));
            Long.parseLong(split[1].replace("h", ""));
            Long.parseLong(split[2].replace("m", ""));
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Translates the given duration to seconds.
     * The format has to be: 1d 2h 3m.
     *
     * @param duration The duration to translate.
     * @return The duration in seconds.
     */
    public static long translateDurationSeconds(String duration) {
        var split = duration.split(" ");
        var days = Long.parseLong(split[0].replace("d", ""));
        var hours = Long.parseLong(split[1].replace("h", ""));
        var minutes = Long.parseLong(split[2].replace("m", ""));

        return (days * 24 * 60 * 60) + (hours * 60 * 60) + (minutes * 60);
    }

    /**
     * Translates the given seconds to the format: 1d 2h 3m.
     *
     * @param seconds The seconds to translate.
     * @return The formatted duration.
     */
    public static String secondsToFormat(long seconds) {
        var days = seconds / (24 * 60 * 60);
        var hours = (seconds % (24 * 60 * 60)) / (60 * 60);
        var minutes = ((seconds % (24 * 60 * 60)) % (60 * 60)) / 60;

        return days + "d " + hours + "h " + minutes + "m";
    }
}
