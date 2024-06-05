package gadgetarium.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetAllBannerResponse {

    private Long id;
    private List<String> images;
}
