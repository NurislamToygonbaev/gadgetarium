package gadgetarium.services;

import gadgetarium.dto.request.BannerRequest;
import gadgetarium.dto.response.BannerResponse;

public interface BannerService {

    BannerResponse create(BannerRequest bannerRequest);
}
