package gadgetarium.dto.response;
import lombok.Builder;
import java.util.List;

@Builder
public record ComparedGadgetsResponse(
        int smartphones,
        int laptops,
        int headphones,
        List<SubGadgetResponse> subGadgetResponses
){}
