package gadgetarium.services.impl;

import gadgetarium.repositories.BannerRepository;
import gadgetarium.services.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class BannerServiceImpl implements BannerService {
    private final BannerRepository bannerRepo;
}
