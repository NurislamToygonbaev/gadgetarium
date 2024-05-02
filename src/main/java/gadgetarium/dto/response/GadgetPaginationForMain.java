package gadgetarium.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GadgetPaginationForMain(
        int page,
        int size,
        List<GadgetResponseMainPage> mainPages
) {
}
