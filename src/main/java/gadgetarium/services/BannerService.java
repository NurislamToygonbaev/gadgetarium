package gadgetarium.services;

import gadgetarium.dto.request.BannerRequest;
import gadgetarium.dto.response.BannerResponse;
import gadgetarium.dto.response.GetAllBannerResponse;
import gadgetarium.dto.response.HttpResponse;

import java.util.List;

public interface BannerService {

    HttpResponse create(BannerRequest bannerRequest);

    List<GetAllBannerResponse> getAll();

    GetAllBannerResponse getById(Long bannerId);

    HttpResponse deleteBannerById(Long bannerId);
}
