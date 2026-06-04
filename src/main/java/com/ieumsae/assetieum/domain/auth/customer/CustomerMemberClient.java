package com.ieumsae.assetieum.domain.auth.customer;

import java.util.Optional;

public interface CustomerMemberClient {

	Optional<CustomerMember> authenticate(String employeeNumber, String rawPassword);
}
