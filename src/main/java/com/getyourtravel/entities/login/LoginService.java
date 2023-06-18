package com.getyourtravel.entities.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired private LoginRepository loginRepository;

    public void save(Login login) {
        loginRepository.save(login);
    }
}