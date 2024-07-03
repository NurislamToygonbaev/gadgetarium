package gadgetarium.dto.response;

import lombok.*;

import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColorResponse {

   private List<ColorResponseWithCount> countList;
}
