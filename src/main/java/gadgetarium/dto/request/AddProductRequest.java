package gadgetarium.dto.request;

import gadgetarium.validations.issueDate.IssueDateValidation;
import gadgetarium.validations.warranty.WarrantyValidation;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AddProductRequest(
        String nameOfGadget,
        @IssueDateValidation
        LocalDate dateOfIssue,
        @WarrantyValidation
        int warranty,
        List<ProductsRequest> productsRequests
        ) {
}
