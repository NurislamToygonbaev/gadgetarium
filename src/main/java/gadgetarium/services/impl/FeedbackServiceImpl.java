package gadgetarium.services.impl;

import gadgetarium.repositories.FeedbackRepository;
import gadgetarium.services.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepo;
}
