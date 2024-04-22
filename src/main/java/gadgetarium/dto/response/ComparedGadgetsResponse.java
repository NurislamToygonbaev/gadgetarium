package gadgetarium.dto.response;
import lombok.Builder;
import java.util.List;
import java.util.Map;

@Builder
public record ComparedGadgetsResponse(
        Map<String, Integer> categoryCounts,
        List<SampleResponse> subGadgetResponses
){}
