package gadgetarium.enums;

import gadgetarium.exceptions.BadRequestException;
import gadgetarium.exceptions.IllegalArgumentException;

import java.util.HashMap;
import java.util.Map;

public enum Memory {
    GB_16("16"),
    GB_32("32"),
    GB_64("64"),
    GB_128("128"),
    GB_256("256"),
    GB_512("512"),
    TB_1("1");

    private final String russianName;

    private static final Map<String, String> englishToRussianMap = new HashMap<>();
    private static final Map<String, String> russianToEnglishMap = new HashMap<>();

    static {
        for (Memory memory : Memory.values()) {
            englishToRussianMap.put(memory.name(), memory.russianName);
            russianToEnglishMap.put(memory.russianName, memory.name());
        }
    }

    Memory(String russianName) {
        this.russianName = russianName;
    }

    public static String getMemoryToRussian(String englishName) {
        String russianName = englishToRussianMap.get(englishName);
        if (russianName == null) {
            throw new BadRequestException("No such memory type: " + englishName);
        }
        return russianName;
    }

    public static String getMemoryToEnglish(String russianName) {
        String englishName = russianToEnglishMap.get(russianName);
        if (englishName == null) {
            throw new BadRequestException("No such memory type: " + russianName);
        }
        return englishName;
    }
}
