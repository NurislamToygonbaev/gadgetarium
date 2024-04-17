package gadgetarium.services.impl;

import gadgetarium.dto.request.BannerRequest;
import gadgetarium.dto.response.BannerResponse;
import gadgetarium.entities.Banner;
import gadgetarium.repositories.BannerRepository;
import gadgetarium.services.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
