package gadgetarium.dto.request;

import gadgetarium.validation.warranty.WarrantyValidation;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AddProductRequest(
        List<ProductsRequest> productsRequests,
        String nameOfGadget,
        LocalDate dateOfIssue,
        @WarrantyValidation
        int warranty
) {
}
