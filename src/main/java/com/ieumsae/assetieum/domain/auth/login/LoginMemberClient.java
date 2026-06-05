package com.ieumsae.assetieum.domain.auth.login;

import java.util.Optional;

public interface LoginMemberClient {

	Optional<LoginMember> authenticate(String memberNo, String rawPassword);
}
