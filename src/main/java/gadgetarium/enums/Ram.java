package gadgetarium.enums;

import gadgetarium.exceptions.BadRequestException;
import gadgetarium.exceptions.IllegalArgumentException;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum Ram {
    RAM_4("4"),
    RAM_6("6"),
    RAM_8("8"),
    RAM_12("12"),
    RAM_16("16"),
    RAM_32("32");

    private final String russianName;

    private static final Map<String, String> englishToRussianMap = new HashMap<>();
    private static final Map<String, String> russianToEnglishMap = new HashMap<>();

    static {
        for (Ram ram : Ram.values()) {
            englishToRussianMap.put(ram.name(), ram.russianName);
            russianToEnglishMap.put(ram.russianName, ram.name());
        }
    }

    Ram(String russianName) {
        this.russianName = russianName;
    }

    public static String getRamToRussian(String englishName) {
        String russianName = englishToRussianMap.get(englishName);
        if (russianName == null) {
            throw new BadRequestException("No such memory type: " + englishName);
        }
        return russianName;
    }

    public static String getRamToEnglish(String russianName) {
        String englishName = russianToEnglishMap.get(russianName);
        if (englishName == null) {
            throw new BadRequestException("No such memory type: " + russianName);
        }
        return englishName;
    }
}
