package gadgetarium.services;

import gadgetarium.dto.request.BannerRequest;
import gadgetarium.dto.response.BannerResponse;
import gadgetarium.dto.response.GetAllBannerResponse;

import java.util.List;

public interface BannerService {

    BannerResponse create(BannerRequest bannerRequest);

    List<GetAllBannerResponse> getAll();

    GetAllBannerResponse getById(Long bannerId);
}
