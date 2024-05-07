package gadgetarium.services;

import gadgetarium.dto.request.CategoryNameRequest;
import gadgetarium.dto.request.SignInRequest;
import gadgetarium.dto.request.SignUpRequest;
import gadgetarium.dto.response.*;

import java.util.List;

public interface UserService {

    SignResponse signUp(SignUpRequest signUpRequest);

    SignResponse signIn(SignInRequest signInRequest);

    HttpResponse addCompare(Long subGadgetsId);

    List<ListComparisonResponse> seeComparison();

    ComparedGadgetsResponse compare(CategoryNameRequest selectCategory, boolean isDifferences);

    HttpResponse deleteSubGadget(Long subId);

    HttpResponse deleteAllGadgets();

    HttpResponse addToFavorites(Long subGadgetId);

    HttpResponse addAllGadgetsToFavorites(List<Long> subGadgetId);

    List<ListComparisonResponse> seeFavorites();

    List<AllFavoritesResponse> getAllFavorites();

    HttpResponse deleteById(Long subGadgetId);

    HttpResponse clearFavorites();
}
