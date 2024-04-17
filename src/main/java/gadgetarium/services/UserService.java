package gadgetarium.services;

import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.ComparedGadgetsResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ListComparisonResponse;
import gadgetarium.dto.response.SignResponse;

import java.util.List;

public interface UserService {

    SignResponse signUp(SignUpRequest signUpRequest);

    SignResponse signIn(SignInRequest signInRequest);

    HttpResponse addCompare(Long subGadgetsId);

    List<ListComparisonResponse> seeComparison();

    ComparedGadgetsResponse compare(String selectCategory, boolean differences);

    HttpResponse deleteSubGadget(Long subId);

    HttpResponse deleteAllGadgets();
}
