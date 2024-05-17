package gadgetarium.enums;

import gadgetarium.exceptions.BadRequestException;

public enum Memory {
    GB_16,
    GB_32,
    GB_64,
    GB_128,
    GB_256,
    GB_512,
    TB_1;

    public static Memory fromString(String memoryString) {
        switch (memoryString) {
            case "GB_16":
                return GB_16;
            case "GB_32":
                return GB_32;
            case "GB_64":
                return GB_64;
            case "GB_128":
                return GB_128;
            case "GB_256":
                return GB_256;
            case "GB_512":
                return GB_512;
            case "TB_1":
                return TB_1;
            default:
                throw new BadRequestException("Unsupported memory size: " + memoryString);
        }
    }

}
