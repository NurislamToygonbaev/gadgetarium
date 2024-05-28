package gadgetarium.dto.request;

import gadgetarium.validations.string.StringValidation;

public record AdminRequest(
        @StringValidation
        String responseAdmin
) {
}
