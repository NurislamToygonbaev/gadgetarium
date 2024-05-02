package gadgetarium.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class InfoResponseFor{
        private BigDecimal currentPeriod;
        private BigDecimal previousPeriod;
}
