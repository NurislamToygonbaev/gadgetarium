package gadgetarium.services;

import gadgetarium.dto.request.SelectCategoryRequest;
import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.ComparedGadgetsResponse;
import gadgetarium.dto.response.SignResponse;

public interface UserService {

    SignResponse signUp(SignUpRequest signUpRequest);

    SignResponse signIn(SignInRequest signInRequest);

    ComparedGadgetsResponse compare(String diffClear, SelectCategoryRequest selectCategoryRequest);
}
