package gadgetarium.dto.request;

import gadgetarium.validation.string.StringValidation;

public record AdminRequest(
        @StringValidation
        String responseAdmin
) {
}
