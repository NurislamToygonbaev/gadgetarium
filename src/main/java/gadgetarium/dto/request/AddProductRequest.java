package gadgetarium.dto.request;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AddProductRequest(
        List<ProductsRequest> productsRequests,
        String nameOfGadget,
        LocalDate dateOfIssue,
        int warranty
) {
}
