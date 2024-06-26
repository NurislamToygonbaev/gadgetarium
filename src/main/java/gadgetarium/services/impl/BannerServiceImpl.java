package gadgetarium.services.impl;

import gadgetarium.dto.request.BannerRequest;
import gadgetarium.dto.response.GetAllBannerResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.entities.Banner;
import gadgetarium.repositories.BannerRepository;
import gadgetarium.services.BannerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepo;

    @Override
    @Transactional
    public HttpResponse create(BannerRequest bannerRequest) {
        for (String image : bannerRequest.images()) {
            Banner banner = new Banner();
            banner.setImage(image);
            bannerRepo.save(banner);
        }
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("success added banner")
                .build();
    }

    @Override
    public List<GetAllBannerResponse> getAll() {
        List<Banner> banners = bannerRepo.findAll();
        return banners.stream()
                .map(banner -> new GetAllBannerResponse(banner.getId(), banner.getImage()))
                .sorted(Comparator.comparingLong(GetAllBannerResponse::getId))
                .collect(Collectors.toList());
    }

    @Override
    public GetAllBannerResponse getById(Long bannerId) {
        Banner banner = bannerRepo.getBannerById(bannerId);
        return GetAllBannerResponse.builder()
                .id(banner.getId())
                .images(banner.getImage())
                .build();
    }

    @Override
    public HttpResponse deleteBannerById(Long bannerId) {
        Banner banner = bannerRepo.getBannerById(bannerId);
        bannerRepo.delete(banner);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("success deleted banner with id: "+banner.getId())
                .build();
    }
}
