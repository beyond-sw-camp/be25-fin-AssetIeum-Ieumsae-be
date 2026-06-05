package com.ieumsae.assetieum.domain.auth.client;

import java.util.Optional;

public interface LoginMemberClient {

	Optional<LoginMember> authenticate(String memberNo, String rawPassword);
}
