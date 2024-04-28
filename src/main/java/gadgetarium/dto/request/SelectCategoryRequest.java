package gadgetarium.dto.request;

import lombok.Builder;

@Builder
public record SelectCategoryRequest (
        String nameCategory
){}
