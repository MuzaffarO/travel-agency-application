package com.travelbackendapp.travelmanagement.service;

import com.travelbackendapp.travelmanagement.model.api.request.UpdateNameRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.response.*;
import com.travelbackendapp.travelmanagement.model.api.request.SignUpRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.SignInRequestDTO;

public interface AuthService {

    SignUpResponseDTO signUp(SignUpRequestDTO signUpRequest);

    SignInResponseDTO signIn(SignInRequestDTO signInRequest);


    UserInfoResponseDTO getUserInfo(String userIdEmail);

    UpdateProfileResponseDTO changePassword(String userEmail, String currentPassword, String newPassword);

    UpdateNameResponseDTO updateUserName(String userIdEmail, UpdateNameRequestDTO req);
    UpdateProfileResponseDTO updateUserPicture(String userEmail, String imageUrl);

}
