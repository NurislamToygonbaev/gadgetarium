package gadgetarium.enums;

import gadgetarium.exceptions.BadRequestException;

public enum Ram {
    RAM_4,
    RAM_6,
    RAM_8,
    RAM_12,
    RAM_16,
    RAM_32;

    public static Ram fromString(String ramString) {
        switch (ramString) {
            case "RAM_4":
                return RAM_4;
            case "RAM_6":
                return RAM_6;
            case "RAM_8":
                return RAM_8;
            case "RAM_12":
                return RAM_12;
            case "RAM_16":
                return RAM_16;
            case "RAM_32":
                return RAM_32;
            default:
                throw new BadRequestException("Unsupported RAM size: " + ramString);
        }
    }
}
