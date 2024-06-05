package gadgetarium.services.impl;

import gadgetarium.dto.request.BannerRequest;
import gadgetarium.dto.response.BannerResponse;
import gadgetarium.dto.response.GetAllBannerResponse;
import gadgetarium.entities.Banner;
import gadgetarium.repositories.BannerRepository;
import gadgetarium.services.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepo;

    @Override
    public BannerResponse create(BannerRequest bannerRequest) {
        Banner banner = new Banner();
        banner.setImages(bannerRequest.images());
        bannerRepo.save(banner);
        return BannerResponse.builder()
                .id(banner.getId())
                .images(banner.getImages())
                .build();
    }

    @Override
    public List<GetAllBannerResponse> getAll() {
        List<Banner> banners = bannerRepo.findAll();
        return banners.stream()
                .map(banner -> new GetAllBannerResponse(banner.getId(), banner.getImages()))
                .collect(Collectors.toList());
    }

    @Override
    public GetAllBannerResponse getById(Long bannerId) {
        Banner banner = bannerRepo.getBannerById(bannerId);
        return GetAllBannerResponse.builder()
                .id(banner.getId())
                .images(banner.getImages())
                .build();
    }
}
