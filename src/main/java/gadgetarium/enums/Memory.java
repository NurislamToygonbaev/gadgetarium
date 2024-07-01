package gadgetarium.enums;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum Memory {
    GB_16("16", "16"),
    GB_32("32", "32"),
    GB_64("64", "64"),
    GB_128("128", "128"),
    GB_256("256", "256"),
    GB_512("512", "512"),
    TB_1("1", "1");

    private final String name;

    private static final Map<String, Memory> nameToMemoryMap = new HashMap<>();

    static {
        for (Memory memory : Memory.values()) {
            nameToMemoryMap.put(memory.name, memory);
        }
    }

    Memory(String name, String russianName) {
        this.name = name;
    }

    public String getRussianName() {
        return name;
    }

    public static Memory fromName(String name) {
        Memory memory = nameToMemoryMap.get(name);
        if (memory == null) {
            throw new IllegalArgumentException("No such memory type: " + name);
        }
        return memory;
    }
}

